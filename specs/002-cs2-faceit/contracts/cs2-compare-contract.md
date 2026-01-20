# API Contract: CS2 Compare Command

**Endpoint**: `!cs2 compare @player1 @player2` or `/cs2 compare @player1 @player2`

**Feature**: 002-cs2-faceit | **Story**: User Story 4 - Compare Two Players' Statistics

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `compare`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "compare",
      "type": 1,
      "description": "Compare two players' statistics side-by-side",
      "options": [
        {
          "name": "player1",
          "type": 9,
          "description": "First player (mention or Faceit username)",
          "required": true
        },
        {
          "name": "player2",
          "type": 9,
          "description": "Second player (mention or Faceit username)",
          "required": true
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
| `player1` | String/Mention | YES | Discord mention OR Faceit username | `@Player1` | First comparison player |
| `player2` | String/Mention | YES | Discord mention OR Faceit username | `@Player2` | Second comparison player |

### Lookup Resolution
(Same precedence as CS2 Stats Command for each player)

---

## Response Contract

### Success Response (HTTP 200)

**Trigger**: Both players found and stats available

**Response Body** (Discord Embed):
```
Title: "⚔️ Player Comparison"
Subtitle: "{player1Nickname} vs {player2Nickname}"

Fields:
  [RANK & ELO]
  - Name: "Faceit Level", Value: "{player1Level} ★ {player2Level}", Inline: true
  - Name: "Elo Rating", Value: "{player1Elo} {'🟢 ↑' if better} {player2Elo}", Inline: true
  
  [MATCH RECORD]
  - Name: "Matches Played", Value: "{player1Matches} vs {player2Matches}", Inline: true
  - Name: "Win Rate", Value: "{player1WR:.1f}% {'🟢' if better} {player2WR:.1f}%", Inline: true
  
  [COMBAT STATISTICS]
  - Name: "K/D Ratio", Value: "{player1KD:.2f} {'🟢' if better} {player2KD:.2f}", Inline: true
  - Name: "ADR", Value: "{player1ADR:.1f} {'🟢' if better} {player2ADR:.1f}", Inline: true
  - Name: "HS%", Value: "{player1HS:.1f}% {'🟢' if better} {player2HS:.1f}%", Inline: true
  - Name: "MVPs/Match", Value: "{player1MVPM:.2f} {'🟢' if better} {player2MVPM:.2f}", Inline: true
  
  [RECENT FORM]
  - Name: "Last 5 Matches", Value: "{player1Form} vs {player2Form}", Inline: false
    (W=🟢, L=🔴)
  
  [SUMMARY]
  - Name: "Overall Assessment", Value: "{summaryText}", Inline: false

Color: BLUE (#3498db)
Footer: "Comparison based on current Faceit data"
```

### Success Response - One Player Not Found (HTTP 404)

**Trigger**: One player lookup fails

**Response Body** (Discord Embed):
```
Title: "❌ Player Not Found"
Description: "Could not find {playerName}."

Fields:
  - Name: "Found Player", Value: "{successfulPlayer}", Inline: true
  - Name: "Missing Player", Value: "{failedPlayer}", Inline: true

Color: RED (#e74c3c)
```

### Success Response - One Account Not Linked (HTTP 403)

**Trigger**: One player is mentioned but not linked

**Response Body** (Discord Embed):
```
Title: "⚠️ Account Not Linked"
Description: "{playerName} hasn't linked their Faceit account."

Fields:
  - Name: "Found Player", Value: "{successfulPlayer} (linked)", Inline: false
  - Name: "Missing Account", Value: "{failedPlayer} - ask them to link with `/cs2 link <username>`", Inline: false

Color: ORANGE (#f39c12)
```

### Error Response - Both Players Not Found (HTTP 404)

**Response Body** (Discord Embed):
```
Title: "❌ Both Players Not Found"
Description: "Could not find either player for comparison."

Color: RED (#e74c3c)
```

---

## Acceptance Criteria

1. ✅ Two linked players compared → Side-by-side stats with visual indicators (arrows/emojis for better stat)
2. ✅ All metrics displayed: Elo, K/D, ADR, HS%, win rate, recent form
3. ✅ Summary assessment provided (e.g., "Player1 is currently performing better")
4. ✅ One unlinked player → Error asking to link, but shows successful player's stats
5. ✅ Both unlinked → Error message

---

## Implementation Notes

- **Data Fetch**: Parallel fetch both players' stats via `FaceitAPIClient` for performance
- **Visual Indicators**: Use emoji (🟢 for better, 🔴 for worse) or arrows (↑/↓) for comparison clarity
- **Summary Logic**: Simple heuristic: count metrics where player1 > player2; declare winner if significantly ahead
- **Recent Form**: Extract last 5 match results from both players' histories
- **Null Handling**: If stats incomplete for one player, note in summary that comparison is partial
- **Caching**: Use existing CachedPlayerStats for both players (may require 2 cache fetches)

---
