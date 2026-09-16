# MissionAssist Architecture

## 1. Architecture Overview
MissionAssist employs a unidirectional data flow and state-driven architecture built on Kotlin and Jetpack Compose. The application cleanly separates platform-specific dependencies (such as Android SpeechRecognizer and Google ML Kit) from business logic through explicit Client interfaces.

* **UI Layer**: Built entirely in Jetpack Compose. It consumes reactive `StateFlow` objects and dispatch user intents.
* **Manager Layer**: Pure Kotlin state-holders (e.g., `SpeechRecognizerManager`, `TranslationManager`) that encapsulate business logic, error retry policies, and session coordination.
* **Client Interface Layer**: Boundaries (e.g., `SpeechClient`, `TranslationClient`) that define the exact operations the Managers require.
* **Platform Implementation Layer**: Android-bound classes (e.g., `DefaultSpeechClient`) that implement the interfaces and interact with the OS.

State originates in the Manager layer, flows downward via `StateFlow` to the UI, and the UI reacts by rendering corresponding Jetpack Compose components.

## 2. Project Structure
The repository is structured by feature domains within `app/src/main/java/com/missionassist/app/`:

* **`ui/`**: Contains the Jetpack Compose components, specifically `ConversationScreen.kt` and `ConversationDirection.kt`.
* **`speech/`**: Contains all speech-to-text logic (`SpeechRecognizerManager`, `SpeechClient`, `DefaultSpeechClient`, `SpeechState`).
* **`translation/`**: Contains the translation pipeline (`TranslationManager`, `PhraseResolver`, `TranslationClient`, `DefaultTranslationClient`, `TranslationState`).
* **`tts/`**: Contains the text-to-speech dictation logic (`TtsManager`).
* **`app/src/test/`**: Contains pure JVM unit tests (`FakeSpeechClient`, `SpeechRecognizerManagerTest`, `PhraseResolverTest`, `DefaultSpeechClientTest`).

## 3. Core Components

### `SpeechRecognizerManager`
* **Responsibility**: Manages the lifecycle and state of speech recognition, handling transient disconnections and tracking capabilities.
* **Inputs**: Language codes (`en-IN`, `ta-IN`), user actions (start, clear).
* **Outputs**: `StateFlow<SpeechState>`, `StateFlow<SpeechCapability?>`, `StateFlow<String?>` (download errors).
* **Dependencies**: `SpeechClient`.
* **Relationships**: Provides state to `ConversationScreen`; acts as the first step in the conversion pipeline.

### `TranslationManager`
* **Responsibility**: Orchestrates the translation process, falling back from curated phrases to ML Kit models.
* **Inputs**: Recognized text, source language, target language.
* **Outputs**: `StateFlow<TranslationState>`.
* **Dependencies**: `TranslationClient`, `PhraseResolver`.
* **Relationships**: Called by `ConversationScreen` automatically when a speech result is received.

### `TtsManager`
* **Responsibility**: Wraps the Android `TextToSpeech` engine to dictate translation results.
* **Inputs**: Translated text, language locale.
* **Outputs**: `StateFlow<TtsState>`.
* **Dependencies**: Android `Context` / `TextToSpeech`.
* **Relationships**: Invoked manually by `ConversationScreen` when "Play Translation" is pressed.

### `ConversationScreen`
* **Responsibility**: The primary Compose UI rendering the conversation data flow.
* **Inputs**: User touches, `StateFlow` emissions from the Managers.
* **Outputs**: UI updates (capability cards, text fields, error toasts).
* **Dependencies**: `SpeechRecognizerManager`, `TranslationManager`, `TtsManager`, `ConversationDirection`.
* **Relationships**: The central coordinator connecting the outputs of Speech to the inputs of Translation.

### `PhraseResolver`
* **Responsibility**: Deterministically maps canonical field phrases and intent rules to verified translations.
* **Inputs**: Recognized text string, source language, target language.
* **Outputs**: Optional translated string.
* **Dependencies**: None (pure Kotlin logic).
* **Relationships**: Consulted by `TranslationManager` before invoking ML Kit.

### `SpeechClient` & `TranslationClient`
* **Responsibility**: Explicit interfaces defining platform contracts.
* **Inputs**: Business parameters (e.g., `languageCode`).
* **Outputs**: Callbacks (`onSuccess`, `onError`, `onResults`).
* **Dependencies**: None (pure Kotlin).
* **Relationships**: Injected into Managers to facilitate testing.

### `DefaultSpeechClient` & `DefaultTranslationClient`
* **Responsibility**: The concrete Android implementations of the interfaces.
* **Inputs**: Requests from Managers.
* **Outputs**: Executed platform commands.
* **Dependencies**: Android SDK (`SpeechRecognizer`, `Intent`, ML Kit `Translator`).
* **Relationships**: Implement the interfaces passed to the Managers.

