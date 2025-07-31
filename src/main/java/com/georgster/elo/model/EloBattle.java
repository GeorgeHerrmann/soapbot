package com.georgster.elo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.georgster.control.manager.Manageable;
import com.georgster.elo.EloRating;
/**
 * Represents an active Elo battle between players.
 * Manages battle state, participants, wagering, and timing.
 */
public class EloBattle implements Manageable {
    
    /**
     * Enum representing the current state of a battle.
     */
    public enum BattleState {
        PENDING_ACCEPTANCE,  // Waiting for opponent to accept
        ACTIVE,             // Battle is ongoing
        AWAITING_WINNER,    // Battle finished, waiting for winner declaration
        COMPLETED,          // Battle completed and processed
        CANCELLED,          // Battle was cancelled
        EXPIRED             // Battle expired without completion
    }
    
    /**
     * Enum representing the type of battle wager.
     */
    public enum WagerType {
        NONE,      // No wager
        COINS,     // Coin wager
        ELO_STAKES // Enhanced Elo point stakes
    }
    
    private final String battleId;
    private final String creatorId;
    private final String opponentId;
    private final String guildId;
    private final LocalDateTime createdAt;
    
    private BattleState state;
    private String winnerId;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    
    // Wager information
    private WagerType wagerType;
    private long coinWager;
    private boolean eloStakes;
    
    // Battle statistics
    private int creatorRatingBefore;
    private int opponentRatingBefore;
    private int creatorRatingAfter;
    private int opponentRatingAfter;
    private int ratingChange;
    
    // Additional metadata
    private String battleType; // "quick", "challenge", "tournament"
    private List<String> spectators;
    private String notes;
    
    /**
     * Creates a new EloBattle.
     * 
     * @param battleId Unique identifier for this battle
     * @param creatorId Discord ID of the battle creator
     * @param opponentId Discord ID of the opponent
     * @param guildId Discord guild ID where the battle takes place
     */
    public EloBattle(String battleId, String creatorId, String opponentId, String guildId) {
        this.battleId = battleId;
        this.creatorId = creatorId;
        this.opponentId = opponentId;
        this.guildId = guildId;
        this.createdAt = LocalDateTime.now();
        this.state = BattleState.PENDING_ACCEPTANCE;
        this.wagerType = WagerType.NONE;
        this.coinWager = 0;
        this.eloStakes = false;
        this.spectators = new ArrayList<>();
        this.battleType = "challenge";
        
        // Set expiration to 24 hours from creation
        this.expiresAt = createdAt.plusHours(24);
    }
    
