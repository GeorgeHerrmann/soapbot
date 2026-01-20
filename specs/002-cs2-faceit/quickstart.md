# Quick Start: CS2 Faceit Integration

**Feature**: 002-cs2-faceit | **Date**: 2026-01-19

---

## Overview

The CS2 Faceit integration allows SoapBot to fetch and display Faceit player statistics, match reports, and server leaderboards. Users link their Faceit accounts once, then access all their stats via simple commands.

---

## Setup

### 1. Obtain Faceit API Key

1. Visit [Faceit Developers Portal](https://developers.faceit.com/)
2. Log in with your Faceit account
3. Create a new application
4. Copy your **API Key**
5. Store securely (do NOT commit to git)

### 2. Configure API Key in SoapBot

**Option A: Environment Variable (Recommended)**
```bash
export FACEIT_API_KEY="your-api-key-here"
```

**Option B: Configuration File**
Create file `faceit_api_key.txt` in project root:
```
your-api-key-here
```
(Add to `.gitignore` to prevent accidental commits)

**Option C: Spring Boot application.properties**
```properties
faceit.api.key=your-api-key-here
```

### 3. Add Maven Dependencies

Update `pom.xml` with new dependencies (to be added by developers):
```xml
<!-- HTTP Client -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>3.11.0</version>
</dependency>

<!-- Caching -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.2</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.2.0</version>
    <scope>test</scope>
</dependency>
```

### 4. Add CS2COMMAND Permission

Modify `com.georgster.permissions.PermissibleAction` enum:
```java
public enum PermissibleAction {
    DEFAULT("default"),
    ADMIN("admin"),
    CS2COMMAND("cs2_command"),  // NEW
    // ... other permissions
}
```

### 5. Build and Deploy

```bash
mvn clean compile
mvn test
mvn package
# Deploy jar to server
```

---

## First Commands

### Link Your Faceit Account

```
/cs2 link GeorgsterCS
```

**Expected Output**:
```
✅ Faceit Account Linked!
Nickname: GeorgsterCS
Faceit Level: 8 (Elo: 1850)
Country: 🇩🇰 Denmark
Status: Account linked successfully. Use `/cs2 stats` to view your stats.
```

### View Your Stats

```
/cs2 stats
```

**Expected Output**:
```
📈 CS2 Statistics - GeorgsterCS
Faceit Level 8 | ELO: 1850

Total Matches: 342
Wins / Losses: 215 / 127
Win Rate: 62.9%

K/D Ratio: 1.45
ADR: 78.3
HS%: 18.5%
MVPs/Match: 0.42

BEST MAPS:
de_mirage: Win Rate 68.2% | K/D 1.52 | ADR 82.1
de_inferno: Win Rate 65.0% | K/D 1.38 | ADR 76.5
de_nuke: Win Rate 60.3% | K/D 1.35 | ADR 74.2

Recent Form: 🟢 🟢 🔴 🟢 🔴 🟢 🟢 🟢 🔴 🟢
```

### View Last Match

```
/cs2 match
```

**Expected Output**:
```
📊 Last Match - de_mirage
GeorgsterCS | Level 8

Result: W (16-14)
K/D: 18/12 (1.50)
Assists: 5
ADR: 82.3
HS%: 22.5%
MVPs: 2
Rounds: 30

vs. Your Average:
K/D Average: 1.45 → 1.50 (+0.05)
ADR Average: 78.3 → 82.3 (+4.0)
HS% Average: 18.5% → 22.5% (+4.0%)
```

### View Server Leaderboard

```
/cs2 leaderboard
```

**Expected Output**:
```
🏆 Server Leaderboard - CS2 Elo Rankings
Top 10 linked players in this server

#1 🥇 GeorgsterCS
Elo: 1850 | Level: 8 | K/D: 1.45

#2 🥈 ProPlayer42
Elo: 1720 | Level: 7 | K/D: 1.38

#3 🥉 MidTier88
Elo: 1650 | Level: 7 | K/D: 1.22

... (up to 10)

Total Linked Players: 24
Server Average Elo: 1420
```

### Compare Two Players

```
/cs2 compare @GeorgsterCS @ProPlayer42
```

**Expected Output**:
```
⚔️ Player Comparison
GeorgsterCS vs ProPlayer42

Faceit Level: 8 ★ 7
Elo Rating: 1850 🟢 1720

K/D Ratio: 1.45 🟢 1.38
ADR: 78.3 🟢 76.1
HS%: 18.5% 🟢 17.2%

Recent Form: 🟢🟢🔴🟢🔴 vs 🟢🟢🟢🔴🔴

Overall Assessment: GeorgsterCS is currently performing better with higher Elo and K/D.
```

### View Match History

```
/cs2 history
```

**Expected Output**:
```
📋 Match History - GeorgsterCS
Last 10 Matches

#1: de_mirage | W | K/D: 18/12 (1.50) | ADR: 82.3 | 2 hours ago
#2: de_inferno | W | K/D: 16/14 (1.14) | ADR: 71.2 | 4 hours ago
#3: de_nuke | L | K/D: 12/15 (0.80) | ADR: 65.1 | 6 hours ago
... (up to 10 matches)

Record: 7W - 3L (70% win rate)
Average K/D: 1.45
Average ADR: 78.3
```

---

## Player Lookup Formats

All commands that reference players support multiple input formats:

| Format | Example | Resolution |
|--------|---------|-----------|
| **Discord Mention** | `@GeorgsterCS` | Looks up Discord user's linked Faceit account |
| **Faceit Username** | `GeorgsterCS` or `faceit:GeorgsterCS` | Direct Faceit username lookup |
| **Steam ID** | `steam:STEAM_0:0:123456789` | Steam ID → Faceit account lookup |

**Precedence Order**: Mention → faceit: prefix → steam: prefix → bare text as Faceit username

---

## Troubleshooting

### "Player Not Found" Error

**Cause**: Faceit username typo or account doesn't exist  
**Fix**:
1. Verify username at [faceit.com](https://faceit.com) (case-sensitive)
2. Try again with correct spelling: `/cs2 link CorrectUsername`

### "Account Not Linked" Error

**Cause**: Target user hasn't linked their Faceit account  
**Fix**:
1. Ask user to execute: `/cs2 link <their-faceit-username>`
2. Wait 1-2 minutes for cache to update
3. Try command again

### "Service Temporarily Unavailable" Error

**Cause**: Faceit API is rate-limited, down, or experiencing issues  
**Fix**:
1. Wait 1-2 minutes and try again
2. Check [Faceit Status Page](https://status.faceit.com/)
3. If API is down, retry in 10-15 minutes

### Stats Show Cached Data

**Info**: Stats are cached for 5 minutes to reduce API calls  
**Note**: Background refresh happens automatically; you may see stats update a few seconds after the initial response  
**Force Refresh**: Wait 5 minutes for cache TTL to expire, or unlink/relink your account

### Leaderboard Not Updating

**Info**: Leaderboard refreshes every 10 minutes via background task  
**Expected**: After you link your account, you'll appear in leaderboard within 10 minutes  
**Manual Refresh**: Leaderboard refreshes immediately when a user links/unlinks an account

---

## Command Reference

| Command | Purpose | Example |
|---------|---------|---------|
| `/cs2 link <username>` | Link Faceit account | `/cs2 link GeorgsterCS` |
| `/cs2 unlink` | Unlink Faceit account | `/cs2 unlink` |
| `/cs2 stats [@player]` | View comprehensive stats | `/cs2 stats` or `/cs2 stats @friend` |
| `/cs2 match [@player]` | View last match report | `/cs2 match` |
| `/cs2 history [@player]` | View last 10 matches | `/cs2 history` |
| `/cs2 compare @p1 @p2` | Compare two players | `/cs2 compare @you @friend` |
| `/cs2 leaderboard` | View server leaderboard | `/cs2 leaderboard` |
| `/cs2 team <name>` | View team last match | `/cs2 team Astralis` |
| `/cs2 help` | Display help | `/cs2 help` |

---

## Performance Notes

- **First Load**: Initial command may take 1-3 seconds (API call)
- **Cached Load**: Subsequent commands within 5 minutes load in <500ms
- **Leaderboard**: Updates every 10 minutes in background
- **Memory**: ~1MB total cache for 100+ linked users

---

## Permissions

All CS2 commands require `CS2COMMAND` permission set in the permission system. Ask your server administrator to grant this permission if you can't execute commands.

---

## Support

For issues or feature requests, refer to the main SoapBot documentation or contact the development team.
