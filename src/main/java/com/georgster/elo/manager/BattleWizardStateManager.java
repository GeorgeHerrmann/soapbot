package com.georgster.elo.manager;

import java.util.List;
import java.util.stream.Collectors;

import com.georgster.control.util.ClientContext;
import com.georgster.control.manager.GuildedSoapManager;
import com.georgster.database.ProfileType;
import com.georgster.elo.model.BattleWizardState;
import com.georgster.elo.model.BattleWizardState.WizardPhase;

/**
 * Manages BattleWizardState objects for a guild.
 */
public class BattleWizardStateManager extends GuildedSoapManager<BattleWizardState> {

    /**
     * Creates a new BattleWizardStateManager for the given ClientContext.
     * 
     * @param context The context of the SoapClient for this manager
     */
    public BattleWizardStateManager(ClientContext context) {
        super(context, ProfileType.BATTLE_WIZARD_STATES, BattleWizardState.class, "userId");
    }

    /**
     * Creates a new wizard state for a user.
     * 
     * @param userId The user's ID
     * @return The created BattleWizardState
     */
    public BattleWizardState createWizardState(String userId) {
        // Remove any existing wizard state for this user
        removeUserWizardState(userId);
        
        BattleWizardState state = new BattleWizardState(userId, handler.getId());
        add(state);
        return state;
    }

    /**
     * Gets the wizard state for a specific user.
     * 
     * @param userId The user's ID
     * @return The wizard state or null if not found
     */
    public BattleWizardState getWizardState(String userId) {
        return get(userId);
    }

