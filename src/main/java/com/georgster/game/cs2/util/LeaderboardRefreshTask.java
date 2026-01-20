package com.georgster.game.cs2.util;

import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.cache.FaceitCache;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.util.thread.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scheduled background task for refreshing CS2 leaderboards across all guilds.
 * <p>
 * This task runs every 10 minutes (600,000 milliseconds) per guild to ensure
 * leaderboard data stays fresh without requiring user-initiated refresh commands.
 * <p>
 * The refresh process:
 * <ul>
 *   <li>Queries all linked Faceit accounts in the guild</li>
 *   <li>Fetches fresh player data from the Faceit API</li>
 *   <li>Regenerates the leaderboard with updated rankings</li>
 *   <li>Updates the cache with the new leaderboard (10-minute TTL)</li>
 * </ul>
 * <p>
 * Thread-safe: Uses ConcurrentHashMap to track per-guild refresh tasks.
 * <p>
 * Error handling: Exceptions are logged and do not crash the scheduler.
 */
public class LeaderboardRefreshTask {
    
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardRefreshTask.class);
    
    /**
     * Interval in milliseconds at which leaderboards will be refreshed (10 minutes).
     */
    public static final long REFRESH_INTERVAL = 600000; // 10 minutes
    
    /**
     * Map of guild IDs to their refresh task active status.
     */
    private static final Map<String, AtomicBoolean> ACTIVE_REFRESH_TASKS = new ConcurrentHashMap<>();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private LeaderboardRefreshTask() {
        // Utility class
    }
    
    /**
     * Starts the leaderboard refresh task for a specific guild.
     * <p>
     * If a refresh task is already running for this guild, this method does nothing.
     * <p>
     * The task will run on a separate thread using the guild's general thread pool
     * and will continue running until {@link #stopRefreshTask(String)} is called.
     * 
     * @param guildId The Discord guild ID (Snowflake)
     * @param userManager The UserProfileManager for querying guild profiles
     * @param cache The FaceitCache for storing leaderboards
     * @param apiClient The FaceitAPIClient for fetching player data
     */
    public static void startRefreshTask(
            String guildId,
            UserProfileManager userManager,
            FaceitCache cache,
            FaceitAPIClient apiClient) {
        
        // Check if task is already running for this guild
        AtomicBoolean isActive = ACTIVE_REFRESH_TASKS.computeIfAbsent(guildId, k -> new AtomicBoolean(false));
        
        if (isActive.get()) {
            logger.debug("Leaderboard refresh task already running for guild: {}", guildId);
            return;
        }
        
        isActive.set(true);
        logger.info("Starting leaderboard refresh task for guild: {}", guildId);
        
        // Schedule the refresh task
        ThreadPoolFactory.scheduleGeneralTask(guildId, () -> {
            while (isActive.get()) {
                try {
                    // Wait for refresh interval
                    Thread.sleep(REFRESH_INTERVAL);
                    
                    // Generate fresh leaderboard
                    logger.debug("Refreshing leaderboard for guild: {}", guildId);
                    ServerLeaderboardManager.generateLeaderboard(guildId, userManager, cache, apiClient);
                    logger.debug("Leaderboard refresh completed for guild: {}", guildId);
                    
                } catch (InterruptedException e) {
                    logger.warn("Leaderboard refresh task interrupted for guild: {}", guildId);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error during leaderboard refresh for guild {}: {}", guildId, e.getMessage(), e);
                    // Continue with next refresh cycle
                }
            }
            
            logger.info("Leaderboard refresh task stopped for guild: {}", guildId);
        });
    }
    
    /**
     * Stops the leaderboard refresh task for a specific guild.
     * <p>
     * The task will complete its current iteration (if running) and then stop.
     * 
     * @param guildId The Discord guild ID (Snowflake)
     */
    public static void stopRefreshTask(String guildId) {
        AtomicBoolean isActive = ACTIVE_REFRESH_TASKS.get(guildId);
        if (isActive != null && isActive.get()) {
            logger.info("Stopping leaderboard refresh task for guild: {}", guildId);
            isActive.set(false);
        }
    }
    
    /**
     * Checks if a leaderboard refresh task is currently active for a guild.
     * 
     * @param guildId The Discord guild ID (Snowflake)
     * @return true if the refresh task is active, false otherwise
     */
    public static boolean isRefreshTaskActive(String guildId) {
        AtomicBoolean isActive = ACTIVE_REFRESH_TASKS.get(guildId);
        return isActive != null && isActive.get();
    }
    
    /**
     * Stops all active leaderboard refresh tasks across all guilds.
     * <p>
     * This is typically called during bot shutdown.
     */
    public static void stopAllRefreshTasks() {
        logger.info("Stopping all leaderboard refresh tasks");
        ACTIVE_REFRESH_TASKS.values().forEach(active -> active.set(false));
        ACTIVE_REFRESH_TASKS.clear();
    }
}
