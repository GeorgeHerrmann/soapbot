# Implementation Plan: CS2 Faceit Integration

**Branch**: `002-cs2-faceit` | **Date**: 2026-01-19 | **Spec**: [002-cs2-faceit/spec.md](specs/002-cs2-faceit/spec.md)
**Input**: Feature specification from `/specs/002-cs2-faceit/spec.md`

## Summary

Add CS2 Faceit integration to SoapBot enabling Discord users to link Faceit accounts, view match reports with detailed statistics (K/D, ADR, HS%, MVPs), player stats (Elo, level, win rate), and create leaderboards. Implement through modular command system extending existing ParseableCommand interface, using guild-isolated SoapClient architecture. Primary delivery: P1 stories (account linking, match reports, player stats) with asynchronous Faceit API integration and 5-10 minute cache TTL to minimize rate limiting on constrained server.

## Technical Context

**Language/Version**: Java 19 (Maven 3.6+)  
**Primary Dependencies**: Discord4J 3.2.4, Gson 2.11.0, MongoDB driver 4.9.0, SLF4J 2.0.5, Jackson (NEEDS CLARIFICATION - verify if included or add)  
**Storage**: MongoDB (existing database service per guild via `com.georgster.database.DatabaseService<T>`)  
**Testing**: Maven test framework (NEEDS CLARIFICATION - determine test runner used in project)  
**Target Platform**: Linux server (1 vcore, 1GB RAM constraint)  
**Project Type**: Discord bot (single executable, guild-isolated clients)  
**Performance Goals**: Match report load <3s, leaderboard update 10min interval, 80% cache hit reduction, support 100+ linked accounts/server  
**Constraints**: <1 additional memory footprint per linked user, graceful API degradation (standardized error messages, no cached data exposure), 5-10min cache TTL per stat category  
**Scale/Scope**: 7 user stories (P1: 3, P2: 3, P3: 1), ~500 lines new code estimated, integrates with 1 external API (Faceit)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Alignment Notes |
|-----------|--------|-----------------|
| **I. Code-First, Clean Architecture** | ✅ PASS | Will utilize existing UserProfile, UserProfileManager, and ParseableCommand patterns. Modular command classes (CS2LinkCommand, CS2MatchCommand, etc.) extend existing interfaces. Integrates with SoapClient via ClientContext. |
| **II. Modular Feature Design** | ✅ PASS | CS2 commands designed as guild-isolated, independently testable modules. Each command operates within SoapClient's guild context. Leverages existing UserProfile extension model. |
| **III. Git Discipline** | ✅ PASS | Feature branch `002-cs2-faceit` established. All work committed immediately on task completion. Commit messages reference feature number and task. |
| **IV. Discord API Integration Standards** | ✅ PASS | All commands implement ParseableCommand/Command interfaces. Responses use Discord4J wrapper for embeds. Error messages user-friendly, technical details logged. Respects rate limits via caching. |
| **V. Incremental Development & Testing** | ✅ PASS | Feature broken into P1/P2/P3 user stories with acceptance criteria. Will implement P1 first (linking, match report, stats), P2/P3 follow. |
| **VI. Resource Efficiency** | ⚠️ CONDITIONAL | Caching strategy (5-10min TTL) mitigates API call overhead. MongoDB per-guild model maintains isolation. Must verify: HTTP client connection pooling, background refresh logic avoids memory leaks, cache eviction policy documented. Will validate post-Phase 1. |

**Gate Result**: ✅ CONDITIONALLY PASS - Proceed to Phase 0 with commitment to verify resource constraints in Phase 1 design.

### Post-Phase 1 Re-validation (Constitution Check)

**Status**: ✅ PASSED - All design artifacts reviewed against Constitution v1.0.0

