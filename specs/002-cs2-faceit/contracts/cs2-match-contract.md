# API Contract: CS2 Match Command

**Endpoint**: `!cs2 match [@username]` or `/cs2 match [@username]` (optional other player)

**Feature**: 002-cs2-faceit | **Story**: User Story 2 - View Last Match Report

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `match`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "match",
      "type": 1,
      "description": "View your most recent CS2 match statistics",
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

### Lookup Resolution (Player Precedence)
1. Discord mention → Check linked CS2Profile for guild
2. String with `faceit:` prefix → `faceit:GeorgsterCS`
3. String with `steam:` prefix → `steam:STEAM_X:Y:Z`
4. Bare string → Treat as Faceit username

### Message Context
| Attribute | Type | Source | Notes |
|-----------|------|--------|-------|
| `guildId` | Snowflake | Discord Event | Guild where command executed |
| `userId` | Snowflake | Discord Event | Executor's Discord ID |
| `targetPlayer` | String | Parsed input | Faceit player ID or username |

---

## Response Contract

### Success Response (HTTP 200 - Cached Data)

**Trigger**: `CachedMatchReport` hit OR API succeeds

**Response Body** (Discord Embed):
```
Title: "📊 Last Match - {mapName}"
Description: "{nickname} | Level {faceitLevel}"

Fields:
  - Name: "Result", Value: "{result} (W/L)", Inline: true
  - Name: "Score", Value: "{score}", Inline: true
  - Name: "K/D", Value: "{kills}/{deaths} ({kd_ratio})", Inline: true
  - Name: "Assists", Value: "{assists}", Inline: true
  - Name: "ADR", Value: "{adr:.1f}", Inline: true
  - Name: "HS%", Value: "{hs_percentage:.1f}%", Inline: true
  - Name: "MVPs", Value: "{mvps}", Inline: true
  - Name: "Rounds", Value: "{roundsPlayed}", Inline: true
  
  - Name: "vs. Your Average", Value: "", Inline: false
  - Name: "K/D Average", Value: "{your_career_kd} → {match_kd} ({diff:+.2f})", Inline: true
  - Name: "ADR Average", Value: "{your_career_adr:.1f} → {match_adr:.1f} ({diff:+.1f})", Inline: true
  - Name: "HS% Average", Value: "{your_career_hs:.1f}% → {match_hs:.1f}% ({diff:+.1f}%)", Inline: true

Color: BLUE (#3498db) if W, GRAY (#95a5a6) if L
Timestamp: {matchTimestamp}
Footer: "Fetched from Faceit API"
```

### Async Update (After API Refresh)

After cached embed displays, `CompletableFuture` fetches fresh data:
- On success: Call `message.edit()` to update embed with fresh stats
- On failure: Silently log; keep original cached embed (per FR-011)
- No notification to user of background refresh

---

### Error Response - Player Not Found (HTTP 404)

**Trigger**: PlayerLookup fails all precedence levels

**Response Body** (Discord Embed):
```
Title: "❌ Player Not Found"
Description: "Could not find player matching your input."
Fields:
  - Name: "Input", Value: "{playerInput}", Inline: false
  - Name: "Troubleshooting", Value: "1. Verify username is correct\n2. Link your account with `/cs2 link <username>`\n3. Try mentioning the Discord user", Inline: false

Color: RED (#e74c3c)
```

### Error Response - No Recent Matches (HTTP 404)

**Trigger**: Player has no match history

**Response Body** (Discord Embed):
```
Title: "📭 No Recent Matches"
Description: "{nickname} has no recent matches on record."

Fields:
  - Name: "Level", Value: "{faceitLevel}", Inline: true
  - Name: "Elo", Value: "{elo}", Inline: true
  - Name: "Status", Value: "Play a match and try again in a few minutes.", Inline: false

Color: ORANGE (#f39c12)
```

### Error Response - Account Not Linked (HTTP 403)

**Trigger**: Mentioned user has no CS2Profile linked

**Response Body** (Discord Embed):
```
Title: "⚠️ Account Not Linked"
Description: "This user hasn't linked their Faceit account yet."

Fields:
  - Name: "Action Required", Value: "Ask them to link with: `/cs2 link <username>`", Inline: false

Color: ORANGE (#f39c12)
```

### Error Response - API Unavailable (HTTP 503)

**Trigger**: Faceit API rate limited, down, or timeout

**Response Body** (Discord Embed):
```
Title: "⚠️ Service Temporarily Unavailable"
Description: "Unable to fetch match data at this time. Please try again later."

Color: ORANGE (#f39c12)
```

---

## Acceptance Criteria

1. ✅ User with linked account executes `/cs2 match` → Last match displayed with all required stats
2. ✅ User specifies another linked player with `@mention` → Their last match displayed
3. ✅ User specifies unlinked player → Error asking to link account
4. ✅ User with no recent matches → Helpful message with profile info
5. ✅ Cached match displayed immediately; fresh data updates embed asynchronously
6. ✅ API failure: User sees standardized error, not technical details

---

## Implementation Notes

- **Caching**: Key format `{guildId}:{faceitPlayerId}:match`, TTL 5 minutes
- **Async Refresh**: `CompletableFuture.supplyAsync()` → `FaceitAPIClient.fetchLastMatch()` → `message.edit()`
- **Comparison Calculation**: Fetch PlayerStats cache for career averages; calculate deltas
- **Performance**: Cached path should complete in <500ms; API path in <3s
- **Error Handling**: All exceptions caught by command handler, logged via SLF4J
- **Testing**: Mock FaceitAPIClient for cached success, API failure, and missing match scenarios

---
