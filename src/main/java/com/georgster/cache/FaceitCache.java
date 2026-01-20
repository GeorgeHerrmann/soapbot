package com.georgster.cache;

import com.georgster.api.faceit.model.MatchDetails;
import com.georgster.api.faceit.model.PlayerStats;
import com.georgster.api.faceit.model.ServerLeaderboard;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe caching layer for Faceit API data using Caffeine cache.
 * <p>
 * Provides in-memory caching with TTL-based expiration to minimize API calls
 * and improve response times. Cache keys are guild-isolated to maintain proper
 * data separation across Discord servers.
 * <p>
 * Cache TTL Configuration:
 * - Player stats: 5 minutes
 * - Match reports: 5 minutes
 * - Match history: 5 minutes
 * - Server leaderboards: 10 minutes
 * <p>
 * Thread-safe: Supports concurrent access from multiple Discord command executions.
 * <p>
 * Memory Management: Uses LRU eviction policy with maximum 10,000 cached entries
 * to prevent unbounded memory growth.
 */
public class FaceitCache {
    
    private static final Logger logger = LoggerFactory.getLogger(FaceitCache.class);
    
    // Cache instances with different TTLs
    private final Cache<String, PlayerStats> statsCache;
    private final Cache<String, MatchDetails> matchCache;
    private final Cache<String, List<MatchDetails>> historyCache;
    private final Cache<String, ServerLeaderboard> leaderboardCache;
    
    // Cache TTL constants (in minutes)
    private static final int STATS_TTL = 5;
    private static final int MATCH_TTL = 5;
    private static final int HISTORY_TTL = 5;
    private static final int LEADERBOARD_TTL = 10;
    
    // Maximum cache size to prevent memory overflow
    private static final int MAX_CACHE_SIZE = 10000;
    
