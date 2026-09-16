# MissionAssist Project Context

**Last Updated:** 2026-09-07

## 1. Project Snapshot
MissionAssist is a Kotlin/Jetpack Compose Android app currently providing field-focused bidirectional speech-to-text, deterministic phrase matching, and on-device ML Kit translation between English and Tamil. Its main user interaction revolves around a single `ConversationScreen` that captures speech, attempts to find an exact or rule-based verified translation (e.g., medical or logistical field phrases), falls back to on-device ML Kit translation if no match is found, and dictates the result via Text-to-Speech (TTS). Its major differentiating concept is strictly prioritizing a deterministic verified-phrase layer that takes precedence over general translation when a defined match exists. This provides predictable, predefined mappings for matching rules, rather than guaranteeing correct communication in every real-world context.

## 2. Product Direction
The central product constraint is: **MissionAssist must not become merely an offline Google Translate clone.** 
Translation is treated as a component of field assistance, not the entire product. The current direction focuses on:
* **Field-focused communication:** Equipping the app to reliably handle specific interactions (e.g., medical emergencies).
* **Context-aware assistance:** Emphasizing verified, purpose-specific language support over generic conversational translation.
* **Resilience:** Operating predictably and robustly under constrained field connectivity when technical prerequisites (models) are met.

## 3. Current Capabilities

| Capability | Current State | Notes |
| ---------- | ------------- | ----- |
| Bidirectional speech-to-text | Implemented | Uses `ConversationDirection` to map `en-IN` / `ta-IN`. |
| English/Tamil switching | Implemented | Swaps UI labels and strictly flushes stale states. |
| On-device speech recognition detection | Implemented | Relies on API 33+ `RecognitionSupport` to natively query OS capabilities. |
| Offline speech preference | Implemented | Appends `EXTRA_PREFER_OFFLINE` strictly when on-device recognition is explicitly used (API 23+). |
| Speech recovery/retry | Implemented | Automatically retries exactly once on `ERROR_SERVER_DISCONNECTED` (Error 11). |
| Deterministic phrase resolution | Implemented | Memory-mapped normalization and exact variant matching against verified phrases. |
| Experimental intent rules | Experimental | Compound matching logic (e.g., doctor + need) resolving to predefined phrases. |
| ML Kit translation fallback | Implemented | Used strictly when PhraseResolver yields no match. |
| Translation model acquisition | Implemented | Initiates downloads for ML Kit models. Requires internet connection initially. |
| Offline translation | Implemented | ML Kit translates entirely on-device once models are acquired. |
| Text-to-speech (TTS) | Implemented | Uses Android `TextToSpeech` engine; heavily dependent on installed OEM language data. |
| Capability UI | Implemented | Renders capability cards indicating offline readiness or download requirements. |

## 4. Current User Flow
```text
User
 ↓
Conversation UI (Start Conversation)
 ↓
Speech Recognition Layer
 ↓
Recognized Text
 ↓
Phrase Resolution
 ↓
Verified Match?
 ├── Yes → Verified Translation (Curated field phrase)
 └── No  → ML Kit Translation (On-device ML Kit translation)
                ↓
        Translation Result
                ↓
               UI
                ↓
               TTS (Play Translation)
```

## 5. Language and Localization Context
MissionAssist currently restricts its scope to English and Tamil.
* **Speech Recognition**: Handled dynamically using `en-IN` and `ta-IN` passed directly to the Android `SpeechRecognizer`.
* **Translation**: Hardcoded translation directions between Google ML Kit's English and Tamil languages.
* **TTS Locale**: Synthesized locally using `Locale("en", "IN")` and `Locale("ta", "IN")`.

**Important Context**: While MissionAssist coordinates the language codes, actual speech recognition and text synthesis capabilities are entirely dependent on the underlying Android OEM services (e.g., Google Speech Services).

## 6. Offline Capability Status
MissionAssist pursues offline capabilities, but it is **not universally offline out of the box**.

### Speech Recognition
* **Detection**: On Android 13+ (API 33+), the app queries `RecognitionSupport` to detect if speech is `ON_DEVICE_AVAILABLE`, `ON_DEVICE_NOT_DOWNLOADED`, or `ONLINE_ONLY`. Below API 33, it falls back to standard APIs.
* **Behavior**: MissionAssist controls appending `EXTRA_PREFER_OFFLINE` to Intents (on API 23+). It does not contain the speech models themselves. The actual offline execution is controlled by the Android device's installed speech service.

### Translation
* **Behavior**: Translation is executed entirely on-device using ML Kit. 
* **Acquisition**: The initial language model acquisition (fetching Tamil/English ML Kit models) **requires an active internet connection**. Once available, connectivity is no longer needed.

### TTS
* **Behavior**: Local device synthesis via Android `TextToSpeech`.
* **Limitation**: Entirely dependent on the device having the `ta-IN` or `en-IN` language data already installed. MissionAssist currently lacks a mechanism to automatically force the download of TTS data packages.

## 7. Deterministic Translation Strategy
The `PhraseResolver` acts as a deterministic verified-phrase layer that takes precedence over general translation when a defined match exists. It contains memory-mapped arrays of canonical English and Tamil phrases and their expected variants.
* **Normalization**: Incoming text is trimmed, lowercased, collapsed, and stripped of punctuation.
* **Fallback Rule**: If the input matches a defined rule, it maps deterministically to a verified string. If not, it falls through to ML Kit.
* **Clarification**: Deterministic matching does not inherently make the translation universally correct in all human contexts; it simply makes the application's selected mapping deterministic and predictable when the input matches the defined rule.

