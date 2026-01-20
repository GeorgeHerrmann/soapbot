package com.georgster.api.faceit;

import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.exception.PlayerNotFoundException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.api.faceit.model.MatchDetails;
import com.georgster.api.faceit.model.PlayerStats;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with the Faceit API.
 * <p>
 * Provides methods to fetch player profiles, statistics, match history, and team information
 * from the Faceit platform. Handles authentication, error responses, rate limiting, and
 * JSON deserialization.
 * <p>
 * Thread-safe: Uses connection pooling for concurrent requests.
 * <p>
 * Configuration: Reads API key from FACEIT_API_KEY environment variable or faceit_api_key.txt file.
 * <p>
 * Error Handling: Returns standardized messages per FR-011 requirement.
 */
public class FaceitAPIClient {
    
    private static final Logger logger = LoggerFactory.getLogger(FaceitAPIClient.class);
    private static final String BASE_URL = "https://open.faceit.com/data/v4";
    private static final String CS2_GAME_ID = "cs2";
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String apiKey;
    
    /**
     * Creates a new FaceitAPIClient instance.
     * <p>
     * Initializes OkHttp3 client with connection pooling (max 10 connections),
     * 30-second timeout, and reads API key from environment or file.
     * 
     * @throws FaceitAPIException if API key cannot be loaded
     */
    public FaceitAPIClient() throws FaceitAPIException {
        this.apiKey = loadApiKey();
        this.gson = new Gson();
        
        // Configure OkHttp3 with connection pooling and timeouts
        ConnectionPool connectionPool = new ConnectionPool(10, 5, TimeUnit.MINUTES);
        this.client = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        logger.info("FaceitAPIClient initialized with connection pool (max 10 connections)");
    }
    
    /**
     * Loads the Faceit API key from environment variable or file.
     * 
     * @return The API key
     * @throws FaceitAPIException if API key cannot be found
     */
    private String loadApiKey() throws FaceitAPIException {
        // Try environment variable first
        String key = System.getenv("FACEIT_API_KEY");
        if (key != null && !key.isEmpty()) {
            logger.info("Loaded Faceit API key from environment variable");
            return key;
        }
        
        // Fallback to file
        try {
            Path keyPath = Path.of(System.getProperty("user.dir"), "faceit_api_key.txt");
            if (Files.exists(keyPath)) {
                key = Files.readString(keyPath).trim();
                logger.info("Loaded Faceit API key from file: faceit_api_key.txt");
                return key;
            }
        } catch (IOException e) {
            throw new FaceitAPIException("Failed to read Faceit API key from file", e);
        }
        
        throw new FaceitAPIException("Faceit API key not found. Set FACEIT_API_KEY environment variable or create faceit_api_key.txt");
    }
    
    /**
     * Fetches a player profile by nickname.
     * 
     * @param nickname The player's Faceit nickname
     * @return FaceitPlayer object with player data
     * @throws PlayerNotFoundException if player not found
     * @throws FaceitAPIException if API error occurs
     */
    public FaceitPlayer fetchPlayer(String nickname) throws FaceitAPIException {
        String url = BASE_URL + "/players?nickname=" + nickname + "&game=" + CS2_GAME_ID;
        
        try {
            String responseBody = executeRequest(url);
            FaceitPlayer player = gson.fromJson(responseBody, FaceitPlayer.class);
            
            if (player == null || player.getPlayerId() == null) {
                throw new PlayerNotFoundException("Player '" + nickname + "' not found on Faceit");
            }
            
            logger.debug("Fetched player: {} (ID: {})", player.getNickname(), player.getPlayerId());
            return player;
            
        } catch (PlayerNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceitAPIException("Failed to fetch player: " + nickname, e);
        }
    }
    
