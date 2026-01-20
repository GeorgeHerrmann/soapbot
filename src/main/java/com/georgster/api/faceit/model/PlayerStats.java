package com.georgster.api.faceit.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * Data Transfer Object representing a player's lifetime CS2 statistics on Faceit.
 * <p>
 * Contains comprehensive statistics including total matches, win rate, K/D ratio,
 * average damage per round, headshot percentage, MVPs per match, top maps performance,
 * and recent form (last 5 matches).
 * <p>
 * This class is designed for deserialization from Faceit API JSON responses using Gson.
 */
public class PlayerStats {
    
    @SerializedName("total_matches")
    private int totalMatches;
    
    @SerializedName("wins")
    private int wins;
    
    @SerializedName("losses")
    private int losses;
    
    @SerializedName("win_rate")
    private double winRate;
    
    @SerializedName("kd_ratio")
    private double killDeathRatio;
    
    @SerializedName("average_damage_round")
    private double averageDamageRound;
    
    @SerializedName("headshot_percentage")
    private double headshotPercentage;
    
    @SerializedName("mvps_per_match")
    private double mVPsPerMatch;
    
    @SerializedName("top_maps")
    private List<MapStats> topMaps;
    
    @SerializedName("recent_form")
    private List<String> recentForm; // e.g., ["W", "L", "W", "W", "L"]
    
    /**
     * Creates a new PlayerStats instance.
     */
    public PlayerStats() {
        this.topMaps = new ArrayList<>();
        this.recentForm = new ArrayList<>();
    }
    
    /**
     * Gets the total number of matches played.
     * 
     * @return The total match count
     */
    public int getTotalMatches() {
        return totalMatches;
    }
    
    /**
     * Gets the number of matches won.
     * 
     * @return The win count
     */
    public int getWins() {
        return wins;
    }
    
    /**
     * Gets the number of matches lost.
     * 
     * @return The loss count
     */
    public int getLosses() {
        return losses;
    }
    
    /**
     * Gets the win rate as a percentage.
     * 
     * @return The win rate (0.0-100.0)
     */
    public double getWinRate() {
        return winRate;
    }
    
    /**
     * Gets the kill-death ratio.
     * 
     * @return The K/D ratio
     */
    public double getKillDeathRatio() {
        return killDeathRatio;
    }
    
    /**
     * Gets the average damage per round.
     * 
     * @return The average damage per round
     */
    public double getAverageDamageRound() {
        return averageDamageRound;
    }
    
    /**
     * Gets the headshot percentage.
     * 
     * @return The headshot percentage (0.0-100.0)
     */
    public double getHeadshotPercentage() {
        return headshotPercentage;
    }
    
    /**
     * Gets the average MVPs per match.
     * 
     * @return The average MVPs per match
     */
    public double getMVPsPerMatch() {
        return mVPsPerMatch;
    }
    
    /**
     * Gets the list of top maps with individual statistics.
     * 
     * @return List of MapStats for top 3 maps
     */
    public List<MapStats> getTopMaps() {
        return topMaps;
    }
    
    /**
     * Gets the recent form (last 5 match results).
     * 
     * @return List of "W" (win) or "L" (loss) strings
     */
    public List<String> getRecentForm() {
        return recentForm;
    }
    
    /**
     * Sets the total number of matches played.
     * 
     * @param totalMatches The total match count
     */
    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }
    
    /**
     * Sets the number of matches won.
     * 
     * @param wins The win count
     */
    public void setWins(int wins) {
        this.wins = wins;
    }
    
    /**
     * Sets the number of matches lost.
     * 
     * @param losses The loss count
     */
    public void setLosses(int losses) {
        this.losses = losses;
    }
    
    /**
     * Sets the win rate as a percentage.
     * 
     * @param winRate The win rate (0.0-100.0)
     */
    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }
    
    /**
     * Sets the kill-death ratio.
     * 
     * @param killDeathRatio The K/D ratio
     */
    public void setKillDeathRatio(double killDeathRatio) {
        this.killDeathRatio = killDeathRatio;
    }
    
    /**
     * Sets the average damage per round.
     * 
     * @param averageDamageRound The average damage per round
     */
    public void setAverageDamageRound(double averageDamageRound) {
        this.averageDamageRound = averageDamageRound;
    }
    
    /**
     * Sets the headshot percentage.
     * 
     * @param headshotPercentage The headshot percentage (0.0-100.0)
     */
    public void setHeadshotPercentage(double headshotPercentage) {
        this.headshotPercentage = headshotPercentage;
    }
    
    /**
     * Sets the average MVPs per match.
     * 
     * @param mVPsPerMatch The average MVPs per match
     */
    public void setMVPsPerMatch(double mVPsPerMatch) {
        this.mVPsPerMatch = mVPsPerMatch;
    }
    
    /**
     * Sets the list of top maps with individual statistics.
     * 
     * @param topMaps List of MapStats
     */
    public void setTopMaps(List<MapStats> topMaps) {
        this.topMaps = topMaps;
    }
    
    /**
     * Sets the recent form (last 5 match results).
     * 
     * @param recentForm List of "W" or "L" strings
     */
    public void setRecentForm(List<String> recentForm) {
        this.recentForm = recentForm;
    }
    
    /**
     * Nested class representing statistics for a specific map.
     */
    public static class MapStats {
        @SerializedName("map_name")
        private String mapName;
        
        @SerializedName("matches_played")
        private int matchesPlayed;
        
        @SerializedName("win_rate")
        private double winRate;
        
        @SerializedName("kd_ratio")
        private double kdRatio;
        
        /**
         * Gets the name of the map.
         * 
         * @return The map name (e.g., "de_dust2", "de_mirage")
         */
        public String getMapName() {
            return mapName;
        }
        
        /**
         * Gets the number of matches played on this map.
         * 
         * @return The match count for this map
         */
        public int getMatchesPlayed() {
            return matchesPlayed;
        }
        
        /**
         * Gets the win rate on this map.
         * 
         * @return The win rate percentage (0.0-100.0)
         */
        public double getWinRate() {
            return winRate;
        }
        
        /**
         * Gets the K/D ratio on this map.
         * 
         * @return The K/D ratio for this map
         */
        public double getKdRatio() {
            return kdRatio;
        }
        
        /**
         * Sets the name of the map.
         * 
         * @param mapName The map name
         */
        public void setMapName(String mapName) {
            this.mapName = mapName;
        }
        
        /**
         * Sets the number of matches played on this map.
         * 
         * @param matchesPlayed The match count
         */
        public void setMatchesPlayed(int matchesPlayed) {
            this.matchesPlayed = matchesPlayed;
        }
        
        /**
         * Sets the win rate on this map.
         * 
         * @param winRate The win rate percentage
         */
        public void setWinRate(double winRate) {
            this.winRate = winRate;
        }
        
        /**
         * Sets the K/D ratio on this map.
         * 
         * @param kdRatio The K/D ratio
         */
        public void setKdRatio(double kdRatio) {
            this.kdRatio = kdRatio;
        }
    }
}
