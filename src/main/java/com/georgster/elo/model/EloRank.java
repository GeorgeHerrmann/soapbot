package com.georgster.elo.model;

import discord4j.rest.util.Color;

/**
 * Represents the different Elo ranks with their rating ranges and associated colors.
 */
public enum EloRank {
    BRONZE(0, 1199, Color.of(205, 127, 50)),
    SILVER(1200, 1399, Color.of(192, 192, 192)),
    GOLD(1400, 1599, Color.of(255, 215, 0)),
    PLATINUM(1600, 1799, Color.of(229, 228, 226)),
    DIAMOND(1800, 1999, Color.of(185, 242, 255)),
    CHAMPION(2000, 2299, Color.of(255, 105, 180)),
    GRANDMASTER(2300, Integer.MAX_VALUE, Color.of(138, 43, 226));

    private final int minRating;
    private final int maxRating;
    private final Color color;

    /**
     * Creates an EloRank with the specified rating range and color.
     * 
     * @param minRating The minimum rating for this rank
     * @param maxRating The maximum rating for this rank
     * @param color The color associated with this rank
     */
    EloRank(int minRating, int maxRating, Color color) {
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.color = color;
    }

    /**
     * Gets the minimum rating for this rank.
     * 
     * @return The minimum rating
     */
    public int getMinRating() {
        return minRating;
    }

    /**
     * Gets the maximum rating for this rank.
     * 
     * @return The maximum rating
     */
    public int getMaxRating() {
        return maxRating;
    }

    /**
     * Gets the color associated with this rank.
     * 
     * @return The rank color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the EloRank for the given rating.
     * 
     * @param rating The rating to find the rank for
     * @return The appropriate EloRank
     */
    public static EloRank fromRating(int rating) {
        for (EloRank rank : values()) {
            if (rating >= rank.minRating && rating <= rank.maxRating) {
                return rank;
            }
        }
        return BRONZE; // Default fallback
    }

    /**
     * Gets a formatted display name for this rank.
     * 
     * @return The formatted rank name
     */
    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