    /**
     * Gets all active wizard states (not completed).
     * 
     * @return List of active wizard states
     */
    public List<BattleWizardState> getActiveWizardStates() {
        return observees.stream()
                .filter(state -> !state.isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * Gets all wizard states in a specific phase.
     * 
     * @param phase The wizard phase to filter by
     * @return List of wizard states in the specified phase
     */
    public List<BattleWizardState> getWizardStatesByPhase(WizardPhase phase) {
        return observees.stream()
                .filter(state -> state.getCurrentPhase() == phase)
                .collect(Collectors.toList());
    }

    /**
     * Updates a wizard state's phase.
     * 
     * @param userId The user's ID
     * @param newPhase The new phase
     * @return true if successfully updated, false otherwise
     */
    public boolean updateWizardPhase(String userId, WizardPhase newPhase) {
        BattleWizardState state = get(userId);
        if (state == null) {
            return false;
        }

        if (!state.canAdvanceTo(newPhase)) {
            return false;
        }

        state.advanceToPhase(newPhase);
        update(state);
        return true;
    }

    /**
     * Sets the challenged player for a wizard state.
     * 
     * @param userId The user's ID
     * @param challengedPlayerId The challenged player's ID
     * @return true if successfully set, false otherwise
     */
    public boolean setChallengedPlayer(String userId, String challengedPlayerId) {
        BattleWizardState state = get(userId);
        if (state == null || state.isCompleted()) {
            return false;
        }

        state.setChallengedPlayerId(challengedPlayerId);
        update(state);
        return true;
    }

    /**
     * Sets the battle ID for a wizard state.
     * 
     * @param userId The user's ID
     * @param battleId The battle ID
     * @return true if successfully set, false otherwise
     */
    public boolean setBattleId(String userId, String battleId) {
        BattleWizardState state = get(userId);
        if (state == null || state.isCompleted()) {
            return false;
        }

        state.setBattleId(battleId);
        update(state);
        return true;
    }

    /**
     * Sets the battle description for a wizard state.
     * 
     * @param userId The user's ID
     * @param description The battle description
     * @return true if successfully set, false otherwise
     */
    public boolean setBattleDescription(String userId, String description) {
        BattleWizardState state = get(userId);
        if (state == null || state.isCompleted()) {
            return false;
        }

        state.setBattleDescription(description);
        update(state);
        return true;
    }

    /**
     * Completes a wizard state.
     * 
     * @param userId The user's ID
     * @return true if successfully completed, false otherwise
     */
    public boolean completeWizard(String userId) {
        BattleWizardState state = get(userId);
        if (state == null) {
            return false;
        }

        state.setCompleted(true);
        update(state);
        return true;
    }

    /**
     * Cancels a wizard state.
     * 
     * @param userId The user's ID
     * @return true if successfully cancelled, false otherwise
     */
    public boolean cancelWizard(String userId) {
        BattleWizardState state = get(userId);
        if (state == null) {
            return false;
        }

        state.cancel();
        update(state);
        return true;
    }

    /**
     * Removes a user's wizard state completely.
     * 
     * @param userId The user's ID
     * @return true if successfully removed, false otherwise
     */
    public boolean removeUserWizardState(String userId) {
        if (exists(userId)) {
            remove(userId);
            return true;
        }
        return false;
    }

    /**
     * Checks if a user is currently in a wizard.
     * 
     * @param userId The user's ID
     * @return true if the user has an active wizard state
     */
    public boolean isUserInWizard(String userId) {
        BattleWizardState state = get(userId);
        return state != null && !state.isCompleted();
    }

    /**
     * Gets all wizard states associated with a specific battle.
     * 
     * @param battleId The battle ID
     * @return List of wizard states for the battle
     */
    public List<BattleWizardState> getWizardStatesForBattle(String battleId) {
        return observees.stream()
                .filter(state -> battleId.equals(state.getBattleId()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all wizard states waiting for a specific player to accept.
     * 
     * @param playerId The player's ID
     * @return List of wizard states waiting for this player
     */
    public List<BattleWizardState> getWizardStatesWaitingForPlayer(String playerId) {
        return observees.stream()
                .filter(state -> state.getCurrentPhase() == WizardPhase.WAITING_FOR_ACCEPTANCE)
                .filter(state -> playerId.equals(state.getChallengedPlayerId()))
                .collect(Collectors.toList());
    }

    /**
     * Cleans up stale wizard states (older than 30 minutes).
     * 
     * @return Number of wizard states cleaned up
     */
    public int cleanupStaleWizardStates() {
        int count = 0;
        List<BattleWizardState> staleStates = observees.stream()
                .filter(BattleWizardState::isStale)
                .filter(state -> !state.isCompleted())
                .collect(Collectors.toList());

        for (BattleWizardState state : staleStates) {
            state.cancel();
            update(state);
            count++;
        }

        return count;
    }

    /**
     * Gets the total number of active wizards.
     * 
     * @return Number of active wizard states
     */
    public int getActiveWizardCount() {
        return getActiveWizardStates().size();
    }

    /**
     * Advances a wizard to the next logical phase based on current state.
     * 
     * @param userId The user's ID
     * @return true if successfully advanced, false otherwise
     */
    public boolean advanceWizard(String userId) {
        BattleWizardState state = get(userId);
        if (state == null || state.isCompleted()) {
            return false;
        }

        WizardPhase currentPhase = state.getCurrentPhase();
        WizardPhase nextPhase = getNextPhase(currentPhase);
        
        if (nextPhase != null && state.canAdvanceTo(nextPhase)) {
            state.advanceToPhase(nextPhase);
            update(state);
            return true;
        }

        return false;
    }

    /**
     * Gets the next logical phase for a wizard.
     * 
     * @param currentPhase The current phase
     * @return The next phase or null if no logical next phase
     */
    private WizardPhase getNextPhase(WizardPhase currentPhase) {
        switch (currentPhase) {
            case OPPONENT_SELECTION:
                return WizardPhase.WAITING_FOR_ACCEPTANCE;
            case WAITING_FOR_ACCEPTANCE:
                return WizardPhase.BATTLE_ACTIVE;
            case BATTLE_ACTIVE:
                return WizardPhase.REPORT_RESULTS;
            case REPORT_RESULTS:
                return WizardPhase.COMPLETED;
            default:
                return null;
        }
    }
}
