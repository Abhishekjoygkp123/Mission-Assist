# MissionAssist Development Log

This file serves as the chronological memory of the MissionAssist project. It records important development milestones, technical discoveries, critical decisions, problems encountered, and significant shifts in product direction. It is designed to help future developers and AI agents understand the evolution of the project's architecture and the reasoning behind its current state.

---

# Historical Milestones

## Milestone: Initial Communication and Translation Concept
**Date:** Not established

### Context
MissionAssist was deliberately differentiated from generic translation applications. The product direction focused on field-specific and workflow-focused assistance rather than general conversational AI.
### Development
The initial concept required translating field intent between English and Tamil, establishing the core conversation loop.
### Result
A foundational conversation flow was conceived, emphasizing predictability in high-stakes field environments.
### Lasting Impact
Set the permanent constraint that MissionAssist must not become merely an offline Google Translate clone.

## Milestone: Initial Speech Recognition Architecture
**Date:** Not established

### Context
Speech input was required to facilitate rapid field communication.
### Development
Speech recognition was developed around Android's native `SpeechRecognizer`. Early testing utilized a fixed `en-US` configuration to validate the pipeline.
### Result
The initial `SpeechRecognizerManager` was created to handle state, but language flexibility was limited.
### Lasting Impact
Proved the viability of native speech-to-text but highlighted the need for localized dynamic language handling.

## Milestone: Dynamic Language Direction and Tamil Support
**Date:** Not established

### Context
The fixed `en-US` configuration was insufficient for actual field use involving Tamil speakers.
### Development
The fixed language configuration was replaced with dynamic language handling using `ConversationDirection`, natively supporting `en-IN` and `ta-IN`.
### Result
The UI and speech layers could cleanly swap between English-to-Tamil and Tamil-to-English contexts.
### Lasting Impact
Exposed device/service-specific limitations in Tamil speech recognition, leading to deeper investigations into OEM capabilities and offline handling.

## Milestone: Offline Capability and Error Resilience
**Date:** Not established

### Context
Field users frequently operate in low-connectivity environments.
### Development
Offline speech capability investigation led to the use of Android 13+ capability detection and offline preference behavior (`EXTRA_PREFER_OFFLINE`). Concurrently, speech service Error 11 (`ERROR_SERVER_DISCONNECTED`) became a frequent failure point.
### Result
A bounded retry behavior was implemented for Error 11. Android 13 capability querying was introduced to detect on-device model availability.
### Lasting Impact
Greatly improved the resilience of the application in turbulent network conditions and established the architectural distinction between OS-level capabilities and app-level logic.

## Milestone: PhraseResolver and Translation Integration
**Date:** Not established

### Context
General translation proved too risky for high-stakes field interactions, requiring a verified fallback mechanism.
### Development
Translation was implemented using Google ML Kit's on-device translation as a fallback. `PhraseResolver` was introduced to use explicit verified phrase entries and variants.
### Result
Tamil phrase matching exposed brittleness, leading to text normalization and the introduction of experimental compound intent logic.
### Lasting Impact
Cemented the deterministic translation strategy where a deterministic verified-phrase layer takes precedence over general translation.

## Milestone: Client Abstraction and Testability
**Date:** Not established

### Context
Testing the state managers directly was blocked by Android framework limitations (e.g., `Intent`, `Build.VERSION`) throwing exceptions in ordinary JVM tests.
### Development
Platform-sensitive behavior was separated into pure Kotlin interfaces (`SpeechClient`, `TranslationClient`). Fake clients were introduced to test state-machine behavior without requiring Android framework execution.
### Result
The project improved test coverage on business logic in the tested scenario without relying on Robolectric.
### Lasting Impact
Established the current architectural dependency boundaries, ensuring business logic remains permanently decoupled from Android OS quirks.

---

# IMPORTANT DECISIONS

## Decision: Deterministic Phrase Resolution Before General Translation Fallback
### Decision
Verified field phrases must be evaluated and matched before falling back to ML Kit.
### Reason
Deterministic verified mappings are prioritized in high-stakes contexts to provide predictable, predefined behavior when a defined rule matches.
### Consequence
Introduced `PhraseResolver` as a deterministic verified-phrase layer.
### Current Status
Active

## Decision: Client Boundaries Around Platform Services
### Decision
Android SDK components (`SpeechRecognizer`, ML Kit) must be hidden behind pure Kotlin interfaces.
### Reason
Direct coupling broke pure JVM unit testing.
### Consequence
Managers only interact with `Client` interfaces, and fake implementations are used for robust state-machine testing.
### Current Status
Active

## Decision: Dynamic Language Handling via State
### Decision
Removed fixed `en-US` strings in favor of a `ConversationDirection` state object.
### Reason
Hardcoded locales broke the core requirement of English/Tamil bidirectional communication.
### Consequence
UI and Speech components now reactively observe and apply language codes (`en-IN`, `ta-IN`).
### Current Status
Active

## Decision: Bounded Error 11 Retry
### Decision
Automatically retry exactly once when `ERROR_SERVER_DISCONNECTED` occurs.
### Reason
The Android Speech Service was observed dropping connections transiently; the automatic retry was intended to reduce user friction.
### Consequence
Improved resilience in the tested scenario without risking infinite retry loops.
### Current Status
Active