| Principle | Final Assessment | Evidence |
|-----------|------------------|----------|
| **I. Code-First, Clean Architecture** | ✅ PASS | Data model defines 6 entities reusing existing patterns. FaceitAPIClient utility leverages Gson (existing). CS2Profile nested in UserProfile (no schema breaking). All new code extends ParseableCommand/Command interfaces. |
| **II. Modular Feature Design** | ✅ PASS | 9 modular commands (link, unlink, match, stats, history, compare, leaderboard, team, help) each independently testable. Guild isolation maintained via UserProfileManager + Caffeine key format `{guildId}:{playerId}:*`. |
| **III. Git Discipline** | ✅ PASS | Feature branch `002-cs2-faceit` created. All Phase 0-1 artifacts staged for commit on completion. Commit strategy: one commit per major deliverable (plan, research, data-model, contracts, quickstart). |
| **IV. Discord API Integration Standards** | ✅ PASS | 9 API contracts specify Discord embed formatting, error handling per FR-011 (standardized messages, no technical details). All commands use Discord4J EmbedCreateSpec. Rate limit strategy: Caffeine cache + background refresh avoids duplicate API calls. |
| **V. Incremental Development & Testing** | ✅ PASS | Phase 1 delivers design only. Phase 2 (tasks.md) will break into: P1 foundation (link, match, stats, permissions), P1 completeness (compare, history, leaderboard), P2 (team), P3 (enhancements). Each story has acceptance criteria in contracts. |
| **VI. Resource Efficiency** | ✅ PASS | Memory footprint analyzed: ~100 bytes per cached entry × 10,000 max = ~1MB. OkHttp3 connection pooling (10 max) limits concurrent connections. Caffeine LRU + TTL auto-evicts stale data. Background refresh uses CompletableFuture (non-blocking). No indefinite memory growth identified. |

**Final Gate Result**: ✅ FULL PASS - All Constitution principles satisfied. No violations. Resource efficiency constraints verified.

## Design Decisions Deviations (None Required)

All design choices align with Constitution principles. No deviations or justifications needed.

## Project Structure

### Documentation (this feature)

```text
specs/002-cs2-faceit/
├── plan.md              # This file
├── research.md          # Phase 0: Technology decisions & rationale
├── data-model.md        # Phase 1: Entity definitions & relationships
├── quickstart.md        # Phase 1: Setup & first commands guide
├── contracts/           # Phase 1: API contracts for all 9 commands
│   ├── cs2-link-contract.md
│   ├── cs2-unlink-contract.md
│   ├── cs2-match-contract.md
│   ├── cs2-stats-contract.md
│   ├── cs2-history-contract.md
│   ├── cs2-compare-contract.md
│   ├── cs2-leaderboard-contract.md
│   ├── cs2-team-contract.md
│   └── cs2-help-contract.md
└── tasks.md             # Phase 2: Implementation tasks (NOT yet created)
```

### Source Code (Implementation to follow in Phase 2)

```text
src/main/java/com/georgster/
├── game/
│   ├── CS2Command.java              # Main command router (extends ParseableCommand)
│   ├── commands/
│   │   ├── CS2LinkCommand.java       # Subcommand: link
│   │   ├── CS2UnlinkCommand.java     # Subcommand: unlink
│   │   ├── CS2MatchCommand.java      # Subcommand: match
│   │   ├── CS2StatsCommand.java      # Subcommand: stats
│   │   ├── CS2HistoryCommand.java    # Subcommand: history
│   │   ├── CS2CompareCommand.java    # Subcommand: compare
│   │   ├── CS2LeaderboardCommand.java # Subcommand: leaderboard
│   │   ├── CS2TeamCommand.java       # Subcommand: team
│   │   └── CS2HelpCommand.java       # Subcommand: help
│   └── util/
│       ├── CS2EmbedFormatter.java    # Discord embed formatting utilities
│       └── PlayerLookup.java         # Player resolution (mention/username/steam)
├── api/
│   └── faceit/
│       ├── FaceitAPIClient.java      # Faceit API HTTP client (OkHttp3)
│       ├── model/
│       │   ├── FaceitPlayer.java     # DTO: Player profile
│       │   ├── PlayerStats.java      # DTO: Career statistics
│       │   ├── MatchDetails.java     # DTO: Single match record
│       │   └── LeaderboardEntry.java # DTO: Leaderboard rank entry
│       └── exception/
│           ├── PlayerNotFoundException.java
│           └── FaceitAPIException.java
├── cache/
│   └── FaceitCache.java             # Caffeine cache manager (guild-isolated)
└── profile/
    └── UserProfile.java             # Extended with CS2Profile nested class
        └── CS2Profile               # NEW nested class for Faceit linkage

src/test/java/com/georgster/game/
├── CS2CommandTest.java              # Unit tests for command routing
├── CS2LinkCommandTest.java          # Unit tests: link validation
├── CS2MatchCommandTest.java         # Unit tests: match report formatting
├── CS2StatsCommandTest.java         # Unit tests: stats aggregation
└── ... (additional command tests)
```

**Structure Rationale**: Feature implements as guild-isolated modular commands extending existing SoapBot patterns. All new code under `com.georgster.game` (existing game feature package) with sub-packages for API, cache, and utilities. No new top-level packages to minimize architecture disruption. Tests follow existing Maven convention (`src/test/java` parallel to source).
