# API Contract: CS2 Help Command

**Endpoint**: `!cs2 help` or `/cs2 help`

**Feature**: 002-cs2-faceit | **Story**: Supporting Command - Usage Reference

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `help`  
**Required Permission**: `DEFAULT` (no special permission needed)

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "help",
      "type": 1,
      "description": "Display CS2 Faceit integration commands and usage"
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
| `userId` | Snowflake | Discord Event | Executor's ID (unused) |

---

## Response Contract

### Success Response (HTTP 200)

**Trigger**: Help command executed (always succeeds)

**Response Body** (Discord Embed):
```
Title: "📖 CS2 Faceit Integration Help"
Description: "Complete guide to all available commands"

Fields:
  [ACCOUNT MANAGEMENT]
  - Name: "/cs2 link <username>", Value: "Link your Faceit account to your Discord profile\nExample: `/cs2 link GeorgsterCS`", Inline: false
  - Name: "/cs2 unlink", Value: "Unlink your Faceit account from Discord", Inline: false
  
  [PLAYER STATISTICS]
  - Name: "/cs2 stats [@player]", Value: "View comprehensive CS2 statistics\nExample: `/cs2 stats` or `/cs2 stats @friend`", Inline: false
  - Name: "/cs2 match [@player]", Value: "View your most recent match report\nExample: `/cs2 match` or `/cs2 match @friend`", Inline: false
  - Name: "/cs2 history [@player]", Value: "View last 10 matches and recent form\nExample: `/cs2 history`", Inline: false
  
  [COMPARISONS & LEADERBOARDS]
  - Name: "/cs2 compare @player1 @player2", Value: "Compare two players' statistics side-by-side\nExample: `/cs2 compare @you @friend`", Inline: false
  - Name: "/cs2 leaderboard", Value: "View top 10 linked players in this server ranked by Elo", Inline: false
  
  [TEAM LOOKUP]
  - Name: "/cs2 team <team-name>", Value: "View team's last match statistics\nExample: `/cs2 team Astralis`", Inline: false
  
  [PLAYER LOOKUP FORMATS]
  - Name: "Lookup Reference", Value: "`@Discord_User` - Use linked account\n`faceit:username` - Explicit Faceit username\n`steam:STEAMID` - Steam ID lookup\n`username` - Default Faceit username", Inline: false
  
  [NOTES]
  - Name: "⏱️ Performance", Value: "Stats cached for 5 minutes. Leaderboard updates every 10 minutes.", Inline: false
  - Name: "🔐 Permissions", Value: "All commands require `CS2COMMAND` permission", Inline: false
  - Name: "📌 API Info", Value: "Data sourced from Faceit API. Errors handled gracefully with standardized messages.", Inline: false

Color: BLUE (#3498db)
Footer: "Created for SoapBot | v2.921+"
```

---

## Acceptance Criteria

1. ✅ `/cs2 help` displays all available commands with descriptions
2. ✅ Usage examples provided for each command
3. ✅ Player lookup formats explained clearly
4. ✅ Permission and performance notes included
5. ✅ Help is accessible to all users (no special permission needed)

---

## Implementation Notes

- **No API Calls**: This is a purely static response; no Faceit API interaction required
- **Embed Format**: Single large embed with categorized fields for easy scanning
- **Examples**: Include common use cases (self-lookup, friend lookup, comparisons)
- **Accessibility**: Designed for new users unfamiliar with CS2 commands
- **Maintenance**: Update when new commands added or command syntax changes

---