## 8. Experimental Functionality
* **Compound Intent Logic**: Located within `PhraseResolver`, there is a hardcoded experimental intent rule that maps Tamil inputs to English ("I need to see a doctor.") if the string contains a "doctor" term AND a "need/see" term. It operates without fuzzy matching or embeddings. *This remains highly scoped and experimental.*

## 9. Resilience and Failure Handling
* **Recognition Disconnection**: If the Android Speech Service throws Error 11 (`ERROR_SERVER_DISCONNECTED`), MissionAssist intercepts it and executes exactly one automatic recursive retry to combat transient drops.
* **Recognition Fallback**: If on-device speech is rejected by the OS (Errors 12/13), it seamlessly falls back to standard/online recognition.
* **Translation Failure**: ML Kit failures or offline-download failures bubble up to the UI as readable `TranslationState.Error` messages.
* **TTS Initialization**: Caught gracefully; emits an error if `LANG_MISSING_DATA` or `LANG_NOT_SUPPORTED` occurs, displaying a Toast to the user rather than crashing.

## 10. Current Testing Position
* **Strategy**: Pure Kotlin JVM unit testing. Robolectric is intentionally avoided to force clean decoupling.
* **Fakes**: `FakeSpeechClient` and other interfaces are heavily utilized to validate the state-machine transitions in `SpeechRecognizerManager` and `TranslationManager`.
* **Android Workarounds**: To unit-test behavior like intent configuration (`EXTRA_PREFER_OFFLINE`), platform-sensitive logic was explicitly extracted into pure Kotlin companion functions (e.g., `DefaultSpeechClient.shouldPreferOffline`), achieving 100% testability on business rules without invoking actual Android `Intent` objects.

## 11. Current Strengths
* **Platform/Business Decoupling**: Explicit client boundaries allow seamless state-driven testing.
* **Resilient Speech Handling**: Error 11 retries and offline capability mapping provide excellent real-world stability.
* **Deterministic Fallback Loop**: Ensures high-stakes field phrases (like medical needs) are predictably mapped via predefined rules before falling back to general translation.
* **State-Driven Architecture**: The UI is a pure reflection of robust Kotlin `StateFlows`.

## 12. Current Limitations
* **Speech Recognition Fragmentation**: Tamil speech relies on the OEM's default Speech Service, which varies wildly in quality and offline support between manufacturers (e.g., Samsung vs. Pixel).
* **ML Kit Ambiguity**: When a phrase bypasses the `PhraseResolver` and hits ML Kit, the general translation may lose critical field context.
* **TTS Dependency**: The app assumes the OEM device has TTS language data installed; it cannot natively force the acquisition of it.
* **API Constraints**: Android 13 (API 33) natively supports model downloads but lacks progress callbacks, resulting in a silent download UX that must be manually polled or assumed successful.

## 13. Known Open Questions
* How reliably is `ta-IN` TTS data available out-of-the-box across standard field-issued devices?
* What are the global application lifecycle and permission configurations dictating how a user originally enters `ConversationScreen`?

## 14. Current Project Priorities
> **Product-owner confirmation required.**

While expanding `PhraseResolver` was recommended in the reconnaissance phase to reduce reliance on general translation, no project priority or feature roadmap has been formally approved.

## 15. Important Historical Context
* *Dynamic Language Handling:* Previous hard-coding of `en-US` in early speech testing was ripped out in favor of dynamic `en-IN` / `ta-IN` injection via `ConversationDirection`.
* *Phrase Matching Constraints:* Early Tamil phrase matching proved brittle. This led to the introduction of explicit normalized variants and the experimental compound intent rules.
* *JVM Testing Barriers:* Android framework limitations (e.g., `Intent` crashing JVM tests) historically plagued CI, leading to the current architectural choice of pure Kotlin extraction.
* *Speech Disconnects:* Error 11 was found to frequently plague prolonged conversations; it explicitly forced the introduction of the bounded retry loop.

## 16. Decision Log Summary

| Decision | Current Position | Why It Matters |
| -------- | ---------------- | -------------- |
| Deterministic phrase resolution before fallback | Active | Reduces ambiguity for verified phrases in high-stakes field environments. |
| Client boundaries around platform services | Active | Vastly improves separation of concerns and allows pure JVM testing. |
| Dynamic language handling via State | Active | Avoids inappropriate fixed language configuration and permits localized inputs. |
| Conservative treatment of high-stakes communication | Active | Prioritizes deterministic rules for predictable mapping of high-stakes phrases. |
| Error 11 Bounded Retry | Active | Silently recovers from transient Android service disconnects without user friction. |

## 17. Working Rules for Future Developers and AI Agents
1. **Read the Constitution** (`PROJECT_CONSTITUTION.md`) before changing product direction.
2. **Read the Architecture** (`ARCHITECTURE.md`) before modifying core flows or introducing state.
3. **Inspect the current implementation** before assuming historical context remains active.
4. **Preserve client boundaries** unless there is a concrete, compelling reason to alter them.
5. **Treat experimental intent logic as experimental.** Do not silently expand it without review.
6. **Do not assume offline behavior is universal.** Respect the OS boundaries of Speech and TTS.
7. **Do not casually expand high-stakes language logic.** Update the `PhraseResolver` responsibly.
8. **Update this Context file** when the project's current state materially changes.
