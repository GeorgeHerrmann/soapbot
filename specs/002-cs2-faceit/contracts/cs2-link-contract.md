# API Contract: CS2 Link Command

**Endpoint**: `!cs2 link <faceit-username>` (Discord message command) or `/cs2 link <faceit-username>` (slash command)

**Feature**: 002-cs2-faceit | **Story**: User Story 1 - Link Faceit Account

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `link`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "description": "CS2 Faceit integration commands",
  "options": [
    {
      "name": "link",
      "type": 1,
      "description": "Link your Faceit account to your Discord profile",
      "options": [
        {
          "name": "faceit_username",
          "type": 3,
          "description": "Your Faceit username (e.g., GeorgsterCS)",
          "required": true,
          "min_length": 1,
          "max_length": 32
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
| `faceit_username` | String | YES | 1-32 alphanumeric + special chars | `GeorgsterCS` | Faceit username, case-sensitive |

### Message Context
| Attribute | Type | Source | Notes |
|-----------|------|--------|-------|
| `guildId` | Snowflake | Discord Event | Guild where command executed |
| `userId` | Snowflake | Discord Event | Discord user executing command |
| `username` | String | Discord Event | Discord user's display name |

---

## Response Contract

### Success Response (HTTP 200)

**Trigger**: Faceit API returns valid player profile matching username

**Response Body** (Discord Embed):
```
Title: "✅ Faceit Account Linked!"
Fields:
  - Name: "Nickname", Value: "{faceitNickname}", Inline: true
  - Name: "Faceit Level", Value: "{faceitLevel} (Elo: {elo})", Inline: true
  - Name: "Country", Value: "{country}", Inline: false
  - Name: "Status", Value: "Account linked successfully. Use `/cs2 stats` to view your stats.", Inline: false

Color: GREEN (#2ecc71)
Thumbnail: {playerAvatar URL}
```

### Error Response - Invalid Username (HTTP 400)

**Trigger**: Faceit API returns 404 (player not found)

**Response Body** (Discord Embed):
```
Title: "❌ Account Link Failed"
Description: "Player not found. Please check your Faceit username and try again."
Fields:
  - Name: "Username Provided", Value: "{faceitUsernameInput}", Inline: false
  - Name: "Troubleshooting", Value: "Visit faceit.com to verify your username", Inline: false

Color: RED (#e74c3c)
```

### Error Response - API Unavailable (HTTP 503)

**Trigger**: Faceit API returns 429 (rate limited), 500+, or timeout

**Response Body** (Discord Embed):
```
Title: "⚠️ Service Temporarily Unavailable"
Description: "Unable to link account at this time. Please try again later."

Color: ORANGE (#f39c12)
```

---

## Acceptance Criteria

1. ✅ User with valid Faceit account links successfully → Confirmation embed displayed with Elo/level
2. ✅ User replaces existing link → Old account removed, new one confirmed
3. ✅ User enters invalid Faceit username → Error message displayed, no account saved
4. ✅ User unlinks account → CS2Profile cleared from database

---

## Implementation Notes

- **API Call**: `FaceitAPIClient.fetchPlayer(faceitUsername)` returns `FaceitPlayer` or throws `PlayerNotFoundException`
- **Storage**: `UserProfileManager.get(userId)` → update `CS2Profile` nested object → `UserProfileManager.update(profile)`
- **Validation**: Faceit username matched via API; no format validation needed (Faceit API validates)
- **Error Handling**: Catch all HTTP exceptions, return standardized error per FR-011
- **Testing**: Mock Faceit API with both success and failure scenarios

---
