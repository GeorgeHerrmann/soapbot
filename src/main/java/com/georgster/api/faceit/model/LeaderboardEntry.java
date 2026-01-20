package com.georgster.api.faceit.model;

/**
 * Data Transfer Object representing a single entry in a server's CS2 leaderboard.
 * <p>
 * Contains player ranking information including position, nickname, Faceit level,
 * Elo rating, K/D ratio, and Discord mention ID for linking to Discord members.
 */
public class LeaderboardEntry {
    
    private int rank;
    private String faceitNickname;
    private int faceitLevel;
    private int elo;
    private double killDeathRatio;
    private String discordMentionId;
    
    /**
     * Creates a new LeaderboardEntry instance.
     * 
     * @param rank The player's rank position on the leaderboard
     * @param faceitNickname The player's Faceit nickname
     * @param faceitLevel The player's Faceit level (1-10)
     * @param elo The player's Elo rating
     * @param killDeathRatio The player's K/D ratio
     * @param discordMentionId The Discord user ID for mentions
     */
    public LeaderboardEntry(int rank, String faceitNickname, int faceitLevel, int elo, 
                           double killDeathRatio, String discordMentionId) {
        this.rank = rank;
        this.faceitNickname = faceitNickname;
        this.faceitLevel = faceitLevel;
        this.elo = elo;
        this.killDeathRatio = killDeathRatio;
        this.discordMentionId = discordMentionId;
    }
    
    /**
     * Gets the player's rank position on the leaderboard.
     * 
     * @return The rank (1-based)
     */
    public int getRank() {
        return rank;
    }
    
    /**
     * Gets the player's Faceit nickname.
     * 
     * @return The Faceit nickname
     */
    public String getFaceitNickname() {
        return faceitNickname;
    }
    
    /**
     * Gets the player's Faceit level.
     * 
     * @return The Faceit level (1-10)
     */
    public int getFaceitLevel() {
        return faceitLevel;
    }
    
    /**
     * Gets the player's Elo rating.
     * 
     * @return The Elo rating
     */
    public int getElo() {
        return elo;
    }
    
    /**
     * Gets the player's K/D ratio.
     * 
     * @return The kill-death ratio
     */
    public double getKillDeathRatio() {
        return killDeathRatio;
    }
    
    /**
     * Gets the Discord user ID for mentions.
     * 
     * @return The Discord user ID (Snowflake)
     */
    public String getDiscordMentionId() {
        return discordMentionId;
    }
    
    /**
     * Sets the player's rank position on the leaderboard.
     * 
     * @param rank The rank (1-based)
     */
    public void setRank(int rank) {
        this.rank = rank;
    }
    
    /**
     * Sets the player's Faceit nickname.
     * 
     * @param faceitNickname The Faceit nickname
     */
    public void setFaceitNickname(String faceitNickname) {
        this.faceitNickname = faceitNickname;
    }
    
    /**
     * Sets the player's Faceit level.
     * 
     * @param faceitLevel The Faceit level (1-10)
     */
    public void setFaceitLevel(int faceitLevel) {
        this.faceitLevel = faceitLevel;
    }
    
    /**
     * Sets the player's Elo rating.
     * 
     * @param elo The Elo rating
     */
    public void setElo(int elo) {
        this.elo = elo;
    }
    
    /**
     * Sets the player's K/D ratio.
     * 
     * @param killDeathRatio The kill-death ratio
     */
    public void setKillDeathRatio(double killDeathRatio) {
        this.killDeathRatio = killDeathRatio;
    }
    
    /**
     * Sets the Discord user ID for mentions.
     * 
     * @param discordMentionId The Discord user ID (Snowflake)
     */
    public void setDiscordMentionId(String discordMentionId) {
        this.discordMentionId = discordMentionId;
    }
}
