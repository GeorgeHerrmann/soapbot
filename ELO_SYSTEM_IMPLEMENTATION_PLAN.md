# Elo System Implementation Plan for SOAP Bot

## Overview
This document provides a comprehensive plan for implementing an Elo rating system for SOAP Bot, leveraging existing systems and minimizing new code creation. The system will support competitive battles between users with dynamic ranking, battle wagering, and comprehensive tracking.

## System Architecture

### Core Components to Create

#### 1. Elo Rating Models (`src/main/java/com/georgster/elo/`)

**EloRating.java**
```java
public class EloRating implements Manageable {
    private final String memberId;
    private int rating;           // Current Elo rating (starts at 1500)
    private int matchesPlayed;    // Total matches played
    private int wins;             // Total wins
    private int losses;           // Total losses
    private int winStreak;        // Current win streak
    private int bestRating;       // Highest rating achieved
    private Map<String, Integer> opponentHistory; // Track wins vs specific opponents
    private DateTimed lastMatch;  // When last match was played
}
```

**EloRank.java**
```java
public enum EloRank {
    SILVER(0, 1399, Color.of(192, 192, 192)),
    GOLD(1400, 1599, Color.of(255, 215, 0)),
    PLATINUM(1600, 1799, Color.of(229, 228, 226)),
    DIAMOND(1800, 1999, Color.of(185, 242, 255)),
    CHAMPIONS(2000, 2299, Color.of(255, 105, 180)),
    THE_BEST(2300, Integer.MAX_VALUE, Color.of(138, 43, 226));
}
```

**EloCalculator.java**
```java
public class EloCalculator {
    // Core Elo calculation methods
    public static int calculateNewRating(EloRating player, EloRating opponent, boolean playerWon);
    public static double calculateExpectedScore(int playerRating, int opponentRating);
    public static int calculateKFactor(EloRating rating);
    // Modifier methods
    public static int applyWinStreakBonus(int baseGain, int winStreak);
    public static int applyDiminishingReturns(int baseGain, int consecutiveWins);
    public static int applyRatingDifferenceModifier(int baseGain, int ratingDiff);
}
```

#### 2. Battle System (`src/main/java/com/georgster/elo/battle/`)

**EloBattle.java**
```java
public class EloBattle extends TimedEvent implements SoapEvent {
    private final String battleId;
    private final String creator;
    private final String channel;
    private long eloWager;        // Optional Elo stakes
    private long coinWager;       // Optional coin wager
    private List<String> participants;
    private String winner;
    private BattleStatus status;
    private BattleType type;      // 1v1, tournament, etc.
}
```

**BattleWizard.java** (extends InputWizard)
```java
public class BattleWizard extends InputWizard {
    // Wizard windows for:
    // - Creating battles
    // - Joining battles
    // - Setting wagers
    // - Declaring winners
    // - Viewing battle history
}
```

#### 3. Data Management (`src/main/java/com/georgster/elo/manager/`)

**EloManager.java** (extends GuildedSoapManager<EloRating>)
```java
public class EloManager extends GuildedSoapManager<EloRating> {
    public List<EloRating> getLeaderboard();
    public List<EloRating> getRankings(EloRank rank);
    public void processBattleResult(EloBattle battle);
    public EloRating getOrCreateRating(String memberId);
}
```

**EloBattleManager.java** (extends GuildedSoapManager<EloBattle>)
```java
public class EloBattleManager extends GuildedSoapManager<EloBattle> {
    public List<EloBattle> getActiveBattles();
    public List<EloBattle> getBattleHistory(String memberId);
    public void startBattle(EloBattle battle);
    public void endBattle(String battleId, String winnerId);
}
```

#### 4. Commands (`src/main/java/com/georgster/elo/`)

**EloCommand.java** (implements ParseableCommand)
```java
public class EloCommand implements ParseableCommand {
    // Subcommands:
    // - rank [user] - Show user's Elo rating and rank
    // - leaderboard - Show server leaderboard
    // - battle create <wager> - Create a battle
    // - battle join <id> - Join a battle
    // - battle list - List active battles
    // - battle history [user] - Show battle history
    // - battle winner <id> <user> - Declare battle winner
}
```

## Integration with Existing Systems

### 1. UserProfile Integration
- Add `EloRating eloRating` field to UserProfile
- Modify UserProfile constructors to initialize EloRating with 1500 starting rating
- Update UserProfileManager to handle Elo data persistence

```java
// In UserProfile.java
private final EloRating eloRating;

public UserProfile(String serverId, String userId, String user) {
    // ... existing code ...
    this.eloRating = new EloRating(userId);
}
```

