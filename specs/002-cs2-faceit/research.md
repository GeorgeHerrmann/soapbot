# Phase 0 Research: CS2 Faceit Integration

**Date**: 2026-01-19 | **Status**: Complete | **Feature**: 002-cs2-faceit

## Resolved Clarifications

### 1. Testing Framework & Approach

**Decision**: Maven + JUnit5 for unit tests (existing project infrastructure)

**Rationale**: SoapBot uses Maven build tool. Adding JUnit5 and Mockito to test command classes follows Java community standards. No specialized test runner detected in project structure; standard Maven `mvn test` execution.

**Alternatives Considered**:
- TestNG: more feature-rich but adds complexity; JUnit5 sufficient for command testing
- Integration tests vs. Unit tests: Start with unit tests for command logic validation; integration tests added if Discord API mocking required

**Implementation**: Create test classes in `src/test/java/com/georgster/game/CS2CommandTest.java` following existing patterns.

---

### 2. HTTP Client Library

**Decision**: Add OkHttp3 (lightweight, connection pooling, configurable timeouts) or use existing library

**Rationale**: 
- Faceit API requires HTTPS calls with Bearer token authentication
- OkHttp3 provides connection pooling critical for resource-constrained server (1 vcore, 1GB RAM)
- Library not currently in pom.xml; Gson already present for JSON parsing
- Configuration: connection pool max 10 connections, 30s timeout, exponential backoff for rate-limiting

**Alternatives Considered**:
- HttpClient (Java built-in): requires more boilerplate; less efficient connection pooling
- Retrofit: adds abstraction layer; over-engineered for single external API

**Implementation**: Add OkHttp3 3.11.0+ to pom.xml dependencies

---

### 3. Caching Strategy

**Decision**: Caffeine Cache (lightweight, high-performance, TTL-based eviction)

**Rationale**:
- Requirement: FR-012 specifies 5-10min TTL per stat category with background refresh
- Cache entries: player stats (5min), match history (5min), leaderboard (10min)
- Caffeine: zero external dependencies, thread-safe, automatic eviction, minimal memory overhead
- Strategy: Serve cached data immediately → fetch fresh in background → update embed post-API response
- Not in current pom.xml; must add as dependency

**Alternatives Considered**:
- Redis: distributed caching overkill for single-instance bot; adds external dependency
- Guava Cache: Caffeine is Guava's successor; better performance
- No caching: violates FR-012 and risks Faceit API rate-limiting with 100+ linked users

**Implementation**: 
- Add Caffeine 3.1.2+ to pom.xml
- Create `FaceitCache` class extending `GuildedSoapManager` pattern
- Cache key format: `{guildId}:{playerId}:{statType}` to maintain guild isolation
- Background refresh via `CompletableFuture` without blocking command response

---

### 4. User Profile Extension

**Decision**: Extend `UserProfile` with CS2-specific fields (faceitPlayerId, faceitNickname, steamId, lastUpdated)

**Rationale**:
- `UserProfile` already manages guild-isolated user data (completions, bank, collecteds)
- Existing pattern: `UserProfileManager` extends `GuildedSoapManager<UserProfile>` with MongoDB persistence
- New CS2Profile can be a nested object in UserProfile or standalone; recommend nested object for cleaner access
- Prevents creating new manager; reuses existing persistence layer

**Alternatives Considered**:
- Create separate CS2Profile collection: adds complexity, breaks existing profile manager pattern
- Store in Discord member metadata: Discord doesn't provide user-defined fields; API limitation

**Implementation**:
```java
// Add to UserProfile class
private CS2Profile cs2Profile; // null if not linked

// Create inner/nested class
public static class CS2Profile {
    private String faceitPlayerId;
    private String faceitNickname;
    private String steamId;
    private long lastUpdated; // timestamp
}
```

---

### 5. Permission System Extension

**Decision**: Add `CS2COMMAND` permission to existing `PermissibleAction` enum

**Rationale**:
- FR-014: "Users MUST have CS2COMMAND permission to execute CS2-related commands"
- `PermissibleAction` enum already defines command permissions (DEFAULT, ADMIN, etc.)
- Pattern established in project: `Command.getRequiredPermission()` returns PermissibleAction

**Alternatives Considered**:
- Use existing DEFAULT permission: loses ability to selectively enable/disable feature per server
- Create new permission system: contradicts Constitution requirement to reuse existing code

**Implementation**: Add `CS2COMMAND("CS2_COMMAND")` enum value to `PermissibleAction` class

---

### 6. Faceit API Integration Pattern

**Decision**: Create `FaceitAPIClient` utility class with methods for each endpoint, return strongly-typed DTOs

**Rationale**:
- API endpoints documented: GET `/players/{player_id}`, `/players/{player_id}/stats`, `/players/{player_id}/matches`
- Handle 404 (player not found), 429 (rate limited), 500 (server error) with standardized error responses
- DTOs: `FaceitPlayer`, `PlayerStats`, `MatchDetails` with Gson deserialization
- API key stored securely in config file (never committed), injected via environment variable

**Alternatives Considered**:
- Inline API calls in command classes: violates modularity; hard to test
- Retrofit/REST client generation: overhead for single API; manual client cleaner

**Implementation**:
- Create `com.georgster.api.faceit.FaceitAPIClient` 
- Create DTOs in `com.georgster.api.faceit.model.*`
- Configuration: `FACEIT_API_KEY` environment variable, with fallback to `faceit_api_key.txt` file
- Error handling per FR-011: catch exceptions, return standardized "Service temporarily unavailable" message

