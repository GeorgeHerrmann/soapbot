# API Contract: CS2 Unlink Command

**Endpoint**: `!cs2 unlink` or `/cs2 unlink`

**Feature**: 002-cs2-faceit | **Story**: User Story 1 - Link Faceit Account (Unlink variant)

---

## Command Definition

**Primary Alias**: `cs2`  
**Subcommand**: `unlink`  
**Required Permission**: `CS2COMMAND`

**Slash Command Schema**:
```json
{
  "name": "cs2",
  "type": 1,
  "options": [
    {
      "name": "unlink",
      "type": 1,
      "description": "Unlink your Faceit account from Discord"
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
| `userId` | Snowflake | Discord Event | User whose account is being unlinked |

---

## Response Contract

### Success Response (HTTP 200 - Account Was Linked)

**Trigger**: User has CS2Profile linked

**Response Body** (Discord Embed):
```
Title: "✅ Account Unlinked"
Description: "Your Faceit account has been successfully removed from your Discord profile."

Fields:
  - Name: "Nickname", Value: "{formerFaceitNickname}", Inline: true
  - Name: "Status", Value: "Unlinked", Inline: true
  - Name: "Next Steps", Value: "You can link a new account at any time with `/cs2 link <username>`", Inline: false

Color: ORANGE (#f39c12)
```

### Success Response (HTTP 200 - Account Was Not Linked)

**Trigger**: User has no CS2Profile linked

**Response Body** (Discord Embed):
```
Title: "ℹ️ No Account Linked"
Description: "You don't have a Faceit account linked to your Discord profile."

Fields:
  - Name: "Next Steps", Value: "Link an account with `/cs2 link <username>` to get started", Inline: false

Color: BLUE (#3498db)
```

---

## Acceptance Criteria

1. ✅ User with linked account executes `/cs2 unlink` → Account removed and confirmation shown
2. ✅ User without linked account executes `/cs2 unlink` → Informative message (not error)
3. ✅ After unlink, CS2Profile set to null or deleted from UserProfile
4. ✅ User can re-link a (potentially different) account afterward

---

## Implementation Notes

- **Data Modification**: `UserProfileManager.get(userId)` → set `CS2Profile` to null → `UserProfileManager.update(profile)`
- **MongoDB**: Remove CS2Profile field entirely from document (or set to null; design choice based on schema)
- **Cache Invalidation**: Clear all cache entries for this user: `CachedPlayerStats[{guildId}:{playerId}:*]`, `CachedMatchReport[{guildId}:{playerId}:*]`, etc.
- **Leaderboard Impact**: User removed from next leaderboard refresh; no manual refresh needed
- **No API Calls**: This is a local operation; no Faceit API interaction required
- **Error Handling**: Unlink always succeeds (either removes existing link or confirms none exists)

---
