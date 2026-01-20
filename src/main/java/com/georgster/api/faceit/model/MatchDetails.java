package com.georgster.api.faceit.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * Data Transfer Object representing detailed statistics from a single CS2 match on Faceit.
 * <p>
 * Contains match-specific information including match ID, player performance metrics,
 * map details, team roster, and match outcome.
 * <p>
 * This class is designed for deserialization from Faceit API JSON responses using Gson.
 */
public class MatchDetails {
    
    @SerializedName("match_id")
    private String matchId;
    
    @SerializedName("player_id")
    private String playerId;
    
    @SerializedName("map_name")
    private String mapName;
    
    @SerializedName("score")
    private String score; // e.g., "16-10"
    
    @SerializedName("kills")
    private int kills;
    
    @SerializedName("deaths")
    private int deaths;
    
    @SerializedName("assists")
    private int assists;
    
    @SerializedName("average_damage_round")
    private double averageDamageRound;
    
    @SerializedName("headshot_percentage")
    private double headshotPercentage;
    
    @SerializedName("mvps")
    private int mvps;
    
    @SerializedName("result")
    private String result; // "win" or "loss"
    
    @SerializedName("match_timestamp")
    private long matchTimestamp; // Unix timestamp
    
    @SerializedName("team_roster")
    private List<String> teamRoster; // List of player nicknames on the team
    
    @SerializedName("rounds_played")
    private int roundsPlayed;
    
    /**
     * Creates a new MatchDetails instance.
     */
    public MatchDetails() {
        this.teamRoster = new ArrayList<>();
    }
    
    /**
     * Gets the unique match ID.
     * 
     * @return The Faceit match ID
     */
    public String getMatchId() {
        return matchId;
    }
    
    /**
     * Gets the player ID for this match data.
     * 
     * @return The player's Faceit ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Gets the map name where the match was played.
     * 
     * @return The map name (e.g., "de_dust2")
     */
    public String getMapName() {
        return mapName;
    }
    
    /**
     * Gets the match score.
     * 
     * @return The score in "team1-team2" format (e.g., "16-10")
     */
    public String getScore() {
        return score;
    }
    
    /**
     * Gets the number of kills the player achieved.
     * 
     * @return The kill count
     */
    public int getKills() {
        return kills;
    }
    
    /**
     * Gets the number of deaths the player suffered.
     * 
     * @return The death count
     */
    public int getDeaths() {
        return deaths;
    }
    
    /**
     * Gets the number of assists the player achieved.
     * 
     * @return The assist count
     */
    public int getAssists() {
        return assists;
    }
    
    /**
     * Gets the player's average damage per round in this match.
     * 
     * @return The average damage per round
     */
    public double getAverageDamageRound() {
        return averageDamageRound;
    }
    
    /**
     * Gets the player's headshot percentage in this match.
     * 
     * @return The headshot percentage (0.0-100.0)
     */
    public double getHeadshotPercentage() {
        return headshotPercentage;
    }
    
    /**
     * Gets the number of MVP awards the player received.
     * 
     * @return The MVP count
     */
    public int getMvps() {
        return mvps;
    }
    
    /**
     * Gets the match result for the player's team.
     * 
     * @return "win" or "loss"
     */
    public String getResult() {
        return result;
    }
    
    /**
     * Gets the match timestamp.
     * 
     * @return Unix timestamp of when the match occurred
     */
    public long getMatchTimestamp() {
        return matchTimestamp;
    }
    
    /**
     * Gets the list of player nicknames on the team.
     * 
     * @return List of teammate nicknames
     */
    public List<String> getTeamRoster() {
        return teamRoster;
    }
    
    /**
     * Gets the total number of rounds played in the match.
     * 
     * @return The rounds played count
     */
    public int getRoundsPlayed() {
        return roundsPlayed;
    }
    
    /**
     * Calculates the K/D ratio for this match.
     * 
     * @return The K/D ratio, or 0 if deaths is 0
     */
    public double getKDRatio() {
        return deaths > 0 ? (double) kills / deaths : kills;
    }
    
    /**
     * Sets the unique match ID.
     * 
     * @param matchId The Faceit match ID
     */
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
    
    /**
     * Sets the player ID for this match data.
     * 
     * @param playerId The player's Faceit ID
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    /**
     * Sets the map name where the match was played.
     * 
     * @param mapName The map name
     */
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
    
    /**
     * Sets the match score.
     * 
     * @param score The score in "team1-team2" format
     */
    public void setScore(String score) {
        this.score = score;
    }
    
    /**
     * Sets the number of kills the player achieved.
     * 
     * @param kills The kill count
     */
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    /**
     * Sets the number of deaths the player suffered.
     * 
     * @param deaths The death count
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    /**
     * Sets the number of assists the player achieved.
     * 
     * @param assists The assist count
     */
    public void setAssists(int assists) {
        this.assists = assists;
    }
    
    /**
     * Sets the player's average damage per round in this match.
     * 
     * @param averageDamageRound The average damage per round
     */
    public void setAverageDamageRound(double averageDamageRound) {
        this.averageDamageRound = averageDamageRound;
    }
    
    /**
     * Sets the player's headshot percentage in this match.
     * 
     * @param headshotPercentage The headshot percentage
     */
    public void setHeadshotPercentage(double headshotPercentage) {
        this.headshotPercentage = headshotPercentage;
    }
    
    /**
     * Sets the number of MVP awards the player received.
     * 
     * @param mvps The MVP count
     */
    public void setMvps(int mvps) {
        this.mvps = mvps;
    }
    
    /**
     * Sets the match result for the player's team.
     * 
     * @param result "win" or "loss"
     */
    public void setResult(String result) {
        this.result = result;
    }
    
    /**
     * Sets the match timestamp.
     * 
     * @param matchTimestamp Unix timestamp of when the match occurred
     */
    public void setMatchTimestamp(long matchTimestamp) {
        this.matchTimestamp = matchTimestamp;
    }
    
    /**
     * Sets the list of player nicknames on the team.
     * 
     * @param teamRoster List of teammate nicknames
     */
    public void setTeamRoster(List<String> teamRoster) {
        this.teamRoster = teamRoster;
    }
    
    /**
     * Sets the total number of rounds played in the match.
     * 
     * @param roundsPlayed The rounds played count
     */
    public void setRoundsPlayed(int roundsPlayed) {
        this.roundsPlayed = roundsPlayed;
    }
}