---

### 7. Player Lookup Resolution (Multiple Identifier Formats)

**Decision**: Implement lookup precedence chain: (1) Discord mention → linked account, (2) faceit:username prefix, (3) steam:STEAMID prefix, (4) bare text as Faceit username

**Rationale**:
- FR-005 specifies exact precedence order
- Pattern reuses existing `MemberIdentified` base class for Discord mention parsing
- Faceit and Steam ID lookups fallback gracefully if API fails

**Alternatives Considered**:
- Simple username only: loses flexibility for server members without Discord links
- No precedence: ambiguous behavior for bare text; user confusion

**Implementation**:
- Create `PlayerLookup` utility class with method `resolveFaceitPlayer(String reference, UserProfileManager manager)`
- Return `FaceitPlayer` or throw `PlayerNotFoundException` with clear user message
- Mock Faceit API in tests to validate precedence order

---

### 8. Discord Embed Formatting

**Decision**: Use Discord4J's `EmbedCreateSpec` builder for rich formatting; consistent color scheme (blue/green for stats, red for errors)

**Rationale**:
- FR-004, FR-008 require formatted embeds with multiple stat fields
- `EmbedCreateSpec` standard Discord4J pattern for embeds
- Color convention: match in-game Faceit UI (blue primary, green positive stats)
- Field layout: vertical stats display with inline formatting for comparisons

**Alternatives Considered**:
- Plain text responses: violates user expectation for stats bot; less readable
- Custom Discord formatting (markdown): limited for complex stat tables

**Implementation**:
- Create `CS2EmbedFormatter` utility class with methods `formatMatchReport()`, `formatPlayerStats()`, etc.
- Reuse Discord4J's color constants and field builders
- Test embed rendering via screenshot comparison (optional; manual verification acceptable)

---

### 9. Background Refresh Strategy (Cache + Async Updates)

**Decision**: On command execution, return cached data immediately → fetch fresh async → update embed via `message.edit()` once API completes

**Rationale**:
- FR-012 specifies immediate cache serving + silent background refresh + embed update on completion
- Improves perceived performance: user sees stats instantly vs. waiting 3s for API
- Async prevents blocking command executor thread
- `CompletableFuture` pattern in Java handles non-blocking refresh

**Alternatives Considered**:
- Always fetch fresh data: blocks on API, violates <3s performance goal
- Cache only, no refresh: stale data; doesn't meet FR-012

**Implementation**:
- In command execute: check cache → return cached embed → `CompletableFuture.supplyAsync()` to fetch fresh
- On API success: call `MessageCreateEvent.message.edit()` to update original embed
- On API failure: silently log error; keep original cached data (per FR-011 "no cached data exposure" for failures)

---

### 10. Server Leaderboard Performance

**Decision**: Leaderboard generated fresh from all linked UserProfiles filtered in memory, cached for 10min, scheduled refresh every 10min

**Rationale**:
- FR-007: "leaderboard updates every 10 minutes"
- Can't rely on external leaderboard API (Faceit doesn't provide); must aggregate from linked users
- Caffeine cache stores leaderboard data; scheduled task via `ScheduledExecutorService` refreshes
- Memory footprint: 1 leaderboard entry per linked user; ~100 max per server = minimal

**Alternatives Considered**:
- Real-time leaderboard per command: requires 100+ API calls per execution; rate-limiting risk
- Database-backed leaderboard: adds schema complexity; not needed for 10min refresh granularity

**Implementation**:
- Create `LeaderboardManager` extending `GuildedSoapManager` to manage per-guild leaderboards
- Cache key: `{guildId}:leaderboard`; TTL 10min with scheduled refresh
- Refresh task: iterate all UserProfiles, sort by Faceit Elo, store top 10

---

## Summary of Dependency Changes

### New pom.xml Dependencies Required:
1. **com.squareup.okhttp3:okhttp:3.11.0** - HTTP client with connection pooling
2. **com.github.ben-manes.caffeine:caffeine:3.1.2** - In-memory cache with TTL
3. **junit:junit-jupiter:5.9.2** (test) - Unit testing framework
4. **org.mockito:mockito-core:5.2.0** (test) - Mocking for API tests

### Existing Dependencies Leveraged:
- `discord4j-core:3.2.4` - Discord API wrapper
- `gson:2.11.0` - JSON parsing for Faceit API responses
- `mongodb-driver-sync:4.9.0` - Existing persistence layer
- `slf4j-*:2.0.5` - Logging

---

## Design Decisions Summary

| Area | Decision | Constraint/Note |
|------|----------|----------------|
| HTTP Client | OkHttp3 | 10-conn pool, 30s timeout |
| Cache Library | Caffeine | 5-10min TTL, guild-isolated keys |
| Profile Storage | Extend UserProfile | Nested CS2Profile object |
| Permission | Add CS2COMMAND enum value | Reuses PermissibleAction pattern |
| API Client | FaceitAPIClient utility | Environment variable for API key |
| Player Lookup | Precedence chain (mention → faceit: → steam: → bare) | Matches FR-005 spec |
| Embeds | Discord4J EmbedCreateSpec | Blue/green color scheme |
| Cache Strategy | Immediate serve + async background refresh | Meets <3s performance goal |
| Leaderboard | Scheduled refresh every 10min | Caffeine cache per guild |
| Error Handling | Standardized messages (FR-011) | No technical details to users |

All NEEDS CLARIFICATION items from Technical Context resolved. Ready for Phase 1 design.
