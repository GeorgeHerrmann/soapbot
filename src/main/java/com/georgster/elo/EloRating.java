package com.georgster.elo;

import java.util.HashMap;
import java.util.Map;

import com.georgster.control.manager.Manageable;
import com.georgster.util.DateTimed;

/**
 * Represents an Elo rating for a Discord member within a guild.
 * <p>
 * This class tracks a member's competitive rating, match history, and statistics
 * for the guild's Elo battle system.
 */
public class EloRating implements Manageable {
    private final String memberId;
    private int rating;
    private int matchesPlayed;
    private int wins;
    private int losses;
    private int winStreak;
    private int bestRating;
    private Map<String, Integer> opponentHistory; // Track consecutive wins vs specific opponents
    private DateTimed lastMatch;

    /**
     * Creates a new EloRating with default values for a new player.
     * 
     * @param memberId The Discord member ID this rating belongs to
     */
    public EloRating(String memberId) {
        this.memberId = memberId;
        this.rating = 1500; // Starting Elo rating
        this.matchesPlayed = 0;
        this.wins = 0;
        this.losses = 0;
        this.winStreak = 0;
        this.bestRating = 1500;
        this.opponentHistory = new HashMap<>();
        this.lastMatch = null;
    }

    /**
     * Full constructor for loading from database.
     * 
     * @param memberId The Discord member ID
     * @param rating Current Elo rating
     * @param matchesPlayed Total matches played
     * @param wins Total wins
     * @param losses Total losses
     * @param winStreak Current win streak
     * @param bestRating Highest rating achieved
     * @param opponentHistory Map of opponent IDs to consecutive wins against them
     * @param lastMatch When the last match was played
     */
    public EloRating(String memberId, int rating, int matchesPlayed, int wins, int losses, 
                     int winStreak, int bestRating, Map<String, Integer> opponentHistory, DateTimed lastMatch) {
        this.memberId = memberId;
        this.rating = rating;
        this.matchesPlayed = matchesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.winStreak = winStreak;
        this.bestRating = bestRating;
        this.opponentHistory = opponentHistory != null ? opponentHistory : new HashMap<>();
        this.lastMatch = lastMatch;
    }

    /**
     * Updates the rating after a match result.
     * 
     * @param newRating The new Elo rating
     * @param won Whether the player won the match
     * @param opponentId The opponent's member ID
     */
    public void updateAfterMatch(int newRating, boolean won, String opponentId) {
        this.rating = newRating;
        this.matchesPlayed++;
        this.lastMatch = DateTimed.getCurrentSystemTime();
        
        if (won) {
            this.wins++;
            this.winStreak++;
            if (this.rating > this.bestRating) {
                this.bestRating = this.rating;
            }
            // Track consecutive wins against this opponent
            opponentHistory.put(opponentId, opponentHistory.getOrDefault(opponentId, 0) + 1);
        } else {
            this.losses++;
            this.winStreak = 0;
            // Reset consecutive wins against this opponent
            opponentHistory.put(opponentId, 0);
        }
    }

    /**
     * Gets the current rank based on the rating.
     * 
     * @return The EloRank corresponding to the current rating
     */
    public EloRank getRank() {
        return EloRank.fromRating(rating);
    }

    /**
     * Gets the number of consecutive wins against a specific opponent.
     * 
     * @param opponentId The opponent's member ID
     * @return Number of consecutive wins against this opponent
     */
    public int getConsecutiveWinsAgainst(String opponentId) {
        return opponentHistory.getOrDefault(opponentId, 0);
    }

    /**
     * Calculates the win rate as a percentage.
     * 
     * @return Win rate percentage (0-100)
     */
    public double getWinRate() {
        if (matchesPlayed == 0) return 0.0;
        return (double) wins / matchesPlayed * 100.0;
    }

    // Getters
    public String getMemberId() { return memberId; }
    public int getRating() { return rating; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getWinStreak() { return winStreak; }
    public int getBestRating() { return bestRating; }
    public Map<String, Integer> getOpponentHistory() { return new HashMap<>(opponentHistory); }
    public DateTimed getLastMatch() { return lastMatch; }

    // Setters for database loading
    public void setRating(int rating) { this.rating = rating; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }
    public void setWins(int wins) { this.wins = wins; }
    public void setLosses(int losses) { this.losses = losses; }
    public void setWinStreak(int winStreak) { this.winStreak = winStreak; }
    public void setBestRating(int bestRating) { this.bestRating = bestRating; }
    public void setOpponentHistory(Map<String, Integer> opponentHistory) { 
        this.opponentHistory = opponentHistory != null ? opponentHistory : new HashMap<>(); 
    }
    public void setLastMatch(DateTimed lastMatch) { this.lastMatch = lastMatch; }

    /**
     * Gets the unique identifier for this EloRating (the member ID).
     * 
     * @return The member ID
     */
    @Override
    public String getIdentifier() {
        return memberId;
    }

    @Override
    public String toString() {
        return String.format("EloRating[%s: %d (%s) - %d/%d W/L, %d streak]", 
                           memberId, rating, getRank(), wins, losses, winStreak);
    }
}