## 4. State Management
State is managed exclusively through Kotlin Coroutines `StateFlow`.
* **State-Holder Pattern**: Each Manager exposes a public `val state: StateFlow<State>` and a private mutable backing property `_state`.
* **UI Observation**: `ConversationScreen` uses `collectAsState()` to reactively re-render when state changes.
* **Transitions**: State classes are modeled as sealed classes. For example, `TranslationState` shifts from `Idle` -> `DownloadingModel` -> `Translating` -> `Success(text, source)` or `Error(message)`.
* **Session Protection**: Managers utilize a `currentSessionId` variable. If asynchronous callbacks return after a session is cleared or swapped, the stale callbacks are aggressively ignored.

## 5. Main Conversation Flow

```text
User Action (Start Conversation)
    ↓
Conversation UI (ConversationScreen)
    ↓
SpeechRecognizerManager
    ↓
SpeechClient (DefaultSpeechClient)
    ↓
Recognized Text (SpeechState.Result)
    ↓
LaunchedEffect in UI triggers TranslationManager
    ↓
PhraseResolver
    ↓
Verified Phrase?
   ↙       ↘
 Yes       No
 ↓          ↓
Verified   ML Kit TranslationClient
Result     (DefaultTranslationClient)
   ↘       ↙
 TranslationState.Success (Text, Source)
        ↓
       UI (Card rendering)
        ↓
User Action (Play Translation)
        ↓
      TtsManager (TextToSpeech)
```

## 6. Speech Recognition Architecture
Speech relies on Android's `SpeechRecognizer` abstracted behind `SpeechClient`.
* **Language Selection**: Controlled dynamically by `ConversationDirection` (`en-IN`, `ta-IN`). `en-US` is not hardcoded.
* **Capability Detection**: On Android 13+ (API 33+), the app queries `RecognitionSupport` to natively categorize support (`ON_DEVICE_AVAILABLE`, `ON_DEVICE_NOT_DOWNLOADED`, `ONLINE_ONLY`).
* **Offline Preference**: Evaluated in pure Kotlin via `shouldPreferOffline` and `shouldUseOnDevice`. On Android M+ (API 23+), when on-device recognition is explicitly selected, `RecognizerIntent.EXTRA_PREFER_OFFLINE` is forced to `true`. Standard/online recognizers do not force this preference.
* **Error Handling & Retry**: If the server drops (Error 11, `ERROR_SERVER_DISCONNECTED`), `SpeechRecognizerManager` explicitly executes a single recursive retry. 
* **Fallback Behavior**: If a language is not supported on-device (Error 12/13), it automatically attempts a fallback to standard online recognition.

## 7. Translation Architecture
Translation coordinates deterministic lookups with Google ML Kit.
* **Offline ML Kit Behavior**: ML Kit executes 100% on-device *once* the required models are downloaded. The initial download (`TranslationState.DownloadingModel`) requires internet connectivity.
* **Result Handling**: Emits `TranslationState.Success` attaching a `TranslationSource` (`VERIFIED_PHRASE` or `ML_KIT`).
* **ML Kit Role**: ML Kit acts purely as a general translation fallback. It is explicitly labeled in the UI as an "on-device ML Kit translation" with a warning note to verify critical information.

## 8. PhraseResolver Architecture
The deterministic phrase resolution uses memory-mapped `PhraseEntry` models.
* **Normalization**: Inputs are trimmed, lowercased, whitespace-collapsed, and stripped of punctuation.
* **Variant Lists**: Explicit arrays of known variants (e.g., `tamilVariants = listOf("எனக்கு டாக்டர் தேவை", ...)`) map reliably back to canonical phrases.
* **Intent Rules**: A compound intent logic rule exists specifically for "I need to see a doctor," mapping any occurrence of a doctor term (`டாக்டர்`, `மருத்துவர்`) combined with a need term (`தேவை`, `வேண்டும்`).
* **Limitations**: There is absolutely no fuzzy matching, edit-distance calculation, or embedding-based matching. It is strictly deterministic. The compound intent mechanism is experimental and highly scoped.

## 9. Text-to-Speech Architecture
`TtsManager` wraps the Android `TextToSpeech` engine.
* **Language Mapping**: Converts `ta-IN` to `Locale("ta", "IN")`.
* **Initialization**: Emits `TtsState.Initializing` until the engine reports `SUCCESS`, moving to `Ready`.
* **Error Handling**: Captures `LANG_MISSING_DATA` or `LANG_NOT_SUPPORTED` and emits a readable `TtsState.Error`.
* **Cleanup**: Bound to the Composable's `DisposableEffect`, calling `tts.shutdown()` when the screen is dismissed.

