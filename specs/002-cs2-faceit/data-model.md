# Data Model: CS2 Faceit Integration

**Date**: 2026-01-19 | **Feature**: 002-cs2-faceit | **Status**: Phase 1 Design

## Entity Relationship Diagram

```
UserProfile (existing)
├── CS2Profile (NEW - nested object)
│   ├── faceitPlayerId: String (unique per guild)
│   ├── faceitNickname: String
│   ├── steamId: String (optional, for lookups)
│   └── lastUpdated: long (timestamp)
│
CachedPlayerStats (NEW - transient, in-memory via Caffeine)
├── playerId: String (Faceit player ID)
├── guildId: String (guild context)
├── stats: PlayerStats
├── fetchedAt: long (timestamp for cache expiry)
│
CachedMatchReport (NEW - transient, in-memory via Caffeine)
├── playerId: String
├── guildId: String
├── match: MatchDetails
├── fetchedAt: long
│
ServerLeaderboard (NEW - transient, in-memory via Caffeine)
├── guildId: String (unique per guild)
├── entries: List<LeaderboardEntry>
├── generatedAt: long (timestamp)
├── refreshedAt: long (last background refresh)
```

---

## Detailed Entity Definitions

### 1. CS2Profile (Extension to UserProfile)

**Purpose**: Store Faceit account linkage per Discord user, per guild

**Fields**:
```java
public static class CS2Profile {
    private String faceitPlayerId;      // Faceit unique ID (from API /players endpoint)
    private String faceitNickname;      // Display name (e.g., "GeorgsterCS")
    private String steamId;             // Optional Steam ID for alternative lookups
    private long lastUpdated;           // Timestamp when profile was last synced with Faceit API
    private boolean isLinked;           // Flag: true if successfully linked and validated
}
```

**Location**: Nested within `com.georgster.profile.UserProfile`

**Relationships**:
- 1:1 with UserProfile (each Discord user can link one Faceit account per guild)
- References FaceitPlayer entity during linking validation

**Validation Rules**:
- `faceitPlayerId`: REQUIRED, not null, non-empty string
- `faceitNickname`: REQUIRED for display, not null, 3-32 chars (Faceit username format)
- `steamId`: OPTIONAL, format `STEAM_X:Y:Z` or 76561198XXXXXXXXX
- `lastUpdated`: Auto-set to current timestamp on link/unlink/update
- `isLinked`: Must be true to execute commands; false on unlink

**State Transitions**:
```
[NOT_LINKED] --link()--> [LINKED] --validate()--> [LINKED_VERIFIED] --unlink()--> [NOT_LINKED]
                            ↓ (if API fails)
                        [LINK_FAILED] --retry()--> [LINKED]
```

---

### 2. FaceitPlayer (DTO from API)

**Purpose**: Represent Faceit player profile fetched from Faceit API

**Fields**:
```java
public class FaceitPlayer {
    private String playerId;            // Faceit unique identifier
    private String nickname;            // Username on Faceit
    private String avatar;              // Avatar URL
    private String country;             // Country code (ISO)
    private int faceitLevel;            // 1-10 skill level
    private int elo;                    // Elo rating (e.g., 1000-3000)
    private String steamId;             // Linked Steam ID
}
```

**Source**: Deserialized from Faceit API `GET /players/{player_id}`

**Relationships**:
- 1:many with MatchDetails (one player has many matches)
- 1:1 with PlayerStats (one player has lifetime stats)

**Validation Rules**:
- `playerId`: REQUIRED, matches Faceit API format
- `nickname`: REQUIRED for link validation
- `faceitLevel`: REQUIRED, range [1-10]
- `elo`: REQUIRED, positive integer

---

### 3. PlayerStats (Lifetime Statistics)

**Purpose**: Aggregate CS2 statistics for a player across all career matches

