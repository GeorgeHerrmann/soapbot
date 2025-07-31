package com.georgster.elo;

/**
 * Utility class for calculating Elo rating changes and applying various modifiers.
 * Implements the modified Elo formula with dynamic K-factors and bonus systems.
 */
public class EloCalculator {

    /**
     * Calculates the new Elo rating for a player after a match.
     * 
     * @param player The player's current EloRating
     * @param opponent The opponent's current EloRating
     * @param playerWon Whether the player won the match
     * @return The player's new Elo rating
     */
    public static int calculateNewRating(EloRating player, EloRating opponent, boolean playerWon) {
        double expectedScore = calculateExpectedScore(player.getRating(), opponent.getRating());
        int kFactor = calculateKFactor(player);
        double actualScore = playerWon ? 1.0 : 0.0;
        
        int baseChange = (int) Math.round(kFactor * (actualScore - expectedScore));
        
        // Apply modifiers if player won
        if (playerWon) {
            baseChange = applyWinStreakBonus(baseChange, player.getWinStreak());
            baseChange = applyRatingDifferenceModifier(baseChange, 
                opponent.getRating() - player.getRating());
            baseChange = applyDiminishingReturns(baseChange, 
                player.getConsecutiveWinsAgainst(opponent.getMemberId()));
        }
        
        // Ensure minimum gain/loss of 1 point
        if (Math.abs(baseChange) < 1) {
            baseChange = playerWon ? 1 : -1;
        }
        
        // Calculate new rating, ensuring it doesn't go below 0
        int newRating = player.getRating() + baseChange;
        return Math.max(0, newRating);
    }

    /**
     * Calculates the expected score for a player against an opponent.
     * Based on the standard Elo formula.
     * 
     * @param playerRating The player's current rating
     * @param opponentRating The opponent's current rating
     * @return The expected score (0.0 to 1.0)
     */
    public static double calculateExpectedScore(int playerRating, int opponentRating) {
        double ratingDifference = opponentRating - playerRating;
        return 1.0 / (1.0 + Math.pow(10.0, ratingDifference / 400.0));
    }

    /**
     * Calculates the K-factor based on player experience and rating.
     * Uses dynamic K-factor system:
     * - New players (< 30 matches): K = 40
     * - High-tier players (>= 2000 rating): K = 24
     * - Normal players: K = 32
     * 
     * @param rating The player's EloRating
     * @return The appropriate K-factor
     */
    public static int calculateKFactor(EloRating rating) {
        if (rating.getMatchesPlayed() < 30) {
            return 40; // New players get higher volatility
        } else if (rating.getRating() >= 2000) {
            return 24; // High-tier players get lower volatility
        } else {
            return 32; // Normal players
        }
    }

    /**
     * Applies win streak bonus to rating gains.
     * Players on a 3+ win streak get increasing bonuses up to 50%.
     * 
     * @param baseGain The base rating gain
     * @param winStreak The current win streak
     * @return The modified rating gain
     */
    public static int applyWinStreakBonus(int baseGain, int winStreak) {
        if (winStreak >= 3) {
            // Cap bonus at 50% for win streaks
            double multiplier = 1.0 + Math.min(0.5, (winStreak - 2) * 0.1);
            return (int) Math.round(baseGain * multiplier);
        }
        return baseGain;
    }

    /**
     * Applies diminishing returns for consecutive wins against the same opponent.
     * Prevents farming by reducing gains after 2+ consecutive wins against same player.
     * 
     * @param baseGain The base rating gain
     * @param consecutiveWins Number of consecutive wins against this opponent
     * @return The modified rating gain
     */
    public static int applyDiminishingReturns(int baseGain, int consecutiveWins) {
        if (consecutiveWins >= 2) {
            // Halve the gain for farming prevention, but ensure minimum gain of 1
            return Math.max(1, baseGain / 2);
        }
        return baseGain;
    }

    /**
     * Applies modifier based on large rating differences.
     * Rewards beating much higher-rated players and penalizes losing to much lower-rated players.
     * 
     * @param baseGain The base rating gain/loss
     * @param ratingDiff The rating difference (opponent - player)
     * @return The modified rating gain/loss
     */
    public static int applyRatingDifferenceModifier(int baseGain, int ratingDiff) {
        if (ratingDiff >= 300) {
            // Beating a much higher-rated player gives 50% bonus
            return (int) Math.round(baseGain * 1.5);
        } else if (ratingDiff <= -300) {
            // Losing to a much lower-rated player gives 50% penalty (capped at -40)
            return Math.min(-40, (int) Math.round(baseGain * 1.5));
        }
        return baseGain;
    }

    /**
     * Estimates the rating change for a hypothetical match without applying it.
     * Useful for showing potential gains/losses before battles.
     * 
     * @param player The player's current EloRating
     * @param opponent The opponent's current EloRating
     * @param playerWins Whether the player would win
     * @return The estimated rating change
     */
    public static int estimateRatingChange(EloRating player, EloRating opponent, boolean playerWins) {
        int currentRating = player.getRating();
        int newRating = calculateNewRating(player, opponent, playerWins);
        return newRating - currentRating;
    }

    /**
     * Calculates the probability of a player winning against an opponent.
     * 
     * @param playerRating The player's rating
     * @param opponentRating The opponent's rating
     * @return The win probability as a percentage (0-100)
     */
    public static double calculateWinProbability(int playerRating, int opponentRating) {
        return calculateExpectedScore(playerRating, opponentRating) * 100.0;
    }

    /**
     * Determines if a match would be considered balanced.
     * Matches are considered balanced if both players have 40-60% win probability.
     * 
     * @param playerRating The player's rating
     * @param opponentRating The opponent's rating
     * @return True if the match is balanced
     */
    public static boolean isBalancedMatch(int playerRating, int opponentRating) {
        double winProb = calculateWinProbability(playerRating, opponentRating);
        return winProb >= 40.0 && winProb <= 60.0;
    }
}