    /**
     * Creates a new FaceitCache instance with Caffeine-backed caches.
     * <p>
     * Initializes separate caches for different data types with appropriate TTLs.
     */
    public FaceitCache() {
        this.statsCache = Caffeine.newBuilder()
                .expireAfterWrite(STATS_TTL, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .build();
        
        this.matchCache = Caffeine.newBuilder()
                .expireAfterWrite(MATCH_TTL, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .build();
        
        this.historyCache = Caffeine.newBuilder()
                .expireAfterWrite(HISTORY_TTL, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .build();
        
        this.leaderboardCache = Caffeine.newBuilder()
                .expireAfterWrite(LEADERBOARD_TTL, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .build();
        
    }
    
    // ===== Player Stats Cache =====
    
    /**
     * Retrieves cached player statistics.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     * @return PlayerStats object if cached, null otherwise
     */
    public PlayerStats getPlayerStats(String guildId, String playerId) {
        String key = buildStatsKey(guildId, playerId);
        PlayerStats stats = statsCache.getIfPresent(key);
        if (stats != null) {
            logger.debug("Cache HIT: Player stats for {} in guild {}", playerId, guildId);
        } else {
            logger.debug("Cache MISS: Player stats for {} in guild {}", playerId, guildId);
        }
        return stats;
    }
    
    /**
     * Stores player statistics in cache.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     * @param stats The PlayerStats object to cache
     */
    public void putPlayerStats(String guildId, String playerId, PlayerStats stats) {
        String key = buildStatsKey(guildId, playerId);
        statsCache.put(key, stats);
        logger.debug("Cached player stats for {} in guild {}", playerId, guildId);
    }
    
    /**
     * Invalidates cached player statistics.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     */
    public void invalidatePlayerStats(String guildId, String playerId) {
        String key = buildStatsKey(guildId, playerId);
        statsCache.invalidate(key);
        logger.debug("Invalidated player stats cache for {} in guild {}", playerId, guildId);
    }
    
    private String buildStatsKey(String guildId, String playerId) {
        return guildId + ":" + playerId + ":stats";
    }
    
    // ===== Match Report Cache =====
    
    /**
     * Retrieves cached match report (last match).
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     * @return MatchDetails object if cached, null otherwise
     */
    public MatchDetails getMatchReport(String guildId, String playerId) {
        String key = buildMatchKey(guildId, playerId);
        MatchDetails match = matchCache.getIfPresent(key);
        if (match != null) {
            logger.debug("Cache HIT: Match report for {} in guild {}", playerId, guildId);
        } else {
            logger.debug("Cache MISS: Match report for {} in guild {}", playerId, guildId);
        }
        return match;
    }
    
    /**
     * Stores match report in cache.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     * @param match The MatchDetails object to cache
     */
    public void putMatchReport(String guildId, String playerId, MatchDetails match) {
        String key = buildMatchKey(guildId, playerId);
        matchCache.put(key, match);
        logger.debug("Cached match report for {} in guild {}", playerId, guildId);
    }
    
    /**
     * Invalidates cached match report.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     */
    public void invalidateMatchReport(String guildId, String playerId) {
        String key = buildMatchKey(guildId, playerId);
        matchCache.invalidate(key);
        logger.debug("Invalidated match report cache for {} in guild {}", playerId, guildId);
    }
    
    private String buildMatchKey(String guildId, String playerId) {
        return guildId + ":" + playerId + ":match";
    }
    
    // ===== Match History Cache =====
    
    /**
     * Retrieves cached match history.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     * @return List of MatchDetails objects if cached, null otherwise
     */
    public List<MatchDetails> getMatchHistory(String guildId, String playerId) {
        String key = buildHistoryKey(guildId, playerId);
        List<MatchDetails> history = historyCache.getIfPresent(key);
        if (history != null) {
            logger.debug("Cache HIT: Match history for {} in guild {}", playerId, guildId);
        } else {
            logger.debug("Cache MISS: Match history for {} in guild {}", playerId, guildId);
        }
        return history;
    }
    
    /**
     * Stores match history in cache.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     * @param history List of MatchDetails objects to cache
     */
    public void putMatchHistory(String guildId, String playerId, List<MatchDetails> history) {
        String key = buildHistoryKey(guildId, playerId);
        historyCache.put(key, history);
        logger.debug("Cached match history ({} matches) for {} in guild {}", 
                    history.size(), playerId, guildId);
    }
    
    /**
     * Invalidates cached match history.
     * 
     * @param guildId The Discord guild ID
     * @param playerId The Faceit player ID
     */
    public void invalidateMatchHistory(String guildId, String playerId) {
        String key = buildHistoryKey(guildId, playerId);
        historyCache.invalidate(key);
        logger.debug("Invalidated match history cache for {} in guild {}", playerId, guildId);
    }
    
    private String buildHistoryKey(String guildId, String playerId) {
        return guildId + ":" + playerId + ":history";
    }
    
    // ===== Server Leaderboard Cache =====
    
    /**
     * Retrieves cached server leaderboard.
     * 
     * @param guildId The Discord guild ID
     * @return ServerLeaderboard object if cached, null otherwise
     */
    public ServerLeaderboard getLeaderboard(String guildId) {
        String key = buildLeaderboardKey(guildId);
        ServerLeaderboard leaderboard = leaderboardCache.getIfPresent(key);
        if (leaderboard != null) {
            logger.debug("Cache HIT: Leaderboard for guild {}", guildId);
        } else {
            logger.debug("Cache MISS: Leaderboard for guild {}", guildId);
        }
        return leaderboard;
    }
    
    /**
     * Stores server leaderboard in cache.
     * 
     * @param guildId The Discord guild ID
     * @param leaderboard The ServerLeaderboard object to cache
     */
    public void putLeaderboard(String guildId, ServerLeaderboard leaderboard) {
        String key = buildLeaderboardKey(guildId);
        leaderboardCache.put(key, leaderboard);
        logger.debug("Cached leaderboard for guild {} ({} entries)", guildId, leaderboard.size());
    }
    
    /**
     * Invalidates cached server leaderboard.
     * 
     * @param guildId The Discord guild ID
     */
    public void invalidateLeaderboard(String guildId) {
        String key = buildLeaderboardKey(guildId);
        leaderboardCache.invalidate(key);
        logger.debug("Invalidated leaderboard cache for guild {}", guildId);
    }
    
    private String buildLeaderboardKey(String guildId) {
        return guildId + ":leaderboard";
    }
    
    // ===== Cache Management =====
    
    /**
     * Clears all cached data for a specific guild.
     * <p>
     * Useful when a server is leaving or during administrative operations.
     * 
     * @param guildId The Discord guild ID
     */
    public void clearGuildCache(String guildId) {
        // Invalidate all keys starting with guildId prefix
        statsCache.asMap().keySet().removeIf(key -> key.startsWith(guildId + ":"));
        matchCache.asMap().keySet().removeIf(key -> key.startsWith(guildId + ":"));
        historyCache.asMap().keySet().removeIf(key -> key.startsWith(guildId + ":"));
        leaderboardCache.invalidate(buildLeaderboardKey(guildId));
        

    }
    
    /**
     * Clears all cached data across all guilds.
     * <p>
     * Use sparingly; primarily for maintenance or debugging.
     */
    public void clearAllCaches() {
        statsCache.invalidateAll();
        matchCache.invalidateAll();
        historyCache.invalidateAll();
        leaderboardCache.invalidateAll();
        
        logger.warn("Cleared all caches (stats, matches, history, leaderboards)");
    }
    
    /**
     * Gets cache statistics for monitoring and debugging.
     * 
     * @return String with cache hit rates and sizes
     */
    public String getCacheStats() {
        return String.format(
            "FaceitCache Stats | Stats: %d entries (%.1f%% hit rate) | " +
            "Matches: %d entries (%.1f%% hit rate) | " +
            "History: %d entries (%.1f%% hit rate) | " +
            "Leaderboards: %d entries (%.1f%% hit rate)",
            statsCache.estimatedSize(), calculateHitRate(statsCache.stats()),
            matchCache.estimatedSize(), calculateHitRate(matchCache.stats()),
            historyCache.estimatedSize(), calculateHitRate(historyCache.stats()),
            leaderboardCache.estimatedSize(), calculateHitRate(leaderboardCache.stats())
        );
    }
    
    private double calculateHitRate(com.github.benmanes.caffeine.cache.stats.CacheStats stats) {
        long hits = stats.hitCount();
        long misses = stats.missCount();
        long total = hits + misses;
        return total > 0 ? (hits * 100.0 / total) : 0.0;
    }
}