**Fields**:
```java
public class PlayerStats {
    private String playerId;            // Faceit player ID (foreign key)
    private int totalMatches;           // Total career matches
    private int wins;                   // Career wins
    private int losses;                 // Career losses
    private double winRate;             // wins / (wins + losses)
    private double killDeathRatio;      // K/D ratio (kills / deaths)
    private double averageDamageRound;  // ADR (total damage / rounds played)
    private double headshotPercentage;  // HS% (headshots / kills)
    private double mVPsPerMatch;        // MVP count / total matches
    private List<MapStats> topMaps;     // Best 3 maps with individual stats
    private List<Integer> recentForm;   // Last 5 match results (1=W, 0=L)
}

public class MapStats {
    private String mapName;             // e.g., "de_mirage"
    private int mapMatches;             // Matches played on this map
    private double mapWinRate;          // Win% on this specific map
    private double mapKD;               // K/D on this map
    private double mapADR;              // ADR on this map
}
```

**Source**: Deserialized from Faceit API `GET /players/{player_id}/stats`

**Relationships**:
- 1:1 with FaceitPlayer (aggregates all MatchDetails for that player)

**Validation Rules**:
- `totalMatches`: Non-negative integer
- `winRate`: Range [0.0 - 1.0] or derived from wins/losses
- `killDeathRatio`: Non-negative decimal (0.0+ allowed if deaths = 0)
- `headshotPercentage`: Range [0.0 - 1.0]
- `topMaps`: Size = up to 3 entries, sorted by win rate descending
- `recentForm`: Size = exactly 5 (or less if player has fewer than 5 matches)

---

### 4. MatchDetails (Individual Match Record)

**Purpose**: Store detailed statistics from a single CS2 match

**Fields**:
```java
public class MatchDetails {
    private String matchId;             // Faceit match unique ID
    private String playerId;            // Faceit player ID (foreign key)
    private String mapName;             // Map played (e.g., "de_inferno")
    private int score;                  // Team score (e.g., "16-14")
    private int kills;                  // Player kills in match
    private int deaths;                 // Player deaths in match
    private int assists;                // Player assists in match
    private double averageDamageRound;  // ADR for this match
    private double headshotPercentage;  // HS% for this match
    private int mvps;                   // MVP count earned in match
    private String result;              // "W" (win) or "L" (loss)
    private long matchTimestamp;        // Unix timestamp when match played
    private List<String> teamRoster;    // List of teammate Faceit usernames
    private int roundsPlayed;           // Total rounds in match
}
```

**Source**: Deserialized from Faceit API `GET /players/{player_id}/matches` and match detail endpoints

**Relationships**:
- Many:1 with FaceitPlayer (each match references one player)
- Aggregated into PlayerStats and ServerLeaderboard

**Validation Rules**:
- `matchId`: REQUIRED, unique per player
- `kills`, `deaths`, `assists`: Non-negative integers
- `averageDamageRound`: Non-negative decimal
- `headshotPercentage`: Range [0.0 - 1.0]
- `result`: Must be "W" or "L"
- `matchTimestamp`: Valid Unix timestamp, not in future
- `roundsPlayed`: Positive integer (typically 16-30 for best-of-30 matches)

---

### 4a. FullMatchDetails (Complete Match with All Players)

**Purpose**: Store comprehensive statistics for an entire match including all players from both teams

**Fields**:
```java
public class FullMatchDetails {
    private String matchId;             // Faceit match unique ID
    private String mapName;             // Map played (e.g., "de_mirage")
    private String matchResult;         // "W" or "L" from perspective of team1
    private String finalScore;          // e.g., "16-14"
    private long matchTimestamp;        // Unix timestamp when match played
    private TeamRoster team1;           // Winning team roster
    private TeamRoster team2;           // Losing team roster
}

public class TeamRoster {
    private String teamName;            // Team name (e.g., "Team A")
    private List<PlayerMatchPerformance> players; // All players on this team
}

public class PlayerMatchPerformance {
    private String nickname;            // Faceit username
    private int kills;                  // Kills in match
    private int deaths;                 // Deaths in match
    private int assists;                // Assists in match
    private double adr;                 // Average damage per round
    private double headshotPercentage;  // Headshot percentage
}
```

