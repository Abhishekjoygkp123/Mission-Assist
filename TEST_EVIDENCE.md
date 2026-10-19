# MissionAssist Stage F Test Evidence Report

## Environment Information

- **Date**: 2026-10-19
- **Base Commit (HEAD)**: `568e12bc1277059b4e141c15de85041bb7885db8`
- **Gradle Version**: 9.5.0
- **JDK Version**: OpenJDK 21.0.11 (Eclipse Adoptium 21.0.11+10-LTS)
- **OS**: Windows 10 (amd64)

---

## Stage-to-Commit Map

The following commits implement each stage of MissionAssist on `feature/conversation`:

### Stage A: Top-level navigation shell
- `7f0d802 2026-09-16 feat: add top-level navigation shell (Conversation/Situation)`

### Stage C: Merge of Assistance and Ministry modules
- `a1ee08e 2026-09-17 merge: bring in Assistance and Ministry modules (isolated, not yet wired into navigation)`

### Stage D: Situation -> Assistance/Ministry navigation
- `ae436e0 2026-09-17 feat: wire Situation to Assistance and Ministry navigation`
- `f987682 2026-09-17 fix: add missing onNavigateUp to MinistryScreen for Situation navigation`

### Stage E: Situation prompts, deep-linking, perf fixes, TTS shutdown, SpeechCapabilityCache
- `6e09fef 2026-09-17 feat: Stage E deterministic Situation Detection`
- `a527b74 2026-09-17 test: add ViewModel coverage for initialCategory initialization`
- `99b4ca5 2026-09-17 fix: resolve UX regression for deep-linked back navigation`
- `76466e9 2026-09-17 perf: fix Situation tab recomposition lag`
- `6c2800e 2026-09-17 perf: unblock main thread by backgrounding TTS shutdown`
- `8321980 2026-09-17 fix: prevent transient speech capability failure via session cache`

### Stage F: Use-in-conversation hand-off, verified-phrase bypass, setTranslation
- `dc2dc9a 2026-09-21 feat: implement stage F use in conversation handoff`
- `8e70fb2 2026-09-21 fix: restore auto-translate for voice flow`
- `13d333f 2026-09-22 feat: add missing emergency translations to PhraseResolver`
- `e8bb367 2026-09-24 feat: populate verified assistance items and bypass ML kit translation during hand-off`
- `9a588d2 2026-09-24 test: add test for TranslationManager.setTranslation()`
- `568e12b 2026-10-03 feat(ministry): populate verified Tamil scripture with citation`

#### Raw Git Log (`git log --pretty=format:"%h %ad %s" --date=short`):
```text
568e12b 2026-10-03 feat(ministry): populate verified Tamil scripture with citation
9a588d2 2026-09-24 test: add test for TranslationManager.setTranslation()
e8bb367 2026-09-24 feat: populate verified assistance items and bypass ML kit translation during hand-off
13d333f 2026-09-22 feat: add missing emergency translations to PhraseResolver
8e70fb2 2026-09-21 fix: restore auto-translate for voice flow
dc2dc9a 2026-09-21 feat: implement stage F use in conversation handoff
8321980 2026-09-17 fix: prevent transient speech capability failure via session cache
6c2800e 2026-09-17 perf: unblock main thread by backgrounding TTS shutdown
76466e9 2026-09-17 perf: fix Situation tab recomposition lag
99b4ca5 2026-09-17 fix: resolve UX regression for deep-linked back navigation
a527b74 2026-09-17 test: add ViewModel coverage for initialCategory initialization
6e09fef 2026-09-17 feat: Stage E deterministic Situation Detection
f987682 2026-09-17 fix: add missing onNavigateUp to MinistryScreen for Situation navigation
ae436e0 2026-09-17 feat: wire Situation to Assistance and Ministry navigation
a1ee08e 2026-09-17 merge: bring in Assistance and Ministry modules (isolated, not yet wired into navigation)
7f0d802 2026-09-16 feat: add top-level navigation shell (Conversation/Situation)
```

---

## Unit Test Results

The following counts were parsed directly from XML reports under `app/build/test-results/testDebugUnitTest/*.xml`:

