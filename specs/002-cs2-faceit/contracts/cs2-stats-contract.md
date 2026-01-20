# API Contract: CS2 Stats Command

**Endpoint**: `!cs2 stats [@username]` or `/cs2 stats [@username]` (optional other player)

**Feature**: 002-cs2-faceit | **Story**: User Story 3 - View Comprehensive Player Statistics

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `stats`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "stats",
      "type": 1,
      "description": "View comprehensive CS2 statistics",
      "options": [
        {
          "name": "player",
          "type": 9,
          "description": "Discord user or Faceit username (optional)",
          "required": false
        }
      ]
    }
  ]
}
```

---

## Request Contract

### Input Parameters
| Parameter | Type | Required | Format | Example | Notes |
|-----------|------|----------|--------|---------|-------|
| `player` | String/Mention | NO | Discord mention OR Faceit username | `@GeorgsterCS` or `GeorgsterCS` | Defaults to command executor |

### Lookup Resolution
(Same precedence as CS2 Match Command)

---

## Response Contract

### Success Response (HTTP 200 - Cached Data)

**Trigger**: `CachedPlayerStats` hit OR API succeeds

**Response Body** (Discord Embed):
```
Title: "📈 CS2 Statistics - {nickname}"
Subtitle: "Faceit Level {faceitLevel} | ELO: {elo}"

Fields:
  [CAREER OVERVIEW]
  - Name: "Total Matches", Value: "{totalMatches}", Inline: true
  - Name: "Wins / Losses", Value: "{wins} / {losses}", Inline: true
  - Name: "Win Rate", Value: "{winRate:.1f}%", Inline: true
  
  [COMBAT STATS]
  - Name: "K/D Ratio", Value: "{killDeathRatio:.2f}", Inline: true
  - Name: "ADR", Value: "{adr:.1f}", Inline: true
  - Name: "HS%", Value: "{headshotPercentage:.1f}%", Inline: true
  - Name: "MVPs/Match", Value: "{mvpsPerMatch:.2f}", Inline: true
  
  [BEST MAPS]
  - Name: "{topMaps[0].mapName}", Value: "Win Rate: {topMaps[0].winRate:.1f}% | K/D: {topMaps[0].kd:.2f} | ADR: {topMaps[0].adr:.1f}", Inline: false
  - Name: "{topMaps[1].mapName}", Value: "Win Rate: {topMaps[1].winRate:.1f}% | K/D: {topMaps[1].kd:.2f} | ADR: {topMaps[1].adr:.1f}", Inline: false
  - Name: "{topMaps[2].mapName}", Value: "Win Rate: {topMaps[2].winRate:.1f}% | K/D: {topMaps[2].kd:.2f} | ADR: {topMaps[2].adr:.1f}", Inline: false
  
  [RECENT FORM]
  - Name: "Last 5 Matches", Value: "{emoji_W}{emoji_W}{emoji_L}{emoji_W}{emoji_L}", Inline: false
    (W=🟢 green circle, L=🔴 red circle)

Color: BLUE (#3498db)
Timestamp: {lastUpdated}
Footer: "Fetched from Faceit API | Updated every 5 minutes"
```

### Success Response - Insufficient Data (HTTP 200)

**Trigger**: Player exists but has <5 matches

**Response Body** (Discord Embed):
```
Title: "📭 Limited Match History"
Subtitle: "{nickname} | Level {faceitLevel}"

Fields:
  - Name: "Matches Played", Value: "{totalMatches}", Inline: true
  - Name: "Elo", Value: "{elo}", Inline: true
  
  [AVAILABLE STATS]
  - Name: "Win Rate", Value: "{winRate:.1f}% ({wins}W / {losses}L)", Inline: false
  - Name: "K/D Ratio", Value: "{killDeathRatio:.2f}", Inline: true
  - Name: "ADR", Value: "{adr:.1f}", Inline: true
  
  - Name: "Note", Value: "Statistics improve with more matches. Play some games and check back!", Inline: false

Color: ORANGE (#f39c12)
```

### Error Response - Player Not Found (HTTP 404)

**Trigger**: PlayerLookup fails all precedence levels

**Response Body** (Discord Embed):
```
Title: "❌ Player Not Found"
Description: "Could not find player matching your input."

Color: RED (#e74c3c)
```

### Error Response - Account Not Linked (HTTP 403)

**Trigger**: Mentioned user has no CS2Profile linked

**Response Body** (Discord Embed):
```
Title: "⚠️ Account Not Linked"
Description: "This user hasn't linked their Faceit account yet."

Color: ORANGE (#f39c12)
```

### Error Response - API Unavailable (HTTP 503)

**Trigger**: Faceit API rate limited, down, or timeout

**Response Body** (Discord Embed):
```
Title: "⚠️ Service Temporarily Unavailable"
Description: "Unable to fetch stats at this time. Please try again later."

Color: ORANGE (#f39c12)
```

---

## Acceptance Criteria

1. ✅ User with linked account executes `/cs2 stats` → Comprehensive stats displayed with all required metrics
2. ✅ Stats include top 3 maps with individual win rates and K/D per map
3. ✅ Stats include recent form (last 5 match results as W/L indicators)
4. ✅ User with <5 matches → Helpful "insufficient data" message with available stats
5. ✅ Another player's stats viewable via mention or Faceit username
6. ✅ Cached stats displayed immediately; fresh data updates embed asynchronously
7. ✅ API failure: User sees standardized error

---

## Implementation Notes

- **Caching**: Key format `{guildId}:{faceitPlayerId}:stats`, TTL 5 minutes
- **Data Fetch**: `FaceitAPIClient.fetchStats(faceitPlayerId)` + `FaceitAPIClient.fetchMatches()` (last 10 to extract top maps)
- **Top Maps Calculation**: Aggregate last 10 matches by map, calculate win rate and K/D per map, sort by win rate descending
- **Recent Form**: Extract last 5 match results from match history; display as emoji indicators
- **Performance**: Cached path <500ms; API path <3s (may require 2 API calls)
- **Async Refresh**: Same pattern as Match command
- **Null Handling**: If playerStats unavailable but playerProfile exists, display profile-only info with "insufficient data" message

---
