package com.georgster.elo.model;

import com.georgster.control.manager.Manageable;
import com.georgster.util.DateTimed;

/**
 * Represents an active ELO battle between two players.
 * This model tracks all battle state including participants, consensus, and timing.
 */
public class EloBattle implements Manageable {
    private String battleId;
    private String challengerId;
    private String challengedId;
    private BattleStatus status;
    private DateTimed createdAt;
    private DateTimed expiresAt;
    private String winnerId;
    private String battleDescription;
    private String guildId;

    /**
     * Default constructor for database serialization.
     */
    public EloBattle() {
        // Required for database operations
    }

    /**
     * Creates a new ELO battle.
     * 
     * @param battleId The unique identifier for this battle
     * @param challengerId The ID of the player who initiated the battle
     * @param challengedId The ID of the player being challenged
     * @param guildId The guild where this battle is taking place
     */
    public EloBattle(String battleId, String challengerId, String challengedId, String guildId) {
        this.battleId = battleId;
        this.challengerId = challengerId;
        this.challengedId = challengedId;
        this.guildId = guildId;
        this.status = BattleStatus.PENDING_ACCEPTANCE;
        this.createdAt = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
        this.expiresAt = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime().plusMinutes(30));
        this.battleDescription = "";
    }

    /**
     * Enum representing the different states a battle can be in.
     */
    public enum BattleStatus {
        PENDING_ACCEPTANCE,    // Waiting for challenged player to accept
        IN_PROGRESS,          // Battle is active
        COMPLETED,            // Battle finished with a winner
        CANCELLED,            // Battle was cancelled
        EXPIRED               // Battle expired without completion
    }

    @Override
    public String getIdentifier() {
        return battleId;
    }

    /**
     * Gets the battle ID.
     * 
     * @return The unique battle identifier
     */
    public String getBattleId() {
        return battleId;
    }

    /**
     * Sets the battle ID.
     * 
     * @param battleId The battle identifier
     */
    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    /**
     * Gets the challenger's ID.
     * 
     * @return The challenger's member ID
     */
    public String getChallengerId() {
        return challengerId;
    }

    /**
     * Sets the challenger's ID.
     * 
     * @param challengerId The challenger's member ID
     */
    public void setChallengerId(String challengerId) {
        this.challengerId = challengerId;
    }

    /**
     * Gets the challenged player's ID.
     * 
     * @return The challenged player's member ID
     */
    public String getChallengedId() {
        return challengedId;
    }

    /**
     * Sets the challenged player's ID.
     * 
     * @param challengedId The challenged player's member ID
     */
    public void setChallengedId(String challengedId) {
        this.challengedId = challengedId;
    }

    /**
     * Gets the current battle status.
     * 
     * @return The battle status
     */
    public BattleStatus getStatus() {
        return status;
    }

    /**
     * Sets the battle status.
     * 
     * @param status The new battle status
     */
    public void setStatus(BattleStatus status) {
        this.status = status;
    }

    /**
     * Gets when the battle was created.
     * 
     * @return The creation timestamp, or current time if null
     */
    public DateTimed getCreatedAt() {
        if (createdAt == null) {
            // Initialize with current time for legacy battles
            createdAt = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
        }
        return createdAt;
    }

    /**
     * Sets when the battle was created.
     * 
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(DateTimed createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets when the battle expires.
     * 
     * @return The expiration timestamp
     */
    public DateTimed getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets when the battle expires.
     * 
     * @param expiresAt The expiration timestamp
     */
    public void setExpiresAt(DateTimed expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Gets the winner's ID (null if not yet determined).
     * 
     * @return The winner's member ID or null
     */
    public String getWinnerId() {
        return winnerId;
    }

    /**
     * Sets the winner's ID.
     * 
     * @param winnerId The winner's member ID
     */
    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    /**
     * Gets the battle description.
     * 
     * @return The battle description
     */
    public String getBattleDescription() {
        return battleDescription;
    }

    /**
     * Sets the battle description.
     * 
     * @param battleDescription The battle description
     */
    public void setBattleDescription(String battleDescription) {
        this.battleDescription = battleDescription;
    }

    /**
     * Gets the guild ID where this battle is taking place.
     * 
     * @return The guild ID
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Sets the guild ID where this battle is taking place.
     * 
     * @param guildId The guild ID
     */
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    /**
     * Checks if the battle has expired.
     * 
     * @return true if the battle has expired
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false; // If no expiration time is set, battle never expires
        }
        return DateTimed.getCurrentLocalDateTime().isAfter(expiresAt.getLocalDateTime());
    }

    /**
     * Gets the player ID of the opponent for the given player.
     * 
     * @param playerId The player ID to get the opponent for
     * @return The opponent's ID, or null if the player is not in this battle
     */
    public String getOpponentId(String playerId) {
        if (challengerId.equals(playerId)) {
            return challengedId;
        } else if (challengedId.equals(playerId)) {
            return challengerId;
        }
        return null;
    }

    /**
     * Checks if the given player is a participant in this battle.
     * 
     * @param playerId The player ID to check
     * @return true if the player is participating
     */
    public boolean isParticipant(String playerId) {
        return challengerId.equals(playerId) || challengedId.equals(playerId);
    }

    /**
     * Determines if there's a clear winner (not used in direct result system).
     * This method is deprecated and always returns null.
     * 
     * @return Always returns null
     * @deprecated Use direct result reporting instead
     */
    @Deprecated
    public String determineWinner() {
        return null; // Not used in direct result system
    }

    @Override
    public String toString() {
        return String.format("EloBattle{id='%s', challenger='%s', challenged='%s', status=%s, winner='%s'}",
                battleId, challengerId, challengedId, status, winnerId);
    }
}