**Source**: Deserialized from Faceit API `GET /matches/{match_id}` and `GET /matches/{match_id}/stats`

**Relationships**:
- 1:1 with MatchDetails (via matchId)
- Used by CS2MatchWizard for full match view

**Validation Rules**:
- `matchId`: REQUIRED, matches existing MatchDetails.matchId
- `team1`, `team2`: Each must have 1-5 players
- `finalScore`: Format "XX-YY" where XX and YY are integers 0-30
- `matchResult`: Must be "W" or "L"
- MVP identification: Player with highest ADR across both teams

---

### 5. ServerLeaderboard (Aggregated Ranking)

**Purpose**: Cache top-10 linked players per server, ranked by Faceit Elo

**Fields**:
```java
public class ServerLeaderboard {
    private String guildId;             // Discord guild ID (primary key)
    private List<LeaderboardEntry> entries; // Top 10 players
    private double serverAverageElo;    // Mean Elo of all linked players
    private long generatedAt;           // Timestamp when leaderboard was created
    private long refreshedAt;           // Timestamp of last background refresh
}

public class LeaderboardEntry {
    private int rank;                   // 1-10 rank position
    private String faceitNickname;      // Faceit username
    private int faceitLevel;            // Faceit level (1-10)
    private int elo;                    // Elo rating
    private double killDeathRatio;      // Current K/D ratio
    private String discordMentionId;    // Discord user mention (optional, for tagging)
}
```

**Storage**: In-memory cache via Caffeine, 10-minute TTL

**Relationships**:
- Many:1 with Guild (each guild has one leaderboard)
- 1:many with UserProfile (aggregates all linked CS2Profiles)

**Validation Rules**:
- `guildId`: REQUIRED, valid Discord Snowflake
- `entries`: Size = 0-10 (10 or fewer linked players)
- `rank`: Values 1-10, sorted descending by Elo
- `serverAverageElo`: Calculated mean, >= 0
- `generatedAt`, `refreshedAt`: Valid timestamps, `generatedAt` <= `refreshedAt`

**Refresh Policy**:
- Initial generation: On first `!cs2 leaderboard` command
- Background refresh: Scheduled task every 10 minutes per guild
- Eviction: Caffeine auto-evicts after 10-minute TTL
- Manual refresh: Triggered by link/unlink commands affecting guild roster

---

### 6. Cache Entries (Transient, In-Memory)

**CachedPlayerStats** (Caffeine Cache):
```
Key: "{guildId}:{playerId}:stats"
Value: PlayerStats + fetchedAt timestamp
TTL: 5 minutes
```

**CachedMatchReport** (Caffeine Cache):
```
Key: "{guildId}:{playerId}:match"
Value: MatchDetails + fetchedAt timestamp
TTL: 5 minutes
```

**CachedFullMatchDetails** (Caffeine Cache):
```
Key: "{guildId}:{matchId}:fullmatch"
Value: FullMatchDetails + fetchedAt timestamp
TTL: 10 minutes
```

**CachedMatchHistory** (Caffeine Cache):
```
Key: "{guildId}:{playerId}:history"
Value: List<MatchDetails> (last 10) + fetchedAt timestamp
TTL: 5 minutes
```

**CachedLeaderboard** (Caffeine Cache):
```
Key: "{guildId}:leaderboard"
Value: ServerLeaderboard + refreshedAt timestamp
TTL: 10 minutes
```

---

## Data Flow Diagrams

### Account Linking Flow
```
User executes: !cs2 link GeorgsterCS
    ↓
PlayerLookup.resolveFaceitPlayer("GeorgsterCS")
    ↓
FaceitAPIClient.fetchPlayer("GeorgsterCS")
    ↓ API Response: FaceitPlayer
Create/Update UserProfile.CS2Profile
    ↓
Persist to MongoDB via UserProfileManager.update()
    ↓
Return confirmation embed with FaceitPlayer details (nickname, Elo, level)
```

