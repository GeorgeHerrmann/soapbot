package com.georgster.elo.model;

/**
 * Utility class for calculating Elo rating changes and related values.
 */
public class EloCalculator {
    
    private static final double BASE_K_FACTOR = 32.0;
    private static final int RATING_DIFFERENCE_THRESHOLD = 400;
    private static final double WIN_STREAK_MULTIPLIER = 0.1;
    private static final double DIMINISHING_RETURNS_FACTOR = 0.95;

    /**
     * Calculates the new rating for a player after a match.
     * 
     * @param player The player's rating object
     * @param opponent The opponent's rating object
     * @param playerWon Whether the player won the match
     * @return The new rating for the player
     */
    public static int calculateNewRating(EloRating player, EloRating opponent, boolean playerWon) {
        double expectedScore = calculateExpectedScore(player.getRating(), opponent.getRating());
        double actualScore = playerWon ? 1.0 : 0.0;
        double kFactor = calculateKFactor(player);
        
        double baseChange = kFactor * (actualScore - expectedScore);
        
        if (playerWon) {
            // Apply win streak bonus
            baseChange = applyWinStreakBonus(baseChange, player.getWinStreak());
            
            // Apply diminishing returns for repeated wins against same opponent
            int consecutiveWins = player.getWinsAgainst(opponent.getMemberId());
            baseChange = applyDiminishingReturns(baseChange, consecutiveWins);
        }
        
        // Apply rating difference modifier
        baseChange = applyRatingDifferenceModifier(baseChange, 
                                                  Math.abs(player.getRating() - opponent.getRating()));
        
        int newRating = player.getRating() + (int) Math.round(baseChange);
        
        // Ensure rating doesn't go below 0
        return Math.max(0, newRating);
    }

    /**
     * Calculates the expected score for a player against an opponent.
     * 
     * @param playerRating The player's current rating
     * @param opponentRating The opponent's current rating
     * @return Expected score (0.0 to 1.0)
     */
    public static double calculateExpectedScore(int playerRating, int opponentRating) {
        double ratingDifference = opponentRating - playerRating;
        return 1.0 / (1.0 + Math.pow(10.0, ratingDifference / 400.0));
    }

    /**
     * Calculates the K-factor for a player based on their experience and rating.
     * 
     * @param rating The player's rating object
     * @return The K-factor to use for rating calculations
     */
    public static double calculateKFactor(EloRating rating) {
        // Higher K-factor for new players (first 10 games)
        if (rating.getMatchesPlayed() < 10) {
            return BASE_K_FACTOR * 1.5;
        }
        
        // Lower K-factor for high-rated players (above 2000)
        if (rating.getRating() >= 2000) {
            return BASE_K_FACTOR * 0.75;
        }
        
        return BASE_K_FACTOR;
    }

    /**
     * Applies a win streak bonus to the base rating gain.
     * 
     * @param baseGain The base rating gain
     * @param winStreak The current win streak
     * @return Modified rating gain with win streak bonus
     */
    public static double applyWinStreakBonus(double baseGain, int winStreak) {
        if (winStreak <= 0 || baseGain <= 0) {
            return baseGain;
        }
        
        // Cap win streak bonus at 5 wins
        int cappedStreak = Math.min(winStreak, 5);
        double bonus = cappedStreak * WIN_STREAK_MULTIPLIER;
        
        return baseGain * (1.0 + bonus);
    }

    /**
     * Applies diminishing returns for consecutive wins against the same opponent.
     * 
     * @param baseGain The base rating gain
     * @param consecutiveWins Number of consecutive wins against this opponent
     * @return Modified rating gain with diminishing returns applied
     */
    public static double applyDiminishingReturns(double baseGain, int consecutiveWins) {
        if (consecutiveWins <= 0 || baseGain <= 0) {
            return baseGain;
        }
        
        // Apply diminishing returns for each consecutive win
        double modifier = Math.pow(DIMINISHING_RETURNS_FACTOR, consecutiveWins);
        
        return baseGain * modifier;
    }

    /**
     * Applies a modifier based on the rating difference between players.
     * 
     * @param baseGain The base rating gain
     * @param ratingDiff The absolute rating difference between players
     * @return Modified rating gain
     */
    public static double applyRatingDifferenceModifier(double baseGain, int ratingDiff) {
        if (ratingDiff > RATING_DIFFERENCE_THRESHOLD) {
            // Reduce gains/losses for very large rating differences
            double modifier = RATING_DIFFERENCE_THRESHOLD / (double) ratingDiff;
            modifier = Math.max(0.5, modifier); // Minimum 50% of normal gain/loss
            return baseGain * modifier;
        }
        
        return baseGain;
    }

    /**
     * Calculates the rating change preview for display purposes.
     * 
     * @param player The player's rating object
     * @param opponent The opponent's rating object
     * @param playerWon Whether the player would win
     * @return The rating change (positive for gain, negative for loss)
     */
    public static int calculateRatingChange(EloRating player, EloRating opponent, boolean playerWon) {
        int newRating = calculateNewRating(player, opponent, playerWon);
        return newRating - player.getRating();
    }
}