## 10. UI Architecture
Built entirely on Jetpack Compose and Material 3 guidelines.
* **Reactive Rendering**: UI states (Cards, Text) respond immediately to sealed class variations from the Managers.
* **Capability Cards**: A dedicated UI card dynamically presents user-friendly speech capability statuses, offering actionable buttons like "Download for Offline Use" or "Open Speech Settings" when applicable.
* **Language Swap Interaction**: Managed by `ConversationDirection.swap()`, triggering a cascading clear of `SpeechManager` and `TranslationManager` to prevent ghost states.

## 11. Dependency Boundaries
```text
Business / State Logic (Managers)
        ↓
Interfaces / Clients (SpeechClient / TranslationClient)
        ↓
Platform Implementations (DefaultSpeechClient / DefaultTranslationClient)
```
This strict boundary exists to guarantee testability. Because Android SDK classes (`SpeechRecognizer`, `Intent`, `Build.VERSION`) cannot be instantiated in standard JVM tests, injecting `FakeSpeechClient` allows rapid, deterministic state-machine testing without relying on heavy frameworks like Robolectric.

## 12. Error Handling and Resilience
* **Recognition Disconnection**: Retries exactly once if `ERROR_SERVER_DISCONNECTED` is fired, handling transient Android service drops silently.
* **Recognition Fallback**: Drops to standard online recognition if the on-device model refuses the language (Errors 12/13).
* **Model Download State**: If a user is offline when triggering an ML Kit download, it cleanly emits a failure state rather than crashing.
* **Settings Fallback**: The "Open Speech Settings" action wraps the OS intent in a `try/catch` and returns a boolean. If a custom Android OEM removed the settings page, the UI degrades gracefully to a Toast message.

## 13. Offline Capability Architecture
MissionAssist does NOT promise universal offline capability, as many factors depend on the OS and OEMs.
### Speech Recognition
* **API Constraints**: Offline support detection is only queryable on Android 13+ (API 33+). 
* **OEM Constraints**: Offline speech requires the device's default `SpeechService` (usually Google) to have the language data installed. MissionAssist can trigger the download, but the OS fulfills it.
### Translation
* **Behavior**: ML Kit operates entirely offline.
* **Acquisition**: Initial language model acquisition (around 30MB) requires an internet connection.
### TTS
* **Behavior**: Depends strictly on device-installed language data. MissionAssist does not currently orchestrate TTS package downloads; it relies on the OS.

## 14. Testing Architecture
* **Pure JVM Tests**: Tests reside in `app/src/test/` and run without emulators or Robolectric.
* **Fake Clients**: Implementations like `FakeSpeechClient` isolate the Managers for exact state-machine verification (e.g., `SpeechRecognizerManagerTest`).
* **Android Framework Limitations**: To test intent configuration logic (like `EXTRA_PREFER_OFFLINE`), the required boolean logic was extracted into pure Kotlin companion functions inside `DefaultSpeechClient`, allowing `DefaultSpeechClientTest` to test the business rules without invoking actual `Intent` objects.

## 15. API-Level Constraints
* **Minimum SDK**: API 23 (Android 6.0 Marshmallow).
* **Target SDK**: API 37.
* **API 23+**: Supports passing `RecognizerIntent.EXTRA_PREFER_OFFLINE`.
* **API 33 (Android 13)**: Introduced `checkRecognitionSupport` for offline model capability detection. Model downloads can be triggered natively but operate silently without progress callbacks.
* **API 34 (Android 14)**: Introduced `ModelDownloadListener`, allowing precise `onProgress` and `onSuccess` tracking when triggering speech model downloads.

## 16. Architectural Strengths
* **Strict Decoupling**: Interface boundaries cleanly sever Android frameworks from state orchestration.
* **Testability**: The extraction of pure functions and fake interfaces allows instant JVM testing of complex business logic.
* **Resilience**: Intentional, verified fallback mechanisms (Error 11 retries, on-device ML Kit translation fallback) handle real-world turbulence efficiently.

## 17. Architectural Weaknesses / Constraints
* **OEM Speech Dependency**: The application is highly vulnerable to heavily customized OEM ROMs that ship broken or incompatible default `SpeechService` implementations.
* **Silent API 33 Downloads**: Because API 33 supports triggering downloads but lacks a callback listener, MissionAssist must simulate a success signal to force the UI into an `ON_DEVICE_DOWNLOADING` capability check.

## 18. Architecture Decision Boundaries
Future developers should approach the following areas with caution:
* **Platform Abstraction**: Do not pass Android `Context` or `Intent` objects directly into the Managers. Always push them down to the `Client` layer to protect JVM testability.
* **Phrase Resolution**: Do not introduce fuzzy or probabilistic matching into the verified-phrase path without explicit product and safety review. High-stakes situations demand predictable, strict matches.
* **State Management**: Do not implement UI-driven state manipulation. All mutations must occur via Manager functions mapping downward into `StateFlows`.
