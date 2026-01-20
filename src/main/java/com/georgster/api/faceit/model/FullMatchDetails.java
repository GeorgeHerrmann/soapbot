package com.georgster.api.faceit.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Data Transfer Object representing complete match statistics for all players in a CS2 match.
 * <p>
 * Contains comprehensive match information including all players from both teams,
 * individual performance metrics, team rosters, and match outcome.
 * <p>
 * This class is designed for deserialization from Faceit API JSON responses using Gson.
 * Used by CS2MatchWizard for displaying full game statistics.
 */
public class FullMatchDetails {
    
    @SerializedName("match_id")
    private String matchId;
    
    @SerializedName("map_name")
    private String mapName;
    
    @SerializedName("match_result")
    private String matchResult; // "W" or "L" from perspective of team1
    
    @SerializedName("final_score")
    private String finalScore; // e.g., "16-14"
    
    @SerializedName("match_timestamp")
    private long matchTimestamp;
    
    @SerializedName("team1")
    private TeamRoster team1; // Winning team
    
    @SerializedName("team2")
    private TeamRoster team2; // Losing team
    
    /**
     * Creates a new FullMatchDetails instance.
     */
    public FullMatchDetails() {
        this.team1 = new TeamRoster();
        this.team2 = new TeamRoster();
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
     * Sets the match ID.
     * 
     * @param matchId The Faceit match ID
     */
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
    
    /**
     * Gets the map name where the match was played.
     * 
     * @return The map name (e.g., "de_mirage")
     */
    public String getMapName() {
        return mapName;
    }
    
    /**
     * Sets the map name.
     * 
     * @param mapName The map name
     */
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
    
    /**
     * Gets the match result from team1's perspective.
     * 
     * @return "W" for win or "L" for loss
     */
    public String getMatchResult() {
        return matchResult;
    }
    
    /**
     * Sets the match result.
     * 
     * @param matchResult "W" or "L"
     */
    public void setMatchResult(String matchResult) {
        this.matchResult = matchResult;
    }
    
    /**
     * Gets the final score.
     * 
     * @return Score in "XX-YY" format (e.g., "16-14")
     */
    public String getFinalScore() {
        return finalScore;
    }
    
    /**
     * Sets the final score.
     * 
     * @param finalScore Score in "XX-YY" format
     */
    public void setFinalScore(String finalScore) {
        this.finalScore = finalScore;
    }
    
    /**
     * Gets the match timestamp.
     * 
     * @return Unix timestamp when match was played
     */
    public long getMatchTimestamp() {
        return matchTimestamp;
    }
    
    /**
     * Sets the match timestamp.
     * 
     * @param matchTimestamp Unix timestamp
     */
    public void setMatchTimestamp(long matchTimestamp) {
        this.matchTimestamp = matchTimestamp;
    }
    
    /**
     * Gets team1 roster (typically the winning team).
     * 
     * @return TeamRoster object for team1
     */
    public TeamRoster getTeam1() {
        return team1;
    }
    
    /**
     * Sets team1 roster.
     * 
     * @param team1 TeamRoster object
     */
    public void setTeam1(TeamRoster team1) {
        this.team1 = team1;
    }
    
    /**
     * Gets team2 roster.
     * 
     * @return TeamRoster object for team2
     */
    public TeamRoster getTeam2() {
        return team2;
    }
    
    /**
     * Sets team2 roster.
     * 
     * @param team2 TeamRoster object
     */
    public void setTeam2(TeamRoster team2) {
        this.team2 = team2;
    }
    
    /**
     * Identifies the MVP (player with highest ADR across both teams).
     * 
     * @return PlayerMatchPerformance of the MVP, or null if no players found
     */
    public PlayerMatchPerformance getMVP() {
        List<PlayerMatchPerformance> allPlayers = new ArrayList<>();
        if (team1 != null && team1.getPlayers() != null) {
            allPlayers.addAll(team1.getPlayers());
        }
        if (team2 != null && team2.getPlayers() != null) {
            allPlayers.addAll(team2.getPlayers());
        }
        
        return allPlayers.stream()
                .max(Comparator.comparingDouble(PlayerMatchPerformance::getAdr))
                .orElse(null);
    }
    
    /**
     * Nested class representing a team roster.
     */
    public static class TeamRoster {
        
        @SerializedName("team_name")
        private String teamName;
        
        @SerializedName("players")
        private List<PlayerMatchPerformance> players;
        
        /**
         * Creates a new TeamRoster instance.
         */
        public TeamRoster() {
            this.players = new ArrayList<>();
        }
        
        /**
         * Gets the team name.
         * 
         * @return The team name
         */
        public String getTeamName() {
            return teamName;
        }
        
        /**
         * Sets the team name.
         * 
         * @param teamName The team name
         */
        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }
        
        /**
         * Gets the list of players on this team.
         * 
         * @return List of PlayerMatchPerformance objects
         */
        public List<PlayerMatchPerformance> getPlayers() {
            return players;
        }
        
        /**
         * Sets the list of players.
         * 
         * @param players List of PlayerMatchPerformance objects
         */
        public void setPlayers(List<PlayerMatchPerformance> players) {
            this.players = players;
        }
        
        /**
         * Adds a player to the roster.
         * 
         * @param player PlayerMatchPerformance to add
         */
        public void addPlayer(PlayerMatchPerformance player) {
            this.players.add(player);
        }
    }
    