### Match Report Flow
```
User executes: !cs2 match
    ↓
Check cache: CachedMatchReport[{guildId}:{playerId}:match]
    ↓
Found? Return cached MatchDetails → Show embed immediately
Not found? Continue...
    ↓
FaceitAPIClient.fetchLastMatch(faceitPlayerId)
    ↓ API Response: MatchDetails
Store in CachedMatchReport with 5-min TTL
    ↓
Background: Fetch fresh match async (CompletableFuture)
    ↓
CompletableFuture completes → message.edit() to update embed with fresh data
```

### Leaderboard Refresh Flow
```
Scheduled task fires every 10 minutes per guild:
    ↓
Check cache: CachedLeaderboard[{guildId}:leaderboard]
    ↓
If cache hit AND still valid:
    └─ Skip refresh (TTL not expired)
    ↓
If cache miss OR TTL expired:
    ↓
Iterate UserProfileManager.getAll() for guild
    ↓
For each UserProfile with CS2Profile:
    ├─ Check CachedPlayerStats[{guildId}:{playerId}:stats]
    ├─ If hit: Use cached stats
    └─ If miss: FaceitAPIClient.fetchStats() → cache it
    ↓
Sort all players by Elo descending
    ↓
Take top 10 → Create LeaderboardEntry list
    ↓
Calculate serverAverageElo from all linked players
    ↓
Create ServerLeaderboard object with refreshedAt = now
    ↓
Store in CachedLeaderboard with 10-min TTL
```

---

## Storage Implementation Notes

### MongoDB Collections
No new collections required. CS2Profile is nested within existing `profiles` collection:
```json
{
  "_id": "memberId",
  "guildId": "guildId",
  "username": "discordUsername",
  "completions": { ... },
  "bank": { ... },
  "collecteds": [ ... ],
  "factory": { ... },
  "cs2Profile": {
    "faceitPlayerId": "faceitId",
    "faceitNickname": "GeorgsterCS",
    "steamId": "STEAM_X:Y:Z",
    "lastUpdated": 1676800000000,
    "isLinked": true
  }
}
```

### In-Memory Cache Management
- **Library**: Caffeine 3.1.2
- **Max Size**: 10,000 total cache entries (across all users in all guilds)
- **Eviction**: LRU (Least Recently Used) + TTL (Time To Live)
- **Memory Estimate**: ~100 bytes per cached entry = ~1MB total worst-case

---

## Validation Rules Summary

| Entity | Field | Rule | Notes |
|--------|-------|------|-------|
| CS2Profile | faceitPlayerId | NOT NULL, non-empty | Primary identifier |
| CS2Profile | faceitNickname | 3-32 chars | Faceit username format |
| CS2Profile | isLinked | TRUE/FALSE | Linking gate |
| FaceitPlayer | faceitLevel | 1-10 | Validated from API |
| PlayerStats | winRate | 0.0 - 1.0 | Derived or validated |
| MatchDetails | result | "W" or "L" | Case-insensitive |
| LeaderboardEntry | rank | 1-10 | Sorted by Elo |
| ServerLeaderboard | entries | 0-10 items | May be empty |

---

## Entity Status for Phase 1

- ✅ **CS2Profile**: Ready for implementation (nested in UserProfile)
- ✅ **FaceitPlayer**: Ready (DTO, from API)
- ✅ **PlayerStats**: Ready (DTO, from API)
- ✅ **MatchDetails**: Ready (DTO, from API)
- ✅ **ServerLeaderboard**: Ready (in-memory cache, scheduled generation)
- ✅ **Cache Entries**: Ready (Caffeine library, TTL configuration)

All entities defined and validated. Proceeding to API contracts.
