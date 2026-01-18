# Implementation Plan: Channel Summary

**Branch**: `001-channel-summary` | **Date**: January 17, 2026 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-channel-summary/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement a summary command that retrieves the last 50 messages from a Discord text channel, filters out bot and system messages, and uses the existing OpenAI integration (via UserProfileManager) to generate a concise one-paragraph recap under 75 words. The command will follow existing patterns in the codebase (similar to GPTCommand) and use a dedicated system prompt to constrain AI output format and length.

## Technical Context

**Language/Version**: Java (version from pom.xml, typically Java 11+)
**Primary Dependencies**: Discord4J (Discord API wrapper), OpenAI Java SDK (com.theokanning.openai), Maven (build system)
**Storage**: Database via existing DatabaseService (guild-specific data isolation via adapter pattern)
**Testing**: Existing test framework in src/test (manual testing initially)
**Target Platform**: Java server application (1 vCore, 1GB RAM constraint per Constitution)
**Project Type**: Single Java project with feature-domain organization
**Performance Goals**: <10 seconds response time for summary generation; <3 seconds for initial Discord interaction reply (slash command deferral)
**Constraints**: <100MB memory overhead per guild; graceful handling of Discord rate limits; reuse existing OpenAI service singleton
**Scale/Scope**: Support across all connected guilds; handle channels with 0 to 10,000+ messages; single new command class (~150-200 LOC)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Production Stability with Minimal Resources
✅ **PASS** - Feature uses existing OpenAI service singleton (no new connections); message retrieval is one-time per command (no persistent polling); memory footprint is minimal (50 messages × ~2KB average = ~100KB temporary per invocation)

### II. Code Organization and Clear Documentation
✅ **PASS** - New command will be placed in existing command structure; follows existing Command/ParseableCommand patterns; will include Javadoc comments matching codebase style

### III. Guild Autonomy and Feature Completeness
✅ **PASS** - Command operates per-channel, inherently guild-isolated; handles edge cases (0 messages, <50 messages, AI failures); provides user-friendly error messages

### IV. Discord4J Best Practices and Integration
✅ **PASS** - Uses Discord4J Message retrieval APIs properly; implements slash command with proper deferral (shouldDefer=true); respects Discord rate limits via existing client patterns

### V. Simplicity and Clarity
✅ **PASS** - Follows existing GPTCommand pattern; no over-engineering; straightforward message filtering and AI prompt construction

**Overall Result**: ✅ ALL GATES PASS - Proceed to Phase 0

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/georgster/
├── summary/
│   └── SummaryCommand.java          # New command implementation
├── gpt/
│   ├── GPTCommand.java              # Existing - reference for patterns
│   └── MemberChatCompletions.java   # Existing - AI conversation tracking
├── control/
│   ├── manager/
│   │   └── UserProfileManager.java  # Existing - provides AI service access
│   └── util/
│       └── CommandExecutionEvent.java # Existing - command context
├── Command.java                      # Existing - command interface
└── ParseableCommand.java            # Existing - parseable command interface

target/
└── classes/                          # Compiled output
```

**Structure Decision**: Single Java project following existing feature-domain organization. New `summary` package will contain the SummaryCommand class. The command will leverage existing infrastructure: UserProfileManager for AI access, CommandExecutionEvent for Discord context, and GuildInteractionHandler for message responses. This follows the established pattern seen in GPTCommand and other domain-specific commands (economy/, music/, events/, etc.).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | No violations | All constitution principles satisfied |

---

## Constitution Re-Check (Post Phase 1 Design)

*Re-evaluation after completing research, data model, and contracts*

### I. Production Stability with Minimal Resources
✅ **PASS** - Confirmed through design:
- SummaryCommand is instantiated per-command (no persistent state)
- createSummaryCompletion() does NOT save to database (no profile updates)
- Memory usage: ~100KB per invocation, garbage collected immediately
- Uses existing OpenAI singleton (no additional connections)
- Message retrieval uses Discord4J streaming (efficient)

### II. Code Organization and Clear Documentation
✅ **PASS** - Confirmed through contracts:
- New package: `com.georgster.summary` (follows domain pattern)
- Comprehensive Javadoc in contracts matches existing style
- Clear separation of concerns (SummaryCommand → UserProfileManager → OpenAI)
- All methods documented with pre/post conditions

### III. Guild Autonomy and Feature Completeness
✅ **PASS** - Confirmed through data model:
- Channel-scoped operation (implicit guild isolation)
- Handles all edge cases: 0 messages, all filtered, AI errors, permission errors
- User-friendly error messages defined in contracts
- No cross-guild data sharing (ephemeral operation)

### IV. Discord4J Best Practices and Integration
✅ **PASS** - Confirmed through research:
- Uses `getMessagesBefore().take(50)` (proper pagination)
- Implements `shouldDefer() = true` (prevents timeout)
- Slash command + prefix command support (ApplicationCommandRequest)
- Respects Discord message types (filters system messages correctly)
- Permission checks via PermissibleAction (standard pattern)

### V. Simplicity and Clarity
✅ **PASS** - Confirmed through quickstart:
- Follows GPTCommand pattern exactly (consistency)
- No abstraction layers (direct API calls)
- Single responsibility: retrieve → filter → summarize → post
- Estimated 150-200 LOC (simple, maintainable)
- No new dependencies required

**Overall Result**: ✅ ALL GATES PASS - Design approved for implementation

**Design Highlights**:
- Zero persistence (completely ephemeral)
- Reuses existing infrastructure (OpenAI service, command system, database service)
- Simple data flow: Discord → Filter → AI → Discord
- Error handling at all layers with user-friendly messages
- Performance well within targets (<10s, <100MB)

---

## Implementation Artifacts Generated

### Phase 0: Research (Completed)
- ✅ [research.md](research.md) - Technical decisions and best practices established

### Phase 1: Design (Completed)
- ✅ [data-model.md](data-model.md) - Entities, relationships, state transitions defined
- ✅ [contracts/discord-command-contract.md](contracts/discord-command-contract.md) - Discord API contract specified
- ✅ [contracts/java-method-contracts.md](contracts/java-method-contracts.md) - Java method signatures and behaviors defined
- ✅ [quickstart.md](quickstart.md) - Step-by-step implementation guide created

### Phase 1: Context Update (Completed)
- ✅ Agent context updated via `update-agent-context.ps1`
- ✅ GitHub Copilot instructions file created with project technologies

### Phase 2: Implementation Tasks (Next Step - NOT created by /speckit.plan)
- ⏭️ `tasks.md` will be generated by `/speckit.tasks` command
- ⏭️ This will break down implementation into atomic, trackable tasks

---

## Next Steps

1. **Review Plan**: Ensure all stakeholders approve the design (plan.md, contracts, data-model)
2. **Run /speckit.tasks**: Generate detailed implementation tasks from this plan
3. **Begin Implementation**: Follow quickstart.md guide (estimated 3-4 hours)
4. **Test**: Execute test cases from contracts/discord-command-contract.md
5. **Deploy**: Merge feature branch `001-channel-summary` to master

**Feature Branch**: `001-channel-summary`  
**Estimated Implementation Time**: 3-4 hours  
**Ready for Development**: ✅ YES


