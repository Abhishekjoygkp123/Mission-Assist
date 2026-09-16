# MissionAssist Project Constitution

This document serves as the authoritative, long-term constitution for MissionAssist. It defines the foundational principles, product boundaries, architectural philosophy, and decision rules that remain stable despite implementation changes. 

## 1. Project Identity
MissionAssist is a field-focused assistance application intended to help missionaries and similar field users overcome practical communication and assistance challenges. The application is designed around real-world field workflows rather than generic translation alone.

## 2. Core Product Question
Every important product decision must ultimately help answer:
> Why should someone use MissionAssist instead of Google Translate or another general-purpose translation application?

A technically impressive feature is not automatically a valuable MissionAssist feature.

## 3. Non-Negotiable Differentiation Rule
**MissionAssist must not become merely an offline Google Translate clone.**
Translation is a capability, not the entire product. MissionAssist creates value through:
* Field-specific communication
* Context-aware assistance
* Workflow support
* Verified or purpose-specific language assistance
* Practical field resources
* Useful operation under constrained connectivity where feasible

## 4. Trust and Safety Principle
MissionAssist is used in real-world, high-stakes situations where an incorrect translation could have critical consequences.
* The system must prefer deterministic, verified behavior when a reliable mapping is available.
* AI/model-generated or probabilistic translations must not be automatically treated as equivalent to verified phrases.
* The application should make important limitations understandable to the user.
* High-stakes communication should be designed conservatively.
* The system must not create false confidence merely because a translation was successfully generated.

## 5. Context Before Cleverness
Prefer useful context and predictable behavior over impressive but unreliable intelligence. A simpler mechanism that behaves consistently in a field situation is often more valuable than a sophisticated mechanism that produces unpredictable results.

## 6. Offline Philosophy
Offline capability is an important goal, but expectations must be clear. We must distinguish between:
* Functionality that operates without an internet connection *once required resources are available*.
* Functionality that still requires initial model or resource acquisition.
Do not promise universal offline capability when the Android device, installed services, or required language models are outside of MissionAssist's control.

## 7. User Experience Principles
MissionAssist should be:
* Fast to understand and fast to operate
* Low friction
* Clear about system state
* Resilient when services fail
* Practical during real conversations

Users should not need to understand the underlying technology. Errors should lead to understandable next actions rather than unexplained technical failure messages.

## 8. Architecture Philosophy
**Prefer:** Clear separation of responsibilities, testable business logic, small focused components, explicit interfaces where they provide meaningful decoupling, minimal necessary dependencies, incremental changes, and maintainable code.
**Avoid:** Premature abstraction, unnecessary frameworks, large speculative refactors, architecture created merely for architectural elegance, and dependencies that solve problems the project does not actually have.
*Existing architecture should be preserved unless there is a concrete reason to change it.*

## 9. Critical Path Ownership
The primary project owner retains decision control over MissionAssist's critical path, including:
* Product direction and core user flows
* Core architecture
* Translation and speech recognition strategy
* Major dependencies and release-critical integration

AI agents and secondary contributors should not silently redefine these areas. They may implement clearly defined work within established boundaries.

## 10. AI Development Rules
AI agents are implementation partners, not autonomous product owners. An AI agent should:
1. Inspect existing code before modifying it.
2. Understand the current architecture.
3. Make the smallest appropriate change.
4. Preserve working functionality.
5. Avoid unrelated refactoring.
6. Report uncertainty instead of inventing certainty.
7. Verify its work where practical.
8. Document important changes.

For high-impact architectural or product decisions, the AI must present a proposal rather than silently deciding.

## 11. Change Discipline
Before introducing a meaningful change, ask:
* **Problem**: What real problem are we solving?
* **Value**: How does this improve MissionAssist?
* **Differentiation**: Does it strengthen MissionAssist's reason to exist?
* **Risk**: What existing functionality could it disrupt?
* **Simplicity**: Is there a smaller and safer way to accomplish the same result?
*If the answer to the value or differentiation question is weak, reconsider the change.*

## 12. High-Stakes Feature Principle
Features involving medical, emergency, safety, security, or similarly consequential communication require a higher standard than ordinary convenience features. For high-stakes communication:
* Prefer explicit verified mappings.
* Keep behavior predictable and make limitations visible where appropriate.
* Test representative variants.
* Avoid giving the user unjustified confidence.
* Escalate architectural or product uncertainty for review.

## 13. Feature Expansion Principle
MissionAssist should grow as a focused product rather than a pile of unrelated utilities. When evaluating a proposed feature:
> Does this make the field experience meaningfully better?

If the justification is primarily "it is technically interesting," that is not sufficient.

## 14. Definition of Done
A feature is not complete merely because implementation code exists. A meaningful change should normally have:
* Integration with the existing application.
* Appropriate testing or verification.
* No obvious regression.
* Clear error behavior.
* Updated project documentation when important knowledge changes.

## 15. Source of Truth
The MissionAssist repository and its Project Brain are the long-term memory of the project. Chat conversations are temporary working context. Important decisions must be captured in project documentation rather than relying on conversational memory.
