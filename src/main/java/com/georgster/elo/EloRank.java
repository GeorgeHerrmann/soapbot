package com.georgster.elo;

import discord4j.rest.util.Color;

/**
 * Represents the different Elo rank tiers based on rating ranges.
 * Each rank has an associated color for display purposes.
 */
public enum EloRank {
    SILVER(0, 1399, "Silver", Color.of(192, 192, 192)),
    GOLD(1400, 1599, "Gold", Color.of(255, 215, 0)),
    PLATINUM(1600, 1799, "Platinum", Color.of(229, 228, 226)),
    DIAMOND(1800, 1999, "Diamond", Color.of(185, 242, 255)),
    CHAMPIONS(2000, 2299, "Champions", Color.of(255, 105, 180)),
    THE_BEST(2300, Integer.MAX_VALUE, "The Best", Color.of(138, 43, 226));

    private final int minRating;
    private final int maxRating;
    private final String displayName;
    private final Color color;

    /**
     * Creates a new EloRank with the specified parameters.
     * 
     * @param minRating The minimum rating for this rank (inclusive)
     * @param maxRating The maximum rating for this rank (inclusive)
     * @param displayName The display name for this rank
     * @param color The Discord color for this rank
     */
    EloRank(int minRating, int maxRating, String displayName, Color color) {
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.displayName = displayName;
        this.color = color;
    }

    /**
     * Gets the EloRank corresponding to the given rating.
     * 
     * @param rating The Elo rating to check
     * @return The corresponding EloRank
     */
    public static EloRank fromRating(int rating) {
        for (EloRank rank : EloRank.values()) {
            if (rating >= rank.minRating && rating <= rank.maxRating) {
                return rank;
            }
        }
        return SILVER; // Fallback to lowest rank
    }

    /**
     * Gets the minimum rating for this rank.
     * 
     * @return The minimum rating (inclusive)
     */
    public int getMinRating() {
        return minRating;
    }

    /**
     * Gets the maximum rating for this rank.
     * 
     * @return The maximum rating (inclusive)
     */
    public int getMaxRating() {
        return maxRating;
    }

    /**
     * Gets the display name for this rank.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the Discord color for this rank.
     * 
     * @return The Discord color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the next rank above this one, if it exists.
     * 
     * @return The next rank, or null if this is the highest rank
     */
    public EloRank getNextRank() {
        EloRank[] ranks = EloRank.values();
        for (int i = 0; i < ranks.length - 1; i++) {
            if (ranks[i] == this) {
                return ranks[i + 1];
            }
        }
        return null; // This is the highest rank
    }

    /**
     * Gets the rating needed to reach the next rank.
     * 
     * @param currentRating The current rating
     * @return The rating needed for next rank, or -1 if already at highest rank
     */
    public int getRatingToNextRank(int currentRating) {
        EloRank nextRank = getNextRank();
        if (nextRank == null) {
            return -1; // Already at highest rank
        }
        return nextRank.getMinRating() - currentRating;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
