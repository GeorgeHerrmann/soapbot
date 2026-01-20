# API Contract: CS2 Team Command

**Endpoint**: `!cs2 team <team-name>` or `/cs2 team <team-name>`

**Feature**: 002-cs2-faceit | **Story**: User Story 7 - View Team Match Statistics

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `team`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "team",
      "type": 1,
      "description": "View team's last match statistics",
      "options": [
        {
          "name": "team_name",
          "type": 3,
          "description": "Team name (e.g., Astralis, FaZe)",
          "required": true,
          "min_length": 1,
          "max_length": 64
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
| `team_name` | String | YES | Team name (case-insensitive) | `Astralis` | Faceit team identifier |

---

## Response Contract

### Success Response (HTTP 200)

**Trigger**: Faceit API returns valid team profile and recent match

**Response Body** (Discord Embed):
```
Title: "🏆 {teamName} - Last Match"
Subtitle: "{teamResult} | {mapName}"

Fields:
  [MATCH DETAILS]
  - Name: "Score", Value: "{teamScore}", Inline: true
  - Name: "Rounds", Value: "{roundsPlayed}", Inline: true
  
  [ROSTER PERFORMANCE]
  - Name: "Player 1: {player1Name}", Value: "K/D: {k1}/{d1} ({kd1:.2f}) | ADR: {adr1:.1f} | MVP: {'✓' if mvp1}", Inline: false
  - Name: "Player 2: {player2Name}", Value: "K/D: {k2}/{d2} ({kd2:.2f}) | ADR: {adr2:.1f} | MVP: {'✓' if mvp2}", Inline: false
  - Name: "Player 3: {player3Name}", Value: "K/D: {k3}/{d3} ({kd3:.2f}) | ADR: {adr3:.1f} | MVP: {'✓' if mvp3}", Inline: false
  - Name: "Player 4: {player4Name}", Value: "K/D: {k4}/{d4} ({kd4:.2f}) | ADR: {adr4:.1f} | MVP: {'✓' if mvp4}", Inline: false
  - Name: "Player 5: {player5Name}", Value: "K/D: {k5}/{d5} ({kd5:.2f}) | ADR: {adr5:.1f} | MVP: {'✓' if mvp5}", Inline: false
  
  [TEAM AGGREGATE]
  - Name: "Combined K/D", Value: "{totalKills}/{totalDeaths} ({combinedKD:.2f})", Inline: true
  - Name: "Average ADR", Value: "{averageADR:.1f}", Inline: true
  - Name: "MVP", Value: "{mvpPlayerName} ({mvpReason})", Inline: false

Color: BLUE (#3498db) if W, GRAY (#95a5a6) if L
Timestamp: {matchTimestamp}
Footer: "Fetched from Faceit API"
```

### Error Response - Team Not Found (HTTP 404)

**Trigger**: Faceit API returns 404 for team name

**Response Body** (Discord Embed):
```
Title: "❌ Team Not Found"
Description: "Could not find team with that name."

Fields:
  - Name: "Team Name", Value: "{teamNameInput}", Inline: false
  - Name: "Troubleshooting", Value: "Check the team name on faceit.com and try again", Inline: false

Color: RED (#e74c3c)
```

### Error Response - No Recent Matches (HTTP 404)

**Trigger**: Team found but has no recent match data

**Response Body** (Discord Embed):
```
Title: "📭 No Recent Matches"
Description: "{teamName} has no recent match data available."

Fields:
  - Name: "Status", Value: "Check back after the team plays a match", Inline: false

Color: ORANGE (#f39c12)
```

### Error Response - API Unavailable (HTTP 503)

**Trigger**: Faceit API rate limited, down, or timeout

**Response Body** (Discord Embed):
```
Title: "⚠️ Service Temporarily Unavailable"
Description: "Unable to fetch team data at this time. Please try again later."

Color: ORANGE (#f39c12)
```

---

## Acceptance Criteria

1. ✅ Valid team name returns last match with all roster members' stats
2. ✅ Each player's K/D, ADR, and MVP status displayed
3. ✅ Team aggregate stats (combined K/D, average ADR) shown
4. ✅ MVP player highlighted with reason (highest ADR or fragger)
5. ✅ Team not found → Error message with troubleshooting hint
6. ✅ No recent matches → Helpful message

---

## Implementation Notes

- **Data Fetch**: `FaceitAPIClient.fetchTeam(teamName)` → `FaceitAPIClient.fetchTeamLastMatch(teamId)`
- **No Caching**: Team stats not cached (different use case than player stats; less frequently requested)
- **Performance**: API call only; expect <3s response time
- **MVP Determination**: Compare ADRs of all players; highest ADR = MVP (alternative: count kills, then deaths)
- **Roster Display**: Display exactly 5 players in order (may be fewer if team roster incomplete in API)
- **Match Result Color**: Blue for W (win), Gray for L (loss) or neutral color
- **Error Handling**: All HTTP exceptions caught, return standardized error per FR-011

---
