package com.georgster.game.cs2.util;

import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.api.faceit.model.LeaderboardEntry;
import com.georgster.api.faceit.model.PlayerStats;
import com.georgster.api.faceit.model.ServerLeaderboard;
import com.georgster.cache.FaceitCache;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for generating server leaderboards from linked Faceit accounts.
 * <p>
 * This manager queries all user profiles in a guild, fetches their Faceit statistics,
 * and creates a ranked leaderboard sorted by Elo rating.
 * <p>
 * Thread-safe: Can be called from multiple command executions concurrently.
 */
public class ServerLeaderboardManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerLeaderboardManager.class);
    private static final int TOP_N_PLAYERS = 10;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ServerLeaderboardManager() {
        // Utility class
    }
    
    /**
     * Generates a leaderboard for a Discord guild by querying all linked Faceit accounts.
     * <p>
     * This method:
     * <ul>
     *   <li>Queries all UserProfiles in the guild with linked CS2Profiles</li>
     *   <li>Fetches fresh Faceit player data and statistics for each linked account</li>
     *   <li>Sorts players by Elo rating (descending)</li>
     *   <li>Takes the top 10 players</li>
     *   <li>Calculates the server average Elo</li>
     *   <li>Stores the leaderboard in cache with 10-minute TTL</li>
     * </ul>
     * 
     * @param guildId The Discord guild ID (Snowflake)
     * @param userManager The UserProfileManager for querying guild profiles
     * @param cache The FaceitCache for storing the leaderboard
     * @param apiClient The FaceitAPIClient for fetching player data
     * @return ServerLeaderboard object with ranked entries
     */
    public static ServerLeaderboard generateLeaderboard(
            String guildId, 
            UserProfileManager userManager, 
            FaceitCache cache,
            FaceitAPIClient apiClient) {
        
        logger.info("Generating leaderboard for guild: {}", guildId);
        
        // Query all linked profiles in the guild
        List<UserProfile> linkedProfiles = userManager.getAll().stream()
                .filter(UserProfile::hasLinkedFaceit)
                .collect(Collectors.toList());
        
        if (linkedProfiles.isEmpty()) {
            logger.info("No linked Faceit accounts found for guild: {}", guildId);
            return new ServerLeaderboard(guildId);
        }
        
        logger.debug("Found {} linked accounts in guild {}", linkedProfiles.size(), guildId);
        
        // Fetch player data and stats for each linked profile
        List<LeaderboardEntry> entries = new ArrayList<>();
        double totalElo = 0;
        int validEntries = 0;
        
        for (UserProfile profile : linkedProfiles) {
            try {
                UserProfile.CS2Profile cs2Profile = profile.getCS2Profile();
                String playerId = cs2Profile.getFaceitPlayerId();
                
                // Fetch player data
                FaceitPlayer player = apiClient.fetchPlayerById(playerId);
                
                // Fetch player stats
                PlayerStats stats = apiClient.fetchPlayerStats(playerId);
                
                // Create leaderboard entry
                LeaderboardEntry entry = new LeaderboardEntry(
                    0, // Rank will be assigned after sorting
                    player.getNickname(),
                    player.getFaceitLevel(),
                    player.getElo(),
                    stats.getKillDeathRatio(),
                    profile.getMemberId()
                );
                
                entries.add(entry);
                totalElo += player.getElo();
                validEntries++;
                
                logger.debug("Added leaderboard entry: {} (Elo: {})", player.getNickname(), player.getElo());
                
            } catch (FaceitAPIException e) {
                logger.warn("Failed to fetch data for player {} in guild {}: {}", 
                    profile.getCS2Profile().getFaceitNickname(), guildId, e.getMessage());
                // Skip this player and continue with others
            }
        }
        
        if (entries.isEmpty()) {
            logger.warn("No valid leaderboard entries for guild: {}", guildId);
            return new ServerLeaderboard(guildId);
        }
        
        // Sort by Elo descending
        entries.sort(Comparator.comparingInt(LeaderboardEntry::getElo).reversed());
        
        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        
        // Take top 10
        List<LeaderboardEntry> topEntries = entries.stream()
                .limit(TOP_N_PLAYERS)
                .collect(Collectors.toList());
        
        // Calculate server average Elo
        double serverAverageElo = validEntries > 0 ? totalElo / validEntries : 0;
        
        // Create leaderboard
        ServerLeaderboard leaderboard = new ServerLeaderboard(guildId, topEntries, serverAverageElo);
        
        // Store in cache
        cache.putLeaderboard(guildId, leaderboard);
        
        logger.info("Generated leaderboard for guild {} with {} entries (avg Elo: {:.0f})", 
            guildId, topEntries.size(), serverAverageElo);
        
        return leaderboard;
    }
}