### 2. Economy System Integration
- Leverage existing CoinBank for coin wagering
- Use existing InsufficientCoinsException for wager validation
- Integrate with coin transfer mechanisms

```java
// In EloBattle.java
public void processCoinWager(UserProfile winner, UserProfile loser) {
    if (coinWager > 0) {
        loser.getBank().transferTo(coinWager, winner.getBank());
    }
}
```

### 3. Wizard System Integration
- Extend existing InputWizard framework
- Reuse InputListenerFactory for consistent UI
- Leverage existing wizard navigation patterns

```java
// In BattleWizard.java
public void createBattle() {
    withResponse(response -> {
        if (response.equals("coin wager")) {
            nextWindow("setCoinWager");
        } else if (response.equals("elo stakes")) {
            nextWindow("setEloStakes");
        }
    }, true, "What type of wager would you like?", 
    "Coin Wager", "Elo Stakes", "No Wager");
}
```

### 4. Event System Integration
- Implement EloBattle as SoapEvent for scheduling
- Use existing SoapEventManager for battle lifecycle
- Leverage TimedEvent for battle timeouts

```java
// In EloBattleManager.java
public void scheduleBattle(EloBattle battle) {
    eventManager.add(battle); // Reuses existing event scheduling
}
```

### 5. Database Integration
- Add ELO ProfileType to existing ProfileType enum
- Extend DatabaseService to handle EloRating and EloBattle objects
- Reuse existing database adapter patterns

```java
// In ProfileType.java
public enum ProfileType {
    // ... existing types ...
    ELO_RATINGS,
    ELO_BATTLES
}
```

### 6. Game System Integration
- Integrate with existing CardGame/DiscordGame infrastructure
- Automatically create Elo battles when games have wagering
- Use existing game result tracking

```java
// In existing game classes
@Override
public void endGame() {
    super.endGame();
    if (hasEloWager()) {
        eloManager.processBattleResult(createEloBattleFromGame());
    }
}
```

## Implementation Steps

### Phase 1: Core Infrastructure
1. Create Elo rating models (EloRating, EloRank, EloCalculator)
2. Extend UserProfile to include EloRating
3. Create EloManager extending GuildedSoapManager
4. Add database support for Elo data

### Phase 2: Battle System
1. Create EloBattle extending TimedEvent
2. Create EloBattleManager
3. Integrate with existing SoapEventManager
4. Create BattleWizard extending InputWizard

### Phase 3: Commands and UI
1. Create EloCommand implementing ParseableCommand
2. Add slash command support
3. Implement leaderboard and ranking displays
4. Add Elo information to existing profile displays

### Phase 4: Game Integration
1. Integrate with existing BlackJackGame
2. Integrate with existing PlinkoGame
3. Add Elo wagering to game commands
4. Automatic battle creation from games

### Phase 5: Advanced Features
1. Implement tournament brackets
2. Add season resets
3. Create achievement system
4. Add detailed statistics tracking

## Elo Calculation Implementation

### Core Formula
```java
public static int calculateNewRating(EloRating player, EloRating opponent, boolean playerWon) {
    double expectedScore = calculateExpectedScore(player.getRating(), opponent.getRating());
    int kFactor = calculateKFactor(player);
    double actualScore = playerWon ? 1.0 : 0.0;
    
    int baseChange = (int) Math.round(kFactor * (actualScore - expectedScore));
    
    // Apply modifiers
    if (playerWon) {
        baseChange = applyWinStreakBonus(baseChange, player.getWinStreak());
        baseChange = applyRatingDifferenceModifier(baseChange, 
            opponent.getRating() - player.getRating());
        baseChange = applyDiminishingReturns(baseChange, 
            player.getConsecutiveWinsAgainst(opponent.getMemberId()));
    }
    
    // Ensure minimum gain/loss
    if (Math.abs(baseChange) < 1) {
        baseChange = playerWon ? 1 : -1;
    }
    
    return Math.max(0, player.getRating() + baseChange);
}
```

### K-Factor Implementation
```java
public static int calculateKFactor(EloRating rating) {
    if (rating.getMatchesPlayed() < 30) {
        return 40; // New players
    } else if (rating.getRating() >= 2000) {
        return 24; // High-tier players
    } else {
        return 32; // Normal players
    }
}
```

