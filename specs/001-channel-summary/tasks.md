---

description: "Task list for Channel Summary feature"
---

# Tasks: Channel Summary

**Input**: Design documents from `/specs/001-channel-summary/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are OPTIONAL. The spec does not explicitly request TDD; acceptance scenarios guide manual validation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Minimal project setup and structure

- [ ] T001 Create `SummaryCommand.java` in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T002 [P] Verify OpenAI key is present in src/main/java/com/georgster/gpt/openaikey.txt
- [ ] T003 [P] Verify Discord4J and OpenAI SDK dependencies in pom.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story

- [ ] T004 [P] Add `SUMMARYCOMMAND` to enum in src/main/java/com/georgster/permissions/PermissibleAction.java
- [ ] T005 Implement `createSummaryCompletion(List<String>, String)` in src/main/java/com/georgster/control/manager/UserProfileManager.java
- [ ] T006 [P] Register `SummaryCommand` in src/main/java/com/georgster/control/CommandRegistry.java
- [ ] T007 [P] Add slash command descriptor in src/main/java/com/georgster/summary/SummaryCommand.java via `getCommandApplicationInformation()`

**Checkpoint**: Foundation ready — user story implementation can begin

---

## Phase 3: User Story 1 — User Requests Channel Summary (Priority: P1) 🎯 MVP

**Goal**: A user invokes `/summary` or `!summary` and receives a one-paragraph recap (≤75 words) of the last 50 messages.

**Independent Test**: Invoke the command in a channel with history; verify a single-paragraph reply under 75 words is posted.

### Implementation for User Story 1

- [ ] T008 [US1] Implement `execute()` to retrieve 50 messages, filter bots/system, call `UserProfileManager.createSummaryCompletion`, and post reply in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T009 [P] [US1] Implement `getAliases()` with ["summary", "summarize", "recap"] in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T010 [P] [US1] Implement `getCommandParser()` (no-arg) in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T011 [P] [US1] Implement `help()` text in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T012 [US1] Implement `shouldDefer()` returning `true` in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T013 [US1] Add `MultiLogger` success/error logs in src/main/java/com/georgster/summary/SummaryCommand.java

**Checkpoint**: User Story 1 functional and independently testable

---

## Phase 4: User Story 2 — Command Handles Few Messages (Priority: P2)

**Goal**: When fewer than 50 messages exist, summarize all available messages gracefully.

**Independent Test**: Invoke in a low-activity channel; verify a valid summary is returned.

### Implementation for User Story 2

- [ ] T014 [US2] Ensure logic summarizes available messages (<50) in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T015 [P] [US2] Post "No messages to summarize in this channel." when none/filtered-empty in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T016 [P] [US2] Ensure prompt construction handles variable message counts in src/main/java/com/georgster/control/manager/UserProfileManager.java

**Checkpoint**: User Stories 1 and 2 work independently

---

## Phase 5: User Story 3 — Command Respects Channel Context (Priority: P1)

**Goal**: Summarize only the channel where the command was invoked.

**Independent Test**: Invoke in multiple channels; each summary reflects only the local channel.

### Implementation for User Story 3

- [ ] T017 [US3] Retrieve messages from invocation channel via `CommandExecutionEvent` in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T018 [P] [US3] Filter to `Message.Type.DEFAULT` and `Message.Type.REPLY` in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T019 [P] [US3] Post summary reply in the same channel via `GuildInteractionHandler.sendPlainMessage()` in src/main/java/com/georgster/summary/SummaryCommand.java

**Checkpoint**: All user stories now independently functional

---

## Phase N: Polish & Cross-Cutting Concerns

- [ ] T020 [P] Update Discord command contract to reflect implementation in specs/001-channel-summary/contracts/discord-command-contract.md
- [ ] T021 [P] Update Java method contracts to reflect implementation in specs/001-channel-summary/contracts/java-method-contracts.md
- [ ] T022 [P] Update quickstart checkpoints to Completed in specs/001-channel-summary/quickstart.md
- [ ] T023 Code cleanup in src/main/java/com/georgster/summary/SummaryCommand.java
- [ ] T024 [P] Validate constitution and performance notes in specs/001-channel-summary/plan.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **User Stories (Phase 3+)**: Depend on Foundational completion
  - US1 (P1) → core implementation
  - US3 (P1) → can proceed in parallel with US1 (same file, coordinate merges)
  - US2 (P2) → follows after US1 to validate edge handling
- **Polish (Final)**: After desired user stories complete

### User Story Dependencies

- **US1 (P1)**: No dependencies on other stories
- **US2 (P2)**: Depends on US1 implementation patterns; independently testable
- **US3 (P1)**: No dependency on US2; complements US1, independently testable

### Within Each User Story

- Models → services → command logic → response
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- [P] Setup T002–T003 can run in parallel
- [P] Foundational T004, T006–T007 can run in parallel
- [P] US1 tasks T009–T011 can run in parallel
- [P] US2 tasks T015–T016 can run in parallel
- [P] US3 tasks T018–T019 can run in parallel
- [P] Polish T020–T022, T024 can run in parallel

---

## Parallel Examples per Story

- **US1**: Run T009, T010, T011 together; T008 and T012–T013 serialize
- **US2**: Run T015 and T016 together; validate T014 afterward
- **US3**: Run T018 and T019 together; validate T017 afterward

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup → Foundational
2. Implement US1 (T008–T013)
3. Validate independently in Discord
4. Optionally deploy/demo

### Incremental Delivery

1. Setup + Foundational
2. Add US1 → test independently → deploy/demo
3. Add US2 → test independently → deploy/demo
4. Add US3 → test independently → deploy/demo

### Team Parallelization

- Developer A: US1
- Developer B: US2
- Developer C: US3

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] labels map tasks to specific user stories
- Each user story is independently completable and testable per spec.md
- Stop at checkpoints to validate story independently
