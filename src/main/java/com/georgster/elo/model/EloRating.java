package com.georgster.elo.model;

import java.util.HashMap;
import java.util.Map;

import com.georgster.control.manager.Manageable;
import com.georgster.util.DateTimed;

/**
 * Represents an Elo rating for a member, implementing Manageable for database storage.
 */
public class EloRating implements Manageable {
    private final String memberId;
    private int rating;           // Current Elo rating (starts at 1500)
    private int matchesPlayed;    // Total matches played
    private int wins;             // Total wins
    private int losses;           // Total losses
    private int winStreak;        // Current win streak
    private int bestRating;       // Highest rating achieved
    private Map<String, Integer> opponentHistory; // Track wins vs specific opponents
    private DateTimed lastMatch;  // When last match was played

    /**
     * Creates a new EloRating with default values.
     * 
     * @param memberId The member's ID
     */
    public EloRating(String memberId) {
        this.memberId = memberId;
        this.rating = 1000;
        this.matchesPlayed = 0;
        this.wins = 0;
        this.losses = 0;
        this.winStreak = 0;
        this.bestRating = 1000;
        this.opponentHistory = new HashMap<>();
        this.lastMatch = null;
    }

    /**
     * Creates an EloRating with specified values (for database loading).
     * 
     * @param memberId The member's ID
     * @param rating Current rating
     * @param matchesPlayed Total matches played
     * @param wins Total wins
     * @param losses Total losses
     * @param winStreak Current win streak
     * @param bestRating Best rating achieved
     * @param opponentHistory Map of opponent wins
     * @param lastMatch Last match time
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
     * Records a win against an opponent.
     * 
     * @param opponentId The opponent's ID
     * @param ratingGain The rating points gained
     */
    public void recordWin(String opponentId, int ratingGain) {
        this.rating += ratingGain;
        this.matchesPlayed++;
        this.wins++;
        this.winStreak++;
        
        if (this.rating > this.bestRating) {
            this.bestRating = this.rating;
        }
        
        opponentHistory.put(opponentId, opponentHistory.getOrDefault(opponentId, 0) + 1);
        this.lastMatch = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Records a loss against an opponent.
     * 
     * @param opponentId The opponent's ID
     * @param ratingLoss The rating points lost
     */
    public void recordLoss(String opponentId, int ratingLoss) {
        this.rating -= ratingLoss;
        this.matchesPlayed++;
        this.losses++;
        this.winStreak = 0;
        this.lastMatch = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Gets the current rank based on rating.
     * 
     * @return The current EloRank
     */
    public EloRank getRank() {
        return EloRank.fromRating(rating);
    }

    /**
     * Gets the win rate as a percentage.
     * 
     * @return Win rate (0.0 to 1.0)
     */
    public double getWinRate() {
        if (matchesPlayed == 0) return 0.0;
        return (double) wins / matchesPlayed;
    }

    /**
     * Gets wins against a specific opponent.
     * 
     * @param opponentId The opponent's ID
     * @return Number of wins against this opponent
     */
    public int getWinsAgainst(String opponentId) {
        return opponentHistory.getOrDefault(opponentId, 0);
    }

    @Override
    public String getIdentifier() {
        return memberId;
    }

    // Getters and setters
    public String getMemberId() {
        return memberId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public int getBestRating() {
        return bestRating;
    }

    public void setBestRating(int bestRating) {
        this.bestRating = bestRating;
    }

    public Map<String, Integer> getOpponentHistory() {
        return opponentHistory;
    }

    public void setOpponentHistory(Map<String, Integer> opponentHistory) {
        this.opponentHistory = opponentHistory;
    }

    public DateTimed getLastMatch() {
        return lastMatch;
    }

    public void setLastMatch(DateTimed lastMatch) {
        this.lastMatch = lastMatch;
    }
}
