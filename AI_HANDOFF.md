# MissionAssist AI Handoff

Welcome. This file is the operational entry point for any future AI agent or developer entering the MissionAssist project. It is intentionally concise and serves as the "front door" to the codebase. 

---

## 1. READ THESE FIRST

To understand the project, read the Project Brain files in the following exact order:

1. **PROJECT_CONSTITUTION.md**: The enduring, non-negotiable product principles and decision boundaries.
2. **PROJECT_CONTEXT.md**: The living current-state document combining repository facts, capabilities, and open questions.
3. **ARCHITECTURE.md**: The technical map of how components, state, and platform boundaries are actually implemented.
4. **DEVELOPMENT_LOG.md**: The chronological memory of key milestones, technical discoveries, and active architectural decisions.
5. **AI_HANDOFF.md**: This file, providing immediate operational context and handoff status.

---

## 2. NON-NEGOTIABLE PROJECT RULES

* MissionAssist must not become merely an offline Google Translate clone.
* Preserve the field-focused purpose.
* Deterministic verified-phrase mappings take precedence when a defined rule matches.
* Treat experimental PhraseResolver intent logic as strictly experimental.
* Do not assume offline behavior is universal.
* Respect Android/device/OEM boundaries.
* Do not make high-stakes language logic changes casually.
* Do not change core architecture without understanding the existing boundaries.
* Do not introduce unrelated refactors.
* Do not invent facts when repository evidence is missing.

---

## 3. CURRENT STATE AT A GLANCE

* **Tech Stack**: Android / Kotlin / Jetpack Compose.
* **Scope**: Bidirectional English and Tamil.
* **Input**: Android `SpeechRecognizer` with optional on-device recognition depending on device and speech-service capability.
* **Primary Resolution**: Deterministic `PhraseResolver` for matching high-stakes field phrases.
* **Secondary Resolution**: On-device ML Kit translation fallback.
* **Output**: Android `TextToSpeech` (TTS) dictation.
* **Offline Capability**: Gracefully managed and capability-checked; highly dependent on OS/OEM data installation.
* **State Management**: Robust unidirectional data flow using Kotlin `StateFlow`.
* **Testing**: Pure Kotlin JVM testing enabled by strict `Client` boundary interfaces isolating the Android framework.

---

## 4. CURRENTLY IMPORTANT COMPONENTS

* **`ConversationScreen`**: The central Compose UI coordinating all reactive state flows.
* **`SpeechRecognizerManager`**: The pure-Kotlin state manager handling speech lifecycles and Error 11 retries.
* **`TranslationManager`**: The pure-Kotlin state manager orchestrating `PhraseResolver` and ML Kit fallbacks.
* **`PhraseResolver`**: The deterministic engine providing predefined mappings for matching rules.
* **`TtsManager`**: The wrapper dictating results via the device TTS engine.
* **`ConversationDirection`**: The configuration enum actively swapping `en-IN` and `ta-IN` contexts.
* **`SpeechClient` / `TranslationClient`**: Interface boundaries ensuring the business logic remains decoupled from Android OS dependencies.

---

## 5. CURRENT LIMITATIONS THAT MUST NOT BE MISUNDERSTOOD

* **Speech**: Tamil speech capability depends partly on the device's installed Android speech services (usually Google). It varies heavily by OEM.
* **Translation**: On-device ML Kit translation works locally *only after* the required models are acquired. Initial acquisition requires connectivity.
* **TTS**: Tamil/English synthesis strictly depends on device language data being locally installed.
* **Android API Levels**: Speech capability behavior varies heavily by API level (e.g., API 33 is required for native offline capability querying; API 23 for `EXTRA_PREFER_OFFLINE`).
* **PhraseResolver**: The current compound intent logic (e.g., doctor + need) is strictly experimental and narrowly scoped.

---

## 6. BEFORE YOU CHANGE CODE

1. Read the Project Brain.
2. Inspect the current implementation.
3. Identify the smallest affected area.
4. Understand existing interfaces and state flow.
5. Decide whether the change affects the critical path.
6. Implement only the requested scope.
7. Verify the result.
8. Update the relevant Project Brain files.

> **Warning:** When uncertain about a high-impact product or architectural decision, stop and propose the decision rather than silently redefining the system.

---

## 7. AFTER YOU CHANGE CODE

* Verify behavior where practical.
* Report changed files.
* Record important technical decisions.
* Update `PROJECT_CONTEXT.md` if the current state changed.
* Add a meaningful entry to `DEVELOPMENT_LOG.md` when appropriate.
* Update `AI_HANDOFF.md` (the "Current Session Handoff" section below) if the immediate continuation state materially changed.

---

## 8. CURRENT OPEN QUESTIONS

* How reliably is `ta-IN` TTS language-data available out-of-the-box across standard field-issued devices?
* What is the global application lifecycle and permission behavior prior to launching `ConversationScreen`?
* Are there any remaining Android API-level capability differences severely impacting backward compatibility?

---

## 9. CURRENT PRIORITY STATUS

> **No next feature has been formally approved in the Project Brain.**

The reconnaissance recommendation to expand `PhraseResolver` is not automatically an approved roadmap item. Any major next feature requires product-owner confirmation.

---

# Current Session Handoff

*Update this section at the end of a significant development session when another AI may need to continue the work.*

## Date
2026-09-07

## What We Were Working On
Establishing the foundational Project Brain documentation (Constitution, Architecture, Context, Development Log, and AI Handoff).

## Current State
The current baseline functionality is implemented and the principal flows are operational, subject to the documented device, OS, API-level, and service dependencies. Dynamic language swapping, Error 11 resilience, deterministic phrase matching, ML Kit on-device translation fallback, and offline capability detection are established. All architectural boundaries and testing harnesses are intact.

## Files Changed
* `PROJECT_CONSTITUTION.md` (Created)
* `ARCHITECTURE.md` (Created)
* `PROJECT_CONTEXT.md` (Created)
* `DEVELOPMENT_LOG.md` (Created)
* `AI_HANDOFF.md` (Created)

## Important Decisions Made
Formalized all enduring principles, architectural constraints, and historical tracking into the Project Brain without modifying any existing application source code.

## Problems / Discoveries
Identified OEM fragmentation for Speech and TTS as major external dependencies limiting universal offline guarantees. Confirmed testing architecture relies heavily on Kotlin interface abstraction due to Robolectric absence.

## Verification Performed
N/A (Documentation synthesis only).

## Remaining Work
Awaiting product-owner confirmation on the next approved feature or roadmap priority.

## Immediate Next Step
Review the finalized Project Brain and propose the next feature expansion.
