# API Contract: CS2 History Command

**Endpoint**: `!cs2 history [@username]` or `/cs2 history [@username]`

**Feature**: 002-cs2-faceit | **Story**: User Story 6 - View Recent Match History

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `history`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "history",
      "type": 1,
      "description": "View last 10 matches and recent form",
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
| `player` | String/Mention | NO | Discord mention OR Faceit username | `@Player` | Defaults to command executor |

### Lookup Resolution
(Same precedence as CS2 Stats Command)

---

## Response Contract

### Success Response (HTTP 200)

**Trigger**: Player found with match history available

**Response Body** (Discord Embed):
```
Title: "📋 Match History - {nickname}"
Subtitle: "Last 10 Matches"

Fields:
  [MATCHES TABLE]
  - Name: "#1", Value: "{map} | {result} | K/D: {kills}/{deaths} ({kd:.2f}) | ADR: {adr:.1f} | {timestamp}", Inline: false
  - Name: "#2", Value: "{map} | {result} | K/D: {kills}/{deaths} ({kd:.2f}) | ADR: {adr:.1f} | {timestamp}", Inline: false
  ... (up to 10 matches)
  
  [RECENT STATISTICS SUMMARY]
  - Name: "Last 10 Matches Form", Value: "🟢 🟢 🔴 🟢 🔴 🟢 🟢 🟢 🔴 🟢", Inline: false
  - Name: "Record", Value: "7W - 3L (70% win rate)", Inline: true
  - Name: "Average K/D", Value: "1.45", Inline: true
  - Name: "Average ADR", Value: "78.3", Inline: true

Color: BLUE (#3498db)
Footer: "Fetched from Faceit API"
```

### Success Response - Fewer Than 10 Matches (HTTP 200)

**Trigger**: Player has <10 matches total

**Response Body** (Discord Embed):
```
[Same format as above, but with {n} matches instead of 10]

Example with 5 matches:
  - Name: "#1", Value: "...", Inline: false
  - Name: "#2", Value: "...", Inline: false
  - Name: "#3", Value: "...", Inline: false
  - Name: "#4", Value: "...", Inline: false
  - Name: "#5", Value: "...", Inline: false
  
  [RECENT STATISTICS SUMMARY]
  - Name: "Record", Value: "3W - 2L (60% win rate)", Inline: false
```

### Error Response - No Match History (HTTP 404)

**Trigger**: Player has 0 recorded matches

**Response Body** (Discord Embed):
```
Title: "📭 No Match History"
Description: "{nickname} has no recorded matches yet."

Fields:
  - Name: "Level", Value: "{faceitLevel}", Inline: true
  - Name: "Elo", Value: "{elo}", Inline: true
  - Name: "Status", Value: "Play some matches and check back!", Inline: false

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

---

## Acceptance Criteria

1. ✅ User executes `/cs2 history` → Last 10 matches displayed with map, result, K/D, ADR, timestamp
2. ✅ Recent statistics summary shows win rate, average K/D, average ADR for last 10 matches
3. ✅ Recent form shown as emoji indicators (W=🟢, L=🔴)
4. ✅ User with <10 matches → All available matches displayed
5. ✅ User with 0 matches → Helpful message with profile info
6. ✅ Another player's history viewable via mention or Faceit username
7. ✅ API failure: Standardized error message

---

## Implementation Notes

- **Caching**: Key format `{guildId}:{faceitPlayerId}:history`, TTL 5 minutes
- **Data Fetch**: `FaceitAPIClient.fetchMatches(faceitPlayerId, limit=10)` returns last 10 MatchDetails
- **Statistics Calculation**: Iterate returned matches; calculate mean K/D and ADR; extract win/loss results
- **Timestamp Formatting**: Convert Unix timestamp to human-readable format (e.g., "2 days ago")
- **Map Abbreviation**: Display full map name (e.g., "de_mirage", not "MIRAGE")
- **Sort Order**: Display most recent match first (#1 = last match played)
- **Performance**: Cached path <500ms; API path <3s
- **Async Refresh**: Same background refresh pattern as Match and Stats commands

---