    /**
     * Nested class representing a player's performance in a match.
     */
    public static class PlayerMatchPerformance {
        
        @SerializedName("nickname")
        private String nickname;
        
        @SerializedName("kills")
        private int kills;
        
        @SerializedName("deaths")
        private int deaths;
        
        @SerializedName("assists")
        private int assists;
        
        @SerializedName("adr")
        private double adr;
        
        @SerializedName("headshot_percentage")
        private double headshotPercentage;
        
        /**
         * Creates a new PlayerMatchPerformance instance.
         */
        public PlayerMatchPerformance() {
        }
        
        /**
         * Gets the player's nickname.
         * 
         * @return The Faceit nickname
         */
        public String getNickname() {
            return nickname;
        }
        
        /**
         * Sets the player's nickname.
         * 
         * @param nickname The Faceit nickname
         */
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        
        /**
         * Gets the player's kills in the match.
         * 
         * @return Kill count
         */
        public int getKills() {
            return kills;
        }
        
        /**
         * Sets the player's kills.
         * 
         * @param kills Kill count
         */
        public void setKills(int kills) {
            this.kills = kills;
        }
        
        /**
         * Gets the player's deaths in the match.
         * 
         * @return Death count
         */
        public int getDeaths() {
            return deaths;
        }
        
        /**
         * Sets the player's deaths.
         * 
         * @param deaths Death count
         */
        public void setDeaths(int deaths) {
            this.deaths = deaths;
        }
        
        /**
         * Gets the player's assists in the match.
         * 
         * @return Assist count
         */
        public int getAssists() {
            return assists;
        }
        
        /**
         * Sets the player's assists.
         * 
         * @param assists Assist count
         */
        public void setAssists(int assists) {
            this.assists = assists;
        }
        
        /**
         * Gets the player's average damage per round.
         * 
         * @return ADR value
         */
        public double getAdr() {
            return adr;
        }
        
        /**
         * Sets the player's ADR.
         * 
         * @param adr ADR value
         */
        public void setAdr(double adr) {
            this.adr = adr;
        }
        
        /**
         * Gets the player's headshot percentage.
         * 
         * @return Headshot percentage (0.0 - 100.0)
         */
        public double getHeadshotPercentage() {
            return headshotPercentage;
        }
        
        /**
         * Sets the player's headshot percentage.
         * 
         * @param headshotPercentage Headshot percentage
         */
        public void setHeadshotPercentage(double headshotPercentage) {
            this.headshotPercentage = headshotPercentage;
        }
        
        /**
         * Gets the K/D/A ratio formatted as a string.
         * 
         * @return K/D/A in "kills/deaths/assists" format
         */
        public String getKDA() {
            return String.format("%d/%d/%d", kills, deaths, assists);
        }
        
        /**
         * Gets the K/D ratio.
         * 
         * @return K/D ratio (kills divided by deaths, or kills if deaths is 0)
         */
        public double getKDRatio() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }
    }
}