### Modifier Implementations
```java
public static int applyWinStreakBonus(int baseGain, int winStreak) {
    if (winStreak >= 3) {
        double multiplier = 1.0 + Math.min(0.5, (winStreak - 2) * 0.1);
        return (int) Math.round(baseGain * multiplier);
    }
    return baseGain;
}

public static int applyDiminishingReturns(int baseGain, int consecutiveWins) {
    if (consecutiveWins >= 2) {
        return Math.max(1, baseGain / 2);
    }
    return baseGain;
}

public static int applyRatingDifferenceModifier(int baseGain, int ratingDiff) {
    if (ratingDiff >= 300) {
        return (int) Math.round(baseGain * 1.5); // Beating much higher player
    } else if (ratingDiff <= -300) {
        return Math.min(-40, (int) Math.round(baseGain * 1.5)); // Losing to much lower player
    }
    return baseGain;
}
```

## Database Schema

### EloRating Table
```sql
CREATE TABLE elo_ratings (
    member_id VARCHAR(20) PRIMARY KEY,
    guild_id VARCHAR(20) NOT NULL,
    rating INTEGER DEFAULT 1500,
    matches_played INTEGER DEFAULT 0,
    wins INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    win_streak INTEGER DEFAULT 0,
    best_rating INTEGER DEFAULT 1500,
    opponent_history TEXT, -- JSON map of opponent wins
    last_match TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### EloBattle Table
```sql
CREATE TABLE elo_battles (
    battle_id VARCHAR(36) PRIMARY KEY,
    guild_id VARCHAR(20) NOT NULL,
    creator_id VARCHAR(20) NOT NULL,
    channel_id VARCHAR(20) NOT NULL,
    elo_wager INTEGER DEFAULT 0,
    coin_wager BIGINT DEFAULT 0,
    participants TEXT, -- JSON array of member IDs
    winner_id VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    battle_type VARCHAR(20) DEFAULT '1V1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP
);
```

## Command Usage Examples

### Basic Commands
```
!elo rank                    # Show your Elo rating
!elo rank @user             # Show user's Elo rating  
!elo leaderboard            # Show server leaderboard
!elo battle create 100      # Create battle with 100 coin wager
!elo battle join battle123  # Join battle with ID
!elo battle list            # List active battles
!elo battle history         # Show your battle history
!elo battle winner battle123 @user  # Declare winner (battle creator only)
```

### Integration with Games
```
!blackjack 50 --elo         # Play blackjack with Elo stakes
!plinko --elo-battle @user  # Challenge user to Elo plinko battle
```

## Wizard Flow Examples

### Creating a Battle
1. **Battle Type Selection**: 1v1, Tournament, Quick Match
2. **Wager Settings**: Coin amount, Elo stakes, or no wager
3. **Battle Settings**: Time limit, game type selection
4. **Confirmation**: Review settings and create battle

### Joining a Battle
1. **Battle List**: Show available battles with details
2. **Battle Selection**: Choose battle to join
3. **Wager Confirmation**: Confirm you can afford the wager
4. **Join Confirmation**: Final confirmation to join

## Error Handling and Validation

### Wager Validation
- Check user has sufficient coins for coin wagers
- Validate Elo stakes don't exceed reasonable limits
- Prevent self-battles
- Check user isn't already in an active battle

### Battle Management
- Auto-expire battles after set time limit
- Handle disconnections gracefully
- Validate winner declarations
- Prevent duplicate battle participation

## Testing Strategy

### Unit Tests
- EloCalculator formula accuracy
- Modifier application correctness
- Edge case handling (minimum ratings, maximum changes)

### Integration Tests  
- Database persistence and retrieval
- Wizard flow completion
- Command parsing and execution
- Event system integration

### Load Testing
- Multiple concurrent battles
- Large leaderboard queries
- High-frequency rating updates

## Deployment Considerations

### Migration Strategy
1. Add database tables for Elo system
2. Initialize all existing users with 1500 Elo rating
3. Deploy new code with feature flag
4. Gradually enable features per guild
5. Monitor for performance issues

### Rollback Plan
- Feature flags to disable Elo system
- Database backup before migration
- Ability to revert UserProfile changes
- Separate Elo data to minimize impact

## Performance Optimizations

### Database Optimizations
- Index on guild_id and rating for leaderboards
- Batch updates for multiple rating changes
- Connection pooling for high-frequency updates

### Caching Strategy
- Cache leaderboards with periodic refresh
- Cache user ratings for quick lookups
- Invalidate caches on rating updates

### Memory Management
- Limit battle history storage
- Archive old battles periodically
- Optimize opponent history storage

This comprehensive plan leverages SOAP Bot's existing architecture while adding a robust Elo system that integrates seamlessly with current features. The modular design allows for phased implementation and easy maintenance.
