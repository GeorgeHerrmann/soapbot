# API Contract: CS2 Leaderboard Command

**Endpoint**: `!cs2 leaderboard` or `/cs2 leaderboard`

**Feature**: 002-cs2-faceit | **Story**: User Story 5 - View Server Leaderboard by Faceit Elo

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `leaderboard`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "leaderboard",
      "type": 1,
      "description": "View top 10 linked players by Faceit Elo in this server",
      "options": []
    }
  ]
}
```

---

## Request Contract

### Message Context (No User Input)
| Attribute | Type | Source | Notes |
|-----------|------|--------|-------|
| `guildId` | Snowflake | Discord Event | Guild where command executed |
| `userId` | Snowflake | Discord Event | Executor's ID (unused for leaderboard) |

---

## Response Contract

### Success Response (HTTP 200 - Cached Data)

**Trigger**: `CachedLeaderboard` hit OR leaderboard successfully generated from linked users

**Response Body** (Discord Embed):
```
Title: "🏆 Server Leaderboard - CS2 Elo Rankings"
Description: "Top 10 linked players in this server"

Fields:
  [LEADERBOARD]
  - Name: "#1 🥇 {nickname1}", Value: "Elo: {elo1} | Level: {level1} | K/D: {kd1:.2f}", Inline: false
  - Name: "#2 🥈 {nickname2}", Value: "Elo: {elo2} | Level: {level2} | K/D: {kd2:.2f}", Inline: false
  - Name: "#3 🥉 {nickname3}", Value: "Elo: {elo3} | Level: {level3} | K/D: {kd3:.2f}", Inline: false
  - Name: "#4 {nickname4}", Value: "Elo: {elo4} | Level: {level4} | K/D: {kd4:.2f}", Inline: false
  ... (up to #10)
  
  [SERVER STATISTICS]
  - Name: "Total Linked Players", Value: "{totalLinkedCount}", Inline: true
  - Name: "Server Average Elo", Value: "{serverAverageElo:.0f}", Inline: true
  
  [YOUR RANK]
  - Name: "Your Rank", Value: "#{yourRank} / {totalLinkedCount} (Elo: {yourElo})", Inline: false
    (Only if executor is in top 10; otherwise omit field)

Color: BLUE (#3498db)
Timestamp: {generatedAt}
Footer: "Updated every 10 minutes | Last refresh: {refreshedAt}"
```

### Success Response - No Linked Players (HTTP 200)

**Trigger**: Guild has zero users with CS2Profile linked

**Response Body** (Discord Embed):
```
Title: "📭 No Linked Players"
Description: "No one in this server has linked their Faceit account yet."

Fields:
  - Name: "Get Started", Value: "Link your account with `/cs2 link <username>` to appear on the leaderboard!", Inline: false

Color: ORANGE (#f39c12)
```

### Success Response - Less Than 10 Linked Players (HTTP 200)

**Trigger**: Guild has 1-9 linked users

**Response Body** (Discord Embed):
```
[Same format as main response, but with only {n} entries instead of 10]

Example with 3 linked players:
  - Name: "#1 🥇 Player1", Value: "...", Inline: false
  - Name: "#2 🥈 Player2", Value: "...", Inline: false
  - Name: "#3 🥉 Player3", Value: "...", Inline: false
  
  [SERVER STATISTICS]
  - Name: "Total Linked Players", Value: "3", Inline: true
  - Name: "Server Average Elo", Value: "1200", Inline: true
```

### Error Response - API Failure During Refresh (HTTP 503)

**Trigger**: Leaderboard generation requested, but Faceit API fails for multiple players

**Response Body** (Discord Embed):
```
Title: "⚠️ Leaderboard Temporarily Unavailable"
Description: "Unable to fetch player data at this time."

Fields:
  - Name: "Cached Data", Value: "Last valid leaderboard is from {lastValidTime}, with {cachedPlayerCount} players.", Inline: false

Color: ORANGE (#f39c12)

[Display cached leaderboard if TTL not expired (showing age)]
```

---

## Acceptance Criteria

1. ✅ Multiple linked users in guild → Top 10 displayed, ranked by Elo descending
2. ✅ Leaderboard includes rank number, nickname, Elo, level, and K/D for each player
3. ✅ Server average Elo calculated and displayed
4. ✅ Executor's rank shown if in top 10
5. ✅ Less than 10 linked players → All players displayed with proper counts
6. ✅ Zero linked players → Helpful message encouraging linking
7. ✅ Leaderboard updates every 10 minutes via background scheduled task
8. ✅ Cached leaderboard displayed immediately; background refresh updates without user notification

---

## Implementation Notes

- **Caching**: Key format `{guildId}:leaderboard`, TTL 10 minutes
- **Data Source**: Iterate `UserProfileManager.getAll()` for guild, filter by CS2Profile linked users
- **Refresh Strategy**: Scheduled task via `ScheduledExecutorService` fires every 10 minutes per guild
- **Stats Fetch**: For each linked user, check `CachedPlayerStats` (5-min TTL); fetch via Faceit API if cache miss
- **Sorting**: Sort all users by Elo descending; take top 10 for display
- **Server Average**: Calculate mean Elo from ALL linked users (not just top 10)
- **Guild Isolation**: Leaderboard unique per guild; no cross-guild data leakage
- **Performance**: Background refresh should complete in <5 seconds; doesn't block command response
- **Error Handling**: If API fails for some users, mark as "stale" and keep cached data; notify user via footer timestamp

---
