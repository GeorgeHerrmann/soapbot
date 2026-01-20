package com.georgster.api.faceit.model;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Data Transfer Object representing a Discord guild's CS2 leaderboard.
 * <p>
 * Contains the guild ID, list of ranked entries, server average Elo,
 * and timestamps for generation and refresh tracking.
 * <p>
 * This class is used for caching leaderboard data with a 10-minute TTL.
 */
public class ServerLeaderboard {
    
    private String guildId;
    private List<LeaderboardEntry> entries;
    private double serverAverageElo;
    private Instant generatedAt;
    private Instant refreshedAt;
    
    /**
     * Creates a new ServerLeaderboard instance.
     * 
     * @param guildId The Discord guild ID (Snowflake)
     */
    public ServerLeaderboard(String guildId) {
        this.guildId = guildId;
        this.entries = new ArrayList<>();
        this.generatedAt = Instant.now();
        this.refreshedAt = Instant.now();
    }
    
    /**
     * Creates a new ServerLeaderboard instance with entries.
     * 
     * @param guildId The Discord guild ID (Snowflake)
     * @param entries The list of leaderboard entries
     * @param serverAverageElo The server's average Elo rating
     */
    public ServerLeaderboard(String guildId, List<LeaderboardEntry> entries, double serverAverageElo) {
        this.guildId = guildId;
        this.entries = entries;
        this.serverAverageElo = serverAverageElo;
        this.generatedAt = Instant.now();
        this.refreshedAt = Instant.now();
    }
    
    /**
     * Gets the Discord guild ID.
     * 
     * @return The guild ID (Snowflake)
     */
    public String getGuildId() {
        return guildId;
    }
    
    /**
     * Gets the list of leaderboard entries.
     * 
     * @return List of LeaderboardEntry objects ranked by Elo
     */
    public List<LeaderboardEntry> getEntries() {
        return entries;
    }
    
    /**
     * Gets the server's average Elo rating across all linked players.
     * 
     * @return The average Elo rating
     */
    public double getServerAverageElo() {
        return serverAverageElo;
    }
    
    /**
     * Gets the timestamp when this leaderboard was first generated.
     * 
     * @return The generation timestamp
     */
    public Instant getGeneratedAt() {
        return generatedAt;
    }
    
    /**
     * Gets the timestamp when this leaderboard was last refreshed.
     * 
     * @return The refresh timestamp
     */
    public Instant getRefreshedAt() {
        return refreshedAt;
    }
    
    /**
     * Sets the Discord guild ID.
     * 
     * @param guildId The guild ID (Snowflake)
     */
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
    
    /**
     * Sets the list of leaderboard entries.
     * 
     * @param entries List of LeaderboardEntry objects
     */
    public void setEntries(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }
    
    /**
     * Sets the server's average Elo rating.
     * 
     * @param serverAverageElo The average Elo rating
     */
    public void setServerAverageElo(double serverAverageElo) {
        this.serverAverageElo = serverAverageElo;
    }
    
    /**
     * Sets the generation timestamp.
     * 
     * @param generatedAt The generation timestamp
     */
    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    /**
     * Sets the refresh timestamp.
     * 
     * @param refreshedAt The refresh timestamp
     */
    public void setRefreshedAt(Instant refreshedAt) {
        this.refreshedAt = refreshedAt;
    }
    
    /**
     * Updates the refresh timestamp to the current time.
     */
    public void markRefreshed() {
        this.refreshedAt = Instant.now();
    }
    
    /**
     * Adds a leaderboard entry to the list.
     * 
     * @param entry The LeaderboardEntry to add
     */
    public void addEntry(LeaderboardEntry entry) {
        this.entries.add(entry);
    }
    
    /**
     * Checks if the leaderboard is empty (no linked players).
     * 
     * @return true if no entries exist, false otherwise
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    /**
     * Gets the number of linked players on the leaderboard.
     * 
     * @return The entry count
     */
    public int size() {
        return entries.size();
    }
}
