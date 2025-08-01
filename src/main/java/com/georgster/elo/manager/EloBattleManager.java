package com.georgster.elo.manager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.georgster.control.util.ClientContext;
import com.georgster.control.manager.GuildedSoapManager;
import com.georgster.database.ProfileType;
import com.georgster.elo.model.EloBattle;
import com.georgster.elo.model.EloBattle.BattleStatus;

/**
 * Manages EloBattle objects for a guild.
 */
public class EloBattleManager extends GuildedSoapManager<EloBattle> {
    
    private final ClientContext context;

    /**
     * Creates a new EloBattleManager for the given ClientContext.
     * 
     * @param context The context of the SoapClient for this manager
     */
    public EloBattleManager(ClientContext context) {
        super(context, ProfileType.ELO_BATTLES, EloBattle.class, "battleId");
        this.context = context;
    }

    /**
     * Creates a new battle between two players.
     * 
     * @param challengerId The ID of the challenging player
     * @param challengedId The ID of the challenged player
     * @return The created EloBattle
     */
    public EloBattle createBattle(String challengerId, String challengedId) {
        String battleId = generateBattleId();
        EloBattle battle = new EloBattle(battleId, challengerId, challengedId, handler.getId());
        add(battle);
        return battle;
    }

    /**
     * Gets all active battles (not completed, cancelled, or expired).
     * 
     * @return List of active battles
     */
    public List<EloBattle> getActiveBattles() {
        return observees.stream()
                .filter(battle -> battle.getStatus() == BattleStatus.PENDING_ACCEPTANCE ||
                                battle.getStatus() == BattleStatus.IN_PROGRESS)
                .filter(battle -> !battle.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Gets all battles for a specific player.
     * 
     * @param playerId The player's ID
     * @return List of battles involving the player
     */
    public List<EloBattle> getBattlesForPlayer(String playerId) {
        return observees.stream()
                .filter(battle -> battle.isParticipant(playerId))
                .collect(Collectors.toList());
    }

    /**
     * Gets active battles for a specific player.
     * 
     * @param playerId The player's ID
     * @return List of active battles involving the player
     */
    public List<EloBattle> getActiveBattlesForPlayer(String playerId) {
        return getActiveBattles().stream()
                .filter(battle -> battle.isParticipant(playerId))
                .collect(Collectors.toList());
    }

    /**
     * Gets battles that are pending acceptance by a specific player.
     * 
     * @param playerId The player's ID
     * @return List of battles waiting for this player's acceptance
     */
    public List<EloBattle> getPendingBattlesForPlayer(String playerId) {
        return observees.stream()
                .filter(battle -> battle.getStatus() == BattleStatus.PENDING_ACCEPTANCE)
                .filter(battle -> battle.getChallengedId().equals(playerId))
                .filter(battle -> !battle.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Accepts a battle challenge.
     * 
     * @param battleId The battle ID
     * @param acceptingPlayerId The ID of the player accepting (should be the challenged player)
     * @return true if successfully accepted, false otherwise
     */
    public boolean acceptBattle(String battleId, String acceptingPlayerId) {
        EloBattle battle = get(battleId);
        if (battle == null || battle.isExpired()) {
            return false;
        }

        if (!battle.getChallengedId().equals(acceptingPlayerId) ||
            battle.getStatus() != BattleStatus.PENDING_ACCEPTANCE) {
            return false;
        }

        battle.setStatus(BattleStatus.IN_PROGRESS);
        update(battle);
        return true;
    }

    /**
     * Declines a battle challenge.
     * 
     * @param battleId The battle ID
     * @param decliningPlayerId The ID of the player declining
     * @return true if successfully declined, false otherwise
     */
    public boolean declineBattle(String battleId, String decliningPlayerId) {
        EloBattle battle = get(battleId);
        if (battle == null) {
            return false;
        }

        if (!battle.getChallengedId().equals(decliningPlayerId) ||
            battle.getStatus() != BattleStatus.PENDING_ACCEPTANCE) {
            return false;
        }

        battle.setStatus(BattleStatus.CANCELLED);
        update(battle);
        return true;
    }

    /**
     * Completes a battle with a specific winner and processes ELO changes.
     * This method is idempotent - calling it multiple times will only process ELO once.
     * 
     * @param battleId The battle ID
     * @param winnerId The ID of the winning player
     * @return true if successfully completed, false otherwise
     */
    public boolean completeBattle(String battleId, String winnerId) {
        EloBattle battle = get(battleId);
        if (battle == null) {
            return false;
        }

        if (!battle.isParticipant(winnerId)) {
            return false;
        }

        // Check if battle is already completed to prevent double ELO processing
        boolean wasAlreadyCompleted = (battle.getStatus() == BattleStatus.COMPLETED);

        // Set winner and complete the battle
        battle.setWinnerId(winnerId);
        battle.setStatus(BattleStatus.COMPLETED);
        update(battle);

        // Only process ELO changes if the battle wasn't already completed
        if (!wasAlreadyCompleted) {
            String loserId = battle.getOpponentId(winnerId);
            if (loserId != null) {
                EloManager eloManager = context.getEloManager();
                eloManager.processBattleResult(winnerId, loserId);
            }
        }

        return true;
    }

    /**
     * Cancels a battle.
     * 
     * @param battleId The battle ID
     * @return true if successfully cancelled, false otherwise
     */
    public boolean cancelBattle(String battleId) {
        EloBattle battle = get(battleId);
        if (battle == null) {
            return false;
        }

        if (battle.getStatus() == BattleStatus.COMPLETED) {
            return false; // Cannot cancel completed battles
        }

        battle.setStatus(BattleStatus.CANCELLED);
        update(battle);
        return true;
    }

    /**
     * Cleans up expired battles by marking them as expired.
     * 
     * @return Number of battles marked as expired
     */
    public int cleanupExpiredBattles() {
        int count = 0;
        for (EloBattle battle : observees) {
            if (battle.isExpired() && 
                battle.getStatus() != BattleStatus.COMPLETED &&
                battle.getStatus() != BattleStatus.CANCELLED &&
                battle.getStatus() != BattleStatus.EXPIRED) {
                
                battle.setStatus(BattleStatus.EXPIRED);
                update(battle);
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if a player is currently in any active battle.
     * 
     * @param playerId The player's ID
     * @return true if the player is in an active battle
     */
    public boolean isPlayerInActiveBattle(String playerId) {
        return getActiveBattlesForPlayer(playerId).size() > 0;
    }

    /**
     * Gets the number of battles a player has completed.
     * 
     * @param playerId The player's ID
     * @return Number of completed battles
     */
    public int getCompletedBattlesCount(String playerId) {
        return (int) observees.stream()
                .filter(battle -> battle.isParticipant(playerId))
                .filter(battle -> battle.getStatus() == BattleStatus.COMPLETED)
                .count();
    }

    /**
     * Gets the number of battles a player has won.
     * 
     * @param playerId The player's ID
     * @return Number of won battles
     */
    public int getWonBattlesCount(String playerId) {
        return (int) observees.stream()
                .filter(battle -> battle.getStatus() == BattleStatus.COMPLETED)
                .filter(battle -> playerId.equals(battle.getWinnerId()))
                .count();
    }

    /**
     * Generates a unique battle ID.
     * 
     * @return A unique battle identifier
     */
    private String generateBattleId() {
        return "battle_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