    /**
     * Fetches detailed statistics for a player.
     * 
     * @param playerId The player's Faceit ID
     * @return PlayerStats object with lifetime statistics
     * @throws FaceitAPIException if API error occurs
     */
    public PlayerStats fetchPlayerStats(String playerId) throws FaceitAPIException {
        String url = BASE_URL + "/players/" + playerId + "/stats/" + CS2_GAME_ID;
        
        try {
            String responseBody = executeRequest(url);
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            
            // Parse lifetime stats from nested JSON structure
            JsonObject lifetime = jsonObject.getAsJsonObject("lifetime");
            if (lifetime == null) {
                // Player has no CS2 stats yet
                return createEmptyStats();
            }
            
            PlayerStats stats = parsePlayerStats(lifetime);
            logger.debug("Fetched stats for player ID: {}", playerId);
            return stats;
            
        } catch (Exception e) {
            throw new FaceitAPIException("Failed to fetch player stats for ID: " + playerId, e);
        }
    }
    
    /**
     * Fetches the most recent match for a player.
     * 
     * @param playerId The player's Faceit ID
     * @return MatchDetails object with match statistics, or null if no matches
     * @throws FaceitAPIException if API error occurs
     */
    public MatchDetails fetchLastMatch(String playerId) throws FaceitAPIException {
        List<MatchDetails> matches = fetchMatchHistory(playerId, 1);
        return matches.isEmpty() ? null : matches.get(0);
    }
    
    /**
     * Fetches match history for a player.
     * 
     * @param playerId The player's Faceit ID
     * @param limit Maximum number of matches to fetch (1-100)
     * @return List of MatchDetails objects, empty if no matches
     * @throws FaceitAPIException if API error occurs
     */
    public List<MatchDetails> fetchMatchHistory(String playerId, int limit) throws FaceitAPIException {
        String url = BASE_URL + "/players/" + playerId + "/history?game=" + CS2_GAME_ID + "&offset=0&limit=" + Math.min(limit, 100);
        
        try {
            String responseBody = executeRequest(url);
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonArray items = jsonObject.getAsJsonArray("items");
            
            List<MatchDetails> matches = new ArrayList<>();
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    JsonObject matchJson = items.get(i).getAsJsonObject();
                    MatchDetails match = parseMatchDetails(matchJson, playerId);
                    if (match != null) {
                        matches.add(match);
                    }
                }
            }
            
