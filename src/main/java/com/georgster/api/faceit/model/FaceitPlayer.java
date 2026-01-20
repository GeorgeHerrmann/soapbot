package com.georgster.api.faceit.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object representing a Faceit player profile.
 * <p>
 * Contains essential player information including ID, nickname, avatar, country,
 * Faceit level, Elo rating, and Steam ID.
 * <p>
 * This class is designed for deserialization from Faceit API JSON responses using Gson.
 */
public class FaceitPlayer {
    
    @SerializedName("player_id")
    private String playerId;
    
    @SerializedName("nickname")
    private String nickname;
    
    @SerializedName("avatar")
    private String avatar;
    
    @SerializedName("country")
    private String country;
    
    @SerializedName("games")
    private Games games;
    
    @SerializedName("steam_id_64")
    private String steamId;
    
    /**
     * Gets the unique Faceit player ID.
     * 
     * @return The player's Faceit ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Gets the player's Faceit nickname.
     * 
     * @return The player's nickname
     */
    public String getNickname() {
        return nickname;
    }
    
    /**
     * Gets the URL to the player's avatar image.
     * 
     * @return The avatar URL, or null if not set
     */
    public String getAvatar() {
        return avatar;
    }
    
    /**
     * Gets the player's country code (e.g., "US", "GB").
     * 
     * @return The country code
     */
    public String getCountry() {
        return country;
    }
    
    /**
     * Gets the player's Faceit level for CS2.
     * 
     * @return The Faceit level (1-10)
     */
    public int getFaceitLevel() {
        return games != null && games.cs2 != null ? games.cs2.skillLevel : 0;
    }
    
    /**
     * Gets the player's Elo rating for CS2.
     * 
     * @return The Elo rating
     */
    public int getElo() {
        return games != null && games.cs2 != null ? games.cs2.faceitElo : 0;
    }
    
    /**
     * Gets the player's Steam ID (64-bit).
     * 
     * @return The Steam ID, or null if not linked
     */
    public String getSteamId() {
        return steamId;
    }
    
    /**
     * Nested class to hold game-specific data.
     */
    private static class Games {
        @SerializedName("cs2")
        private CS2Game cs2;
    }
    
    /**
     * Nested class to hold CS2-specific game data.
     */
    private static class CS2Game {
        @SerializedName("skill_level")
        private int skillLevel;
        
        @SerializedName("faceit_elo")
        private int faceitElo;
    }
}