## Decision: Pure Kotlin Extraction of Platform-Sensitive Logic
### Decision
Extract logic configuring intents (e.g., `EXTRA_PREFER_OFFLINE`) into pure Kotlin companion functions.
### Reason
To test business rules surrounding intent creation without invoking actual Android `Intent` objects in JVM tests.
### Consequence
Tests are fast and isolated; Android API coupling is minimized.
### Current Status
Active

---

# EXPERIMENTS AND REJECTED APPROACHES

## Rejected: Fixed `en-US` Speech Recognition
Early speech validation utilized a fixed `en-US` configuration. This approach was rejected and completely replaced by the dynamic `ConversationDirection` architecture to support Tamil (`ta-IN`) and localized English (`en-IN`).

## Rejected: Android-Dependent JVM Testing
Initial attempts to test Managers tightly coupled to Android intents resulted in framework exceptions (`RuntimeException("Stub!")`). This approach was abandoned in favor of decoupling via interfaces and extracting logic into pure Kotlin functions, deliberately avoiding Robolectric.

## Experimental: Compound Intent PhraseResolver Logic
Tamil phrase matching presented limitations when dealing with varying sentence structures. An experimental compound intent logic was added (e.g., matching "doctor" AND "need/see" terms) to resolve phrases without relying on exact string maps. This remains highly scoped and experimental, rather than a finalized NLP engine.

---

# TECHNICAL DISCOVERIES

## Discovery: Speech Service Disconnect Behavior
### Observation
The Android `SpeechRecognizer` was observed throwing `ERROR_SERVER_DISCONNECTED` (Error 11).
### Impact
Conversations were halting, which was encountered as a source of friction in the tested scenario.
### Response
Implemented a stateful bounded retry mechanism within `SpeechRecognizerManager` that intercepts Error 11 and restarts listening exactly once per session.

## Discovery: Android Framework Limitations in Pure JVM Tests
### Observation
Accessing standard Android classes (`Intent`, `Build.VERSION`) inside `app/src/test/` throws stub exceptions.
### Impact
Blocked the ability to unit-test critical offline capability logic.
### Response
Introduced `SpeechClient` interfaces and extracted offline boolean evaluations into static pure Kotlin functions.

## Discovery: Android Recognition Capability Differences by API Level
### Observation
Offline model availability can only be natively queried starting in Android 13 (API 33) via `RecognitionSupport`. Android M (API 23) introduced `EXTRA_PREFER_OFFLINE`.
### Impact
Universal offline behavior could not be guaranteed across the target SDK range.
### Response
Branched capability detection logic by API level, exposing graceful fallback states and UI capability cards rather than promising universal offline operation.

## Discovery: TTS Device Dependency
### Observation
The Android `TextToSpeech` engine relies on the OEM's installed language data.
### Impact
Synthesizing `ta-IN` text may fail if the device lacks the data, and the app cannot natively force the OEM engine to download it.
### Response
Implemented resilient TTS initialization checks that gracefully emit `LANG_MISSING_DATA` or `LANG_NOT_SUPPORTED` to the UI rather than crashing.

---

# CURRENT BASELINE

The project currently operates as a highly decoupled, reactive Kotlin/Compose application. The historical development has culminated in an architecture featuring:
* Dynamic English/Tamil speech direction swapping.
* Deterministic phrase resolution for high-stakes mappings.
* General translation fallback using on-device ML Kit translation.
* Android TTS integration.
* Offline capability detection for Speech and Translation.
* Resilient speech handling (Error 11 retries, fallback to online).
* Decoupled pure-Kotlin testing architecture utilizing fake clients.

---

# FUTURE LOG TEMPLATE

```text
## YYYY-MM-DD — Short Development Title

### Goal
[What this development aimed to achieve]

### What Changed
[Summary of the modifications]

### Files Changed
[Key files added, modified, or deleted]

### Technical Decisions
[Important architectural or product choices made]

### Discoveries
[New technical facts or constraints learned]

### Problems Encountered
[Roadblocks and how they were resolved]

### Verification
[How the change was tested]

### Result
[The final outcome]

### Remaining Work
[Known limitations or pending tasks]

### Next Step
[Immediate next action, if any]
```

> **Rule:** Future entries should record meaningful development events, architectural shifts, or discoveries, not every minor code change.

---

# LOGGING RULES FOR FUTURE AI AGENTS

Future AI agents acting as implementation partners must adhere to the following logging rules:
1. **Add an entry** to this log when a meaningful architectural, product, or implementation decision occurs.
2. **Never invent dates or history.** If an exact date is unknown, use "Date not established".
3. **Clearly distinguish** experiments from stable decisions.
4. **Record the reasoning** (why important decisions were made), not just what changed.
5. **Avoid duplicating** the `PROJECT_CONTEXT.md` document. This log is a historical trail, not a snapshot of current state.
6. **Update `PROJECT_CONTEXT.md` separately** when the current project state materially changes.
7. **Update `AI_HANDOFF.md`** when the immediate continuation state changes materially.