| Test Class | Tests | Failures | Errors | Skipped |
| :--- | :---: | :---: | :---: | :---: |
| `com.missionassist.app.ExampleUnitTest` | 1 | 0 | 0 | 0 |
| `com.missionassist.app.assistance.data.AssistanceDataConsistencyTest` | 4 | 0 | 0 | 0 |
| `com.missionassist.app.assistance.data.AssistanceRepositoryTest` | 1 | 0 | 0 | 0 |
| `com.missionassist.app.assistance.ui.AssistanceViewModelTest` | 5 | 0 | 0 | 0 |
| `com.missionassist.app.ministry.data.MinistryDataConsistencyTest` | 3 | 0 | 0 | 0 |
| `com.missionassist.app.ministry.data.MinistryRepositoryTest` | 1 | 0 | 0 | 0 |
| `com.missionassist.app.ministry.ui.MinistryViewModelTest` | 6 | 0 | 0 | 0 |
| `com.missionassist.app.speech.DefaultSpeechClientTest` | 2 | 0 | 0 | 0 |
| `com.missionassist.app.speech.SpeechCapabilityCacheTest` | 5 | 0 | 0 | 0 |
| `com.missionassist.app.speech.SpeechRecognizerManagerTest` | 8 | 0 | 0 | 0 |
| `com.missionassist.app.translation.PhraseResolverTest` | 8 | 0 | 0 | 0 |
| `com.missionassist.app.translation.TranslationManagerTest` | 3 | 0 | 0 | 0 |
| `com.missionassist.app.ui.conversation.ConversationDirectionTest` | 3 | 0 | 0 | 0 |
| **Total** | **45** | **0** | **0** | **0** |

---

## Lint Finding

Running `.\gradlew lintDebug` exited with code 1 due to 1 error (and 18 warnings):

```text
D:\Projects\AndroidStudioProjects\MissionAssist\app\src\main\java\com\missionassist\app\speech\DefaultSpeechClient.kt:121: Error: Call requires API level 31 (current min is 23): android.speech.SpeechRecognizer#createOnDeviceSpeechRecognizer [NewApi]
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```

### Code around `createOnDeviceSpeechRecognizer` in `DefaultSpeechClient.kt`:
```kotlin
        val isOnDeviceAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        val useOnDevice = shouldUseOnDevice(Build.VERSION.SDK_INT, isOnDeviceAvailable, isFallback)

        speechRecognizer = if (useOnDevice) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
```

### Analysis of API Guard:
- `SpeechRecognizer.createOnDeviceSpeechRecognizer(context)` is guarded at runtime by `if (useOnDevice)`.
- In `DefaultSpeechClient.kt`, `shouldUseOnDevice` contains:
  ```kotlin
  fun shouldUseOnDevice(sdkInt: Int, isOnDeviceAvailable: Boolean, isFallback: Boolean): Boolean {
      return sdkInt >= Build.VERSION_CODES.S && isOnDeviceAvailable && !isFallback
  }
  ```
- While runtime execution is safe because `useOnDevice` is only true when `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S (31)`, Android Lint static analysis cannot trace the boolean condition across the function boundary of `shouldUseOnDevice`. Because there is no direct inline `if (Build.VERSION.SDK_INT >= 31)` or `@RequiresApi(31)` annotation directly enclosing line 121, Android Lint flags this call with a `[NewApi]` error.

---

## Not proven by automated tests

The automated test suite verifies repository data consistency, Unicode integrity, caching semantics, intent resolution, and ViewModel state transitions. The following behaviors rely on Android platform runtime execution and hardware/system services and are **not proven by automated tests**:

1. **Navigation behavior**: Jetpack Compose Navigation back-stack mechanics between top-level tabs and sub-screens.
2. **One-tap back from deep-linked categories**: Physical or software back button handling returning directly to the Situation tab from deep-linked Assistance or Ministry category views.
3. **Absence of the speech-capability flash**: The visual transition and suppression of transient loading/unsupported UI flashes in the Compose conversation header.
4. **Situation tab lag fix**: Frame rendering rate, smooth scroll performance, and absence of main-thread jank during rapid tab switching or recomposition.
5. **TTS playback**: Actual audio routing, Android TextToSpeech engine binding, and speech synthesis output through device speakers.
6. **Real speech recognition**: On-device and online SpeechRecognizer microphone audio capture, recognition service IPC, and speech-to-text accuracy in real acoustic environments.