    /**
     * Accepts the battle, moving it to ACTIVE state.
     * 
     * @return true if successfully accepted, false if already accepted or invalid state
     */
    public boolean accept() {
        if (state == BattleState.PENDING_ACCEPTANCE && LocalDateTime.now().isBefore(expiresAt)) {
            this.state = BattleState.ACTIVE;
            this.acceptedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }
    
    /**
     * Cancels the battle.
     * 
     * @return true if successfully cancelled, false if battle cannot be cancelled
     */
    public boolean cancel() {
        if (state == BattleState.PENDING_ACCEPTANCE || state == BattleState.ACTIVE) {
            this.state = BattleState.CANCELLED;
            this.completedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }
    
    /**
     * Marks the battle as awaiting winner declaration.
     * This should be called when the actual battle/game concludes.
     */
    public void markAwaitingWinner() {
        if (state == BattleState.ACTIVE) {
            this.state = BattleState.AWAITING_WINNER;
        }
    }
    
    /**
     * Declares the winner and completes the battle.
     * 
     * @param winnerId Discord ID of the winner
     * @param creatorRating Current creator Elo rating
     * @param opponentRating Current opponent Elo rating
     * @return true if winner successfully declared, false otherwise
     */
    public boolean declareWinner(String winnerId, EloRating creatorRating, EloRating opponentRating) {
        if (state != BattleState.AWAITING_WINNER && state != BattleState.ACTIVE) {
            return false;
        }
        
        if (!winnerId.equals(creatorId) && !winnerId.equals(opponentId)) {
            return false;
        }
        
        this.winnerId = winnerId;
        this.state = BattleState.COMPLETED;
        this.completedAt = LocalDateTime.now();
        
        // Record rating changes
        this.creatorRatingBefore = creatorRating.getRating();
        this.opponentRatingBefore = opponentRating.getRating();
        
        return true;
    }
    
    /**
     * Sets the rating changes after the battle is processed.
     * 
     * @param creatorAfter Creator's rating after the battle
     * @param opponentAfter Opponent's rating after the battle
     */
    public void setRatingResults(int creatorAfter, int opponentAfter) {
        this.creatorRatingAfter = creatorAfter;
        this.opponentRatingAfter = opponentAfter;
        
        if (winnerId.equals(creatorId)) {
            this.ratingChange = creatorAfter - creatorRatingBefore;
        } else {
            this.ratingChange = opponentAfter - opponentRatingBefore;
        }
    }
    
    /**
     * Checks if the battle has expired.
     * 
     * @return true if the battle has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Expires the battle if it hasn't been completed.
     */
    public void expire() {
        if (state == BattleState.PENDING_ACCEPTANCE || state == BattleState.ACTIVE) {
            this.state = BattleState.EXPIRED;
            this.completedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Adds a spectator to the battle.
     * 
     * @param spectatorId Discord ID of the spectator
     */
    public void addSpectator(String spectatorId) {
        if (!spectators.contains(spectatorId) && 
            !spectatorId.equals(creatorId) && 
            !spectatorId.equals(opponentId)) {
            spectators.add(spectatorId);
        }
    }
    
    /**
     * Sets the coin wager for this battle.
     * 
     * @param amount Amount of coins to wager
     */
    public void setCoinWager(long amount) {
        this.coinWager = amount;
        this.wagerType = amount > 0 ? WagerType.COINS : WagerType.NONE;
    }
    
    /**
     * Enables Elo stakes for this battle.
     * 
     * @param enabled Whether Elo stakes are enabled
     */
    public void setEloStakes(boolean enabled) {
        this.eloStakes = enabled;
        if (enabled) {
            this.wagerType = WagerType.ELO_STAKES;
        } else if (coinWager == 0) {
            this.wagerType = WagerType.NONE;
        }
    }
    
    /**
     * Gets the loser of the battle.
     * 
     * @return Discord ID of the loser, or null if no winner declared
     */
    public String getLoserId() {
        if (winnerId == null) return null;
        return winnerId.equals(creatorId) ? opponentId : creatorId;
    }
    
    /**
     * Checks if a user is a participant in this battle.
     * 
     * @param userId Discord ID to check
     * @return true if the user is the creator or opponent
     */
    public boolean isParticipant(String userId) {
        return userId.equals(creatorId) || userId.equals(opponentId);
    }
    
    /**
     * Gets the opponent of a given participant.
     * 
     * @param participantId Discord ID of a participant
     * @return Discord ID of their opponent, or null if not a participant
     */
    public String getOpponent(String participantId) {
        if (participantId.equals(creatorId)) return opponentId;
        if (participantId.equals(opponentId)) return creatorId;
        return null;
    }
    
    // Getters
    public String getBattleId() { return battleId; }
    public String getCreatorId() { return creatorId; }
    public String getOpponentId() { return opponentId; }
    public String getGuildId() { return guildId; }
    public BattleState getState() { return state; }
    public String getWinnerId() { return winnerId; }
    public WagerType getWagerType() { return wagerType; }
    public long getCoinWager() { return coinWager; }
    public boolean hasEloStakes() { return eloStakes; }
    public String getBattleType() { return battleType; }
    public List<String> getSpectators() { return new ArrayList<>(spectators); }
    public String getNotes() { return notes; }
    public int getCreatorRatingBefore() { return creatorRatingBefore; }
    public int getOpponentRatingBefore() { return opponentRatingBefore; }
    public int getCreatorRatingAfter() { return creatorRatingAfter; }
    public int getOpponentRatingAfter() { return opponentRatingAfter; }
    public int getRatingChange() { return ratingChange; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    
    // Setters for additional configuration
    public void setBattleType(String battleType) { this.battleType = battleType; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    @Override
    public String getIdentifier() {
        return battleId;
    }
}
