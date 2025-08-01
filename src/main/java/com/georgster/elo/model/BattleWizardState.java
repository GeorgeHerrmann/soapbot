package com.georgster.elo.model;

import com.georgster.control.manager.Manageable;
import com.georgster.util.DateTimed;

/**
 * Represents the state of a battle wizard for a specific user.
 * Tracks progress through the battle creation/participation process.
 */
public class BattleWizardState implements Manageable {
    private String userId;
    private String battleId;
    private WizardPhase currentPhase;
    private String challengedPlayerId;
    private String battleDescription;
    private DateTimed createdAt;
    private DateTimed lastUpdated;
    private boolean isCompleted;
    private String guildId;

    /**
     * Default constructor for database serialization.
     */
    public BattleWizardState() {
        // Required for database operations
    }

    /**
     * Creates a new battle wizard state.
     * 
     * @param userId The ID of the user in the wizard
     * @param guildId The guild where the wizard is running
     */
    public BattleWizardState(String userId, String guildId) {
        this.userId = userId;
        this.guildId = guildId;
        this.currentPhase = WizardPhase.OPPONENT_SELECTION;
        this.createdAt = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
        this.isCompleted = false;
        this.battleDescription = "";
    }

    /**
     * Enum representing the different phases of the battle wizard.
     */
    public enum WizardPhase {
        OPPONENT_SELECTION,     // User is selecting who to challenge
        WAITING_FOR_ACCEPTANCE, // Waiting for opponent to accept challenge
        BATTLE_ACTIVE,         // Battle is in progress
        REPORT_RESULTS,        // Players are reporting battle results
        COMPLETED,             // Wizard finished
        CANCELLED              // Wizard was cancelled
    }

    @Override
    public String getIdentifier() {
        return userId;
    }

    /**
     * Gets the user ID.
     * 
     * @return The user's member ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     * 
     * @param userId The user's member ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the battle ID (if a battle has been created).
     * 
     * @return The battle ID or null
     */
    public String getBattleId() {
        return battleId;
    }

    /**
     * Sets the battle ID.
     * 
     * @param battleId The battle ID
     */
    public void setBattleId(String battleId) {
        this.battleId = battleId;
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Gets the current wizard phase.
     * 
     * @return The current phase
     */
    public WizardPhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Sets the current wizard phase.
     * 
     * @param currentPhase The new phase
     */
    public void setCurrentPhase(WizardPhase currentPhase) {
        this.currentPhase = currentPhase;
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Gets the challenged player's ID.
     * 
     * @return The challenged player's member ID
     */
    public String getChallengedPlayerId() {
        return challengedPlayerId;
    }

    /**
     * Sets the challenged player's ID.
     * 
     * @param challengedPlayerId The challenged player's member ID
     */
    public void setChallengedPlayerId(String challengedPlayerId) {
        this.challengedPlayerId = challengedPlayerId;
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Gets the battle description provided by the user.
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
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Gets when the wizard state was created.
     * 
     * @return The creation timestamp
     */
    public DateTimed getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the wizard state was created.
     * 
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(DateTimed createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets when the wizard state was last updated.
     * 
     * @return The last update timestamp
     */
    public DateTimed getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets when the wizard state was last updated.
     * 
     * @param lastUpdated The last update timestamp
     */
    public void setLastUpdated(DateTimed lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Checks if the wizard is completed.
     * 
     * @return true if the wizard is completed
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Sets whether the wizard is completed.
     * 
     * @param completed true if the wizard is completed
     */
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed) {
            this.currentPhase = WizardPhase.COMPLETED;
        }
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Gets the guild ID where this wizard is running.
     * 
     * @return The guild ID
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Sets the guild ID where this wizard is running.
     * 
     * @param guildId The guild ID
     */
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    /**
     * Advances the wizard to the next phase.
     * 
     * @param nextPhase The next phase to advance to
     */
    public void advanceToPhase(WizardPhase nextPhase) {
        this.currentPhase = nextPhase;
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
        
        if (nextPhase == WizardPhase.COMPLETED || nextPhase == WizardPhase.CANCELLED) {
            this.isCompleted = true;
        }
    }

    /**
     * Cancels the wizard.
     */
    public void cancel() {
        this.currentPhase = WizardPhase.CANCELLED;
        this.isCompleted = true;
        this.lastUpdated = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime());
    }

    /**
     * Checks if the wizard state is stale (older than 30 minutes).
     * 
     * @return true if the wizard state is stale
     */
    public boolean isStale() {
        return DateTimed.getCurrentLocalDateTime().isAfter(
            lastUpdated.getLocalDateTime().plusMinutes(30)
        );
    }

    /**
     * Checks if the wizard can be advanced to the specified phase.
     * 
     * @param phase The phase to check
     * @return true if advancement is allowed
     */
    public boolean canAdvanceTo(WizardPhase phase) {
        if (isCompleted) {
            return false;
        }

        // Define valid phase transitions
        switch (currentPhase) {
            case OPPONENT_SELECTION:
                return phase == WizardPhase.WAITING_FOR_ACCEPTANCE || 
                       phase == WizardPhase.CANCELLED;
            case WAITING_FOR_ACCEPTANCE:
                return phase == WizardPhase.BATTLE_ACTIVE || 
                       phase == WizardPhase.CANCELLED;
            case BATTLE_ACTIVE:
                return phase == WizardPhase.REPORT_RESULTS || 
                       phase == WizardPhase.COMPLETED ||
                       phase == WizardPhase.CANCELLED;
            case REPORT_RESULTS:
                return phase == WizardPhase.COMPLETED || 
                       phase == WizardPhase.CANCELLED;
            case COMPLETED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("BattleWizardState{userId='%s', battleId='%s', phase=%s, challengedPlayer='%s', completed=%s}",
                userId, battleId, currentPhase, challengedPlayerId, isCompleted);
    }
}