            logger.debug("Fetched {} matches for player ID: {}", matches.size(), playerId);
            return matches;
            
        } catch (Exception e) {
            throw new FaceitAPIException("Failed to fetch match history for ID: " + playerId, e);
        }
    }
    
    /**
     * Executes an HTTP GET request to the Faceit API.
     * 
     * @param url The full API URL
     * @return Response body as string
     * @throws FaceitAPIException if request fails
     */
    private String executeRequest(String url) throws FaceitAPIException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new FaceitAPIException("Empty response from Faceit API");
            }
            
            return body.string();
            
        } catch (IOException e) {
            throw new FaceitAPIException("Network error communicating with Faceit API", e);
        }
    }
    
    /**
     * Handles HTTP error responses from Faceit API.
     * 
     * @param response The HTTP response
     * @throws FaceitAPIException with standardized error message
     */
    private void handleErrorResponse(Response response) throws FaceitAPIException {
        int code = response.code();
        
        switch (code) {
            case 404:
                throw new PlayerNotFoundException("Resource not found on Faceit");
            case 429:
                logger.warn("Faceit API rate limit exceeded");
                throw new FaceitAPIException("Service temporarily unavailable (rate limited)");
            case 500:
            case 502:
            case 503:
            case 504:
                logger.error("Faceit API server error: {}", code);
                throw new FaceitAPIException("Service temporarily unavailable (server error)");
            default:
                logger.error("Faceit API returned error: {}", code);
                throw new FaceitAPIException("Service temporarily unavailable (error " + code + ")");
        }
    }
    
    /**
     * Parses PlayerStats from Faceit API lifetime JSON object.
     * 
     * @param lifetime JsonObject containing lifetime stats
     * @return PlayerStats object
     */
    private PlayerStats parsePlayerStats(JsonObject lifetime) {
        PlayerStats stats = new PlayerStats();
        
        // Extract basic stats
        stats.setTotalMatches(getIntOrZero(lifetime, "Matches"));
        stats.setWins(getIntOrZero(lifetime, "Wins"));
        stats.setLosses(getIntOrZero(lifetime, "Matches") - getIntOrZero(lifetime, "Wins"));
        
        double winRate = stats.getTotalMatches() > 0 
            ? (stats.getWins() * 100.0 / stats.getTotalMatches()) 
            : 0.0;
        stats.setWinRate(winRate);
        
        stats.setKillDeathRatio(getDoubleOrZero(lifetime, "Average K/D Ratio"));
        stats.setAverageDamageRound(getDoubleOrZero(lifetime, "Average ADR"));
        stats.setHeadshotPercentage(getDoubleOrZero(lifetime, "Average Headshots %"));
        stats.setMVPsPerMatch(getDoubleOrZero(lifetime, "Average MVPs"));
        
        // Recent form would need to be fetched from match history
        stats.setRecentForm(new ArrayList<>());
        stats.setTopMaps(new ArrayList<>());
        
        return stats;
    }
    
    /**
     * Parses MatchDetails from Faceit API match JSON object.
     * 
     * @param matchJson JsonObject containing match data
     * @param playerId The player ID to extract stats for
     * @return MatchDetails object, or null if parsing fails
     */
    private MatchDetails parseMatchDetails(JsonObject matchJson, String playerId) {
        try {
            MatchDetails match = new MatchDetails();
            
            match.setMatchId(getString(matchJson, "match_id"));
            match.setPlayerId(playerId);
            match.setMatchTimestamp(getLong(matchJson, "started_at"));
            
            // Extract match details from nested structure
            JsonObject stats = matchJson.getAsJsonObject("stats");
            if (stats != null) {
                match.setKills(getIntOrZero(stats, "Kills"));
                match.setDeaths(getIntOrZero(stats, "Deaths"));
                match.setAssists(getIntOrZero(stats, "Assists"));
                match.setAverageDamageRound(getDoubleOrZero(stats, "ADR"));
                match.setHeadshotPercentage(getDoubleOrZero(stats, "Headshots %"));
                match.setMvps(getIntOrZero(stats, "MVPs"));
            }
            
            // Extract map and score
            match.setMapName(getString(matchJson, "map"));
            match.setScore(getString(matchJson, "score"));
            match.setResult(getString(matchJson, "result")); // "win" or "loss"
            match.setRoundsPlayed(getIntOrZero(matchJson, "rounds"));
            
            match.setTeamRoster(new ArrayList<>());
            
            return match;
            
        } catch (Exception e) {
            logger.warn("Failed to parse match details: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates an empty PlayerStats object for players with no CS2 matches.
     * 
     * @return Empty PlayerStats object
     */
    private PlayerStats createEmptyStats() {
        PlayerStats stats = new PlayerStats();
        stats.setTotalMatches(0);
        stats.setWins(0);
        stats.setLosses(0);
        stats.setWinRate(0.0);
        stats.setKillDeathRatio(0.0);
        stats.setAverageDamageRound(0.0);
        stats.setHeadshotPercentage(0.0);
        stats.setMVPsPerMatch(0.0);
        stats.setTopMaps(new ArrayList<>());
        stats.setRecentForm(new ArrayList<>());
        return stats;
    }
    
    // Helper methods for safe JSON parsing
    
    private int getIntOrZero(JsonObject obj, String key) {
        try {
            return obj.has(key) ? obj.get(key).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private double getDoubleOrZero(JsonObject obj, String key) {
        try {
            return obj.has(key) ? obj.get(key).getAsDouble() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private long getLong(JsonObject obj, String key) {
        try {
            return obj.has(key) ? obj.get(key).getAsLong() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    private String getString(JsonObject obj, String key) {
        try {
            return obj.has(key) ? obj.get(key).getAsString() : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Closes the HTTP client and releases resources.
     * Should be called during application shutdown.
     */
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        logger.info("FaceitAPIClient shutdown complete");
    }
}
