package com.georgster.elo.wizard;

import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.elo.manager.EloBattleManager;
import com.georgster.elo.manager.EloManager;
import com.georgster.elo.manager.BattleWizardStateManager;
import com.georgster.elo.model.EloBattle;
import com.georgster.elo.model.EloBattle.BattleStatus;
import com.georgster.elo.model.BattleWizardState;
import com.georgster.elo.model.EloRating;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.input.InputListenerFactory;
import com.georgster.logs.MultiLogger;

import discord4j.core.object.entity.Member;

/**
 * A wizard designed to handle the ELO battle creation and management process.
 * Provides a step-by-step interface for users to challenge opponents and manage battles.
 */
public class BattleWizard extends InputWizard {
    private final EloBattleManager battleManager;
    private final EloManager eloManager;
    private final BattleWizardStateManager wizardStateManager;
    private EloBattle currentBattle;
    private BattleWizardState wizardState;

    /**
     * Creates a new BattleWizard with button-based interaction.
     * 
     * @param event The command execution event that triggered this wizard
     */
    public BattleWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "ELO Battle System"));
        MultiLogger.logSystem("BattleWizard constructor called for user: " + event.getDiscordEvent().getUser().getUsername(), getClass());
        
        this.battleManager = event.getClientContext().getEloBattleManager();
        this.eloManager = event.getClientContext().getEloManager();
        this.wizardStateManager = event.getClientContext().getBattleWizardStateManager();
        
        MultiLogger.logSystem("BattleWizard managers initialized successfully", getClass());
    }

    /**
     * Begins the wizard by checking for existing battles or starting a new one.
     */
    @Override
    public void begin() {
        MultiLogger.logSystem("BattleWizard.begin() called for user: " + user.getUsername(), getClass());
        
        // Check for existing active battles
        List<EloBattle> activeBattles = battleManager.getActiveBattlesForPlayer(user.getId().asString());
        MultiLogger.logSystem("Found " + activeBattles.size() + " active battles for user", getClass());
        
        if (!activeBattles.isEmpty()) {
            MultiLogger.logSystem("Resuming existing battle wizard", getClass());
            resumeBattleWizard(activeBattles.get(0));
            return;
        }

        // Check for pending battle invitations
        List<EloBattle> pendingBattles = battleManager.getPendingBattlesForPlayer(user.getId().asString());
        MultiLogger.logSystem("Found " + pendingBattles.size() + " pending battles for user", getClass());
        
        if (!pendingBattles.isEmpty()) {
            MultiLogger.logSystem("Showing pending battles", getClass());
            showPendingBattles(pendingBattles);
            return;
        }

        // Start new battle creation
        MultiLogger.logSystem("Starting new battle creation - showing battle menu", getClass());
        nextWindow("showBattleMenu");
        end();
    }

    /**
     * Shows the main battle menu with available options.
     */
    public void showBattleMenu() {
        MultiLogger.logSystem("showBattleMenu() called", getClass());
        
        String prompt = "**ELO Battle System**\n\n" +
                       "What would you like to do?\n\n" +
                       "• **Challenge Player** - Start a new battle\n" +
                       "• **View My Rating** - See your current ELO rating";

        String[] options = {"Challenge Player", "View My Rating"};

        withResponse(response -> {
            MultiLogger.logSystem("showBattleMenu() received response: '" + response + "'", getClass());
            MultiLogger.logSystem("Response length: " + response.length(), getClass());
            MultiLogger.logSystem("Available options: Challenge Player, View Voting Battles, View My Rating", getClass());
            
            // Try both exact match and lowercase
            String lowerResponse = response.toLowerCase();
            MultiLogger.logSystem("Lowercase response: '" + lowerResponse + "'", getClass());
            
            if (response.equals("Challenge Player") || lowerResponse.equals("challenge player")) {
                MultiLogger.logSystem("Switching to showOpponentSelection", getClass());
                nextWindow("showOpponentSelection");
            } else if (response.equals("View My Rating") || lowerResponse.equals("view my rating")) {
                MultiLogger.logSystem("Switching to showPlayerRating", getClass());
                nextWindow("showPlayerRating");
            } else {
                MultiLogger.logSystem("Unknown response received: '" + response + "' (length: " + response.length() + ")", getClass());
                // Let's try again with the battle menu
                nextWindow("showBattleMenu");
            }
        }, false, prompt, options);
    }

    /**
     * Shows opponent selection for challenging.
     */
    public void showOpponentSelection() {
        MultiLogger.logSystem("showOpponentSelection() called", getClass());
        
        String prompt = "**Challenge a Player**\n\n" +
                       "Mention (@) the player you want to challenge.\n\n" +
                       "**Example:** `@PlayerName`\n\n" +
                       "Type `cancel` to go back to the main menu.";

        // Use full response to access mentions
        sendMessage(prompt, "Challenge Player");
        
        // Wait for full response to extract mentions
        withFullResponse(response -> {
            MultiLogger.logSystem("showOpponentSelection() received full response", getClass());
            String messageContent = response.getResponse();
            MultiLogger.logSystem("Message content: '" + messageContent + "'", getClass());
            
            if (messageContent.toLowerCase().trim().equals("cancel")) {
                MultiLogger.logSystem("User cancelled opponent selection - returning to battle menu", getClass());
                nextWindow("showBattleMenu");
                return;
            }

            // Extract mentions from the message
            var mentions = response.getMessage().getMemberMentions();
            MultiLogger.logSystem("Found " + mentions.size() + " member mentions", getClass());
            
            if (mentions.isEmpty()) {
                MultiLogger.logSystem("No mentions found in message", getClass());
                sendMessage("Please mention a player using @ (e.g., `@PlayerName`) or type `cancel` to go back.", "No Mention Found");
                nextWindow("showOpponentSelection");
                return;
            }
            
            if (mentions.size() > 1) {
                MultiLogger.logSystem("Multiple mentions found, using first one", getClass());
                sendMessage("Multiple players mentioned. Using the first one: " + mentions.get(0).getDisplayName(), "Multiple Mentions");
            }
            
            // Use the first mentioned member
            var targetMember = mentions.get(0);
            String targetUserId = targetMember.getId().asString();
            MultiLogger.logSystem("Target player: " + targetMember.getDisplayName() + " (ID: " + targetUserId + ")", getClass());
            
            // Validate the target player
            if (targetMember.getId().equals(user.getId())) {
                MultiLogger.logSystem("User tried to challenge themselves", getClass());
                sendMessage("You cannot challenge yourself! Please mention another player.", "Invalid Challenge");
                nextWindow("showOpponentSelection");
                return;
            }
            
            if (targetMember.isBot()) {
                MultiLogger.logSystem("User tried to challenge a bot", getClass());
                sendMessage("You cannot challenge bots! Please mention a real player.", "Invalid Challenge");
                nextWindow("showOpponentSelection");
                return;
            }
            
            MultiLogger.logSystem("Valid target player found: " + targetMember.getDisplayName() + " (ID: " + targetUserId + ")", getClass());
            createBattle(targetUserId);
            nextWindow("showBattleCreated");
            
        }, true, prompt, "cancel");
    }

    /**
     * Shows confirmation that a battle was created and is waiting for acceptance.
     */
    public void showBattleCreated() {
        MultiLogger.logSystem("showBattleCreated() called", getClass());
        MultiLogger.logSystem("currentBattle is: " + (currentBattle != null ? "NOT NULL (ID: " + currentBattle.getBattleId() + ")" : "NULL"), getClass());
        
        if (currentBattle == null) {
            MultiLogger.logSystem("currentBattle is null, returning to battle menu", getClass());
            nextWindow("showBattleMenu");
            return;
        }

        Member opponent = ((GuildInteractionHandler) handler).getMemberById(currentBattle.getChallengedId());
        String opponentName = opponent != null ? opponent.getDisplayName() : "Unknown Player";

        String prompt = "**Battle Challenge Sent!**\n\n" +
                       "📤 Challenge sent to: **" + opponentName + "**\n" +
                       "🆔 Battle ID: `" + currentBattle.getBattleId() + "`\n" +
                       "⏰ Expires in 30 minutes\n\n" +
                       "Waiting for your opponent to accept the challenge...\n" +
                       "*You will be notified when they respond.*";

        String[] options = {"Cancel Challenge", "Refresh Status"};

        withResponse(response -> {
            MultiLogger.logSystem("showBattleCreated() received response: '" + response + "'", getClass());
            
            // Normalize response to handle case variations
            String normalizedResponse = response.toLowerCase();
            MultiLogger.logSystem("Normalized response: '" + normalizedResponse + "'", getClass());
            
            if (normalizedResponse.equals("cancel challenge")) {
                MultiLogger.logSystem("User selected Cancel Challenge - calling cancelBattle()", getClass());
                cancelBattle();
            } else if (normalizedResponse.equals("refresh status")) {
                MultiLogger.logSystem("User selected Refresh Status", getClass());
                // Refresh the battle state
                currentBattle = battleManager.get(currentBattle.getBattleId());
                if (currentBattle == null) {
                    MultiLogger.logSystem("Battle no longer exists after refresh", getClass());
                    sendMessage("Battle no longer exists.", "Battle Cancelled");
                    nextWindow("showBattleMenu");
                } else if (currentBattle.getStatus() == BattleStatus.IN_PROGRESS) {
                    MultiLogger.logSystem("Battle status changed to IN_PROGRESS", getClass());
                    nextWindow("showBattleInProgress");
                } else if (currentBattle.getStatus() == BattleStatus.CANCELLED) {
                    MultiLogger.logSystem("Battle status is CANCELLED", getClass());
                    sendMessage("Battle was cancelled by opponent.", "Battle Cancelled");
                    nextWindow("showBattleMenu");
                } else {
                    MultiLogger.logSystem("Battle status unchanged, staying on created screen", getClass());
                    nextWindow("showBattleCreated"); // Stay on this screen
                }
            } else {
                MultiLogger.logSystem("Unknown response received: '" + response + "' (normalized: '" + normalizedResponse + "')", getClass());
                // Stay on current screen
                nextWindow("showBattleCreated");
            }
        }, false, prompt, options);
    }

    /**
     * Shows pending battle invitations for the user to accept or decline.
     */
    public void showPendingBattles(List<EloBattle> pendingBattles) {
        MultiLogger.logSystem("showPendingBattles() called with " + pendingBattles.size() + " battles", getClass());
        
        if (pendingBattles.isEmpty()) {
            MultiLogger.logSystem("No pending battles, returning to battle menu", getClass());
            nextWindow("showBattleMenu");
            return;
        }

        EloBattle battle = pendingBattles.get(0); // Show first pending battle
        Member challenger = ((GuildInteractionHandler) handler).getMemberById(battle.getChallengerId());
        String challengerName = challenger != null ? challenger.getDisplayName() : "Unknown Player";

        MultiLogger.logSystem("Showing pending battle from: " + challengerName + " (ID: " + battle.getBattleId() + ")", getClass());

        String prompt = "**Battle Challenge Received!**\n\n" +
                       "⚔️ **" + challengerName + "** has challenged you to a battle!\n" +
                       "🆔 Battle ID: `" + battle.getBattleId() + "`\n" +
                       "📅 Created: " + (battle.getCreatedAt() != null ? battle.getCreatedAt().getFormattedDate() : "Unknown") + "\n\n" +
                       "Do you accept this challenge?";

        String[] options = {"Accept Challenge", "Decline Challenge"};

        MultiLogger.logSystem("showPendingBattles() created options: " + String.join(", ", options), getClass());

        withResponse(response -> {
            MultiLogger.logSystem("showPendingBattles() received response: '" + response + "'", getClass());
            String normalizedResponse = response.toLowerCase();
            MultiLogger.logSystem("Normalized response: '" + normalizedResponse + "'", getClass());
            
            if (normalizedResponse.equals("accept challenge")) {
                MultiLogger.logSystem("User selected Accept Challenge", getClass());
                if (battleManager.acceptBattle(battle.getBattleId(), user.getId().asString())) {
                    currentBattle = battleManager.get(battle.getBattleId());
                    MultiLogger.logSystem("Battle accepted, moving to showBattleInProgress", getClass());
                    nextWindow("showBattleInProgress");
                } else {
                    MultiLogger.logSystem("Failed to accept battle", getClass());
                    sendMessage("Failed to accept battle. It may have expired.", "Error");
                    nextWindow("showBattleMenu");
                }
            } else if (normalizedResponse.equals("decline challenge")) {
                MultiLogger.logSystem("User selected Decline Challenge", getClass());
                if (battleManager.declineBattle(battle.getBattleId(), user.getId().asString())) {
                    MultiLogger.logSystem("Battle declined successfully", getClass());
                    sendMessage("Battle challenge declined.", "Challenge Declined");
                } else {
                    MultiLogger.logSystem("Failed to decline battle", getClass());
                }
                MultiLogger.logSystem("Returning to battle menu after decline", getClass());
                nextWindow("showBattleMenu");
            } else {
                MultiLogger.logSystem("Unknown response received: '" + response + "' (normalized: '" + normalizedResponse + "')", getClass());
                MultiLogger.logSystem("Returning to battle menu due to unknown response", getClass());
                nextWindow("showBattleMenu");
            }
        }, false, prompt, options);
    }

    /**
     * Shows the battle in progress phase where players can report results.
     */
    public void showBattleInProgress() {
        MultiLogger.logSystem("showBattleInProgress() called", getClass());
        MultiLogger.logSystem("currentBattle is: " + (currentBattle != null ? "NOT NULL (ID: " + currentBattle.getBattleId() + ", Status: " + currentBattle.getStatus() + ")" : "NULL"), getClass());
        
        if (currentBattle == null || currentBattle.getStatus() != BattleStatus.IN_PROGRESS) {
            MultiLogger.logSystem("currentBattle is null or not IN_PROGRESS, returning to battle menu", getClass());
            nextWindow("showBattleMenu");
            return;
        }

        String prompt = "**Battle is Active!**\n\n" +
                       "⚔️ **Challenger:** " + getMemberDisplayName(currentBattle.getChallengerId()) + "\n" +
                       "🛡️ **Challenged:** " + getMemberDisplayName(currentBattle.getChallengedId()) + "\n" +
                       "🆔 Battle ID: `" + currentBattle.getBattleId() + "`\n\n" +
                       "**Instructions:**\n" +
                       "1. Battle your opponent on your preferred platform\n" +
                       "2. When finished, click 'Report Results' to submit your outcome\n" +
                       "3. Both players must report matching results to complete the battle\n\n" +
                       "Ready to report your battle results?";

        String[] options = {"Report Results", "Cancel Battle"};

        MultiLogger.logSystem("showBattleInProgress() created options: " + String.join(", ", options), getClass());

        withResponse(response -> {
            MultiLogger.logSystem("showBattleInProgress() received response: '" + response + "'", getClass());
            String normalizedResponse = response.toLowerCase();
            MultiLogger.logSystem("Normalized response: '" + normalizedResponse + "'", getClass());
            
            if (normalizedResponse.equals("report results")) {
                MultiLogger.logSystem("User selected Report Results", getClass());
                nextWindow("showReportResults");
            } else if (normalizedResponse.equals("cancel battle")) {
                MultiLogger.logSystem("User selected Cancel Battle", getClass());
                confirmCancelBattle();
            } else {
                MultiLogger.logSystem("Unknown response received: '" + response + "' (normalized: '" + normalizedResponse + "')", getClass());
                MultiLogger.logSystem("Staying on showBattleInProgress due to unknown response", getClass());
                nextWindow("showBattleInProgress");
            }
        }, false, prompt, options);
    }

    /**
     * Shows the result reporting interface where players submit their battle outcomes.
     */
    public void showReportResults() {
        MultiLogger.logSystem("showReportResults() called", getClass());
        
        // Refresh battle status to ensure we have the latest data
        if (currentBattle != null) {
            currentBattle = battleManager.get(currentBattle.getBattleId());
        }
        
        if (currentBattle == null) {
            MultiLogger.logSystem("currentBattle is null, returning to battle menu", getClass());
            nextWindow("showBattleMenu");
            return;
        }
        
        // Check if battle is already completed
        if (currentBattle.getStatus() == BattleStatus.COMPLETED) {
            MultiLogger.logSystem("Battle already completed, showing completion screen", getClass());
            sendMessage("This battle has already been completed!", "Battle Already Finished");
            showBattleCompleted();
            return;
        }
        
        // Check if battle is still in progress
        if (currentBattle.getStatus() != BattleStatus.IN_PROGRESS) {
            MultiLogger.logSystem("currentBattle is not IN_PROGRESS (status: " + currentBattle.getStatus() + "), returning to battle menu", getClass());
            nextWindow("showBattleMenu");
            return;
        }

        String prompt = "**Report Battle Results**\n\n" +
                       "🆔 Battle ID: `" + currentBattle.getBattleId() + "`\n" +
                       "⚔️ **Challenger:** " + getMemberDisplayName(currentBattle.getChallengerId()) + "\n" +
                       "🛡️ **Challenged:** " + getMemberDisplayName(currentBattle.getChallengedId()) + "\n\n" +
                       "**What was the outcome of your battle?**\n" +
                       "Please report your result honestly.";

        String[] options = {"I Won", "I Lost", "Back to Battle"};

        MultiLogger.logSystem("showReportResults() created options: " + String.join(", ", options), getClass());

        withResponse(response -> {
            MultiLogger.logSystem("showReportResults() received response: '" + response + "'", getClass());
            String normalizedResponse = response.toLowerCase();
            MultiLogger.logSystem("Normalized response: '" + normalizedResponse + "'", getClass());
            
            if (normalizedResponse.equals("i won")) {
                MultiLogger.logSystem("User reported they won", getClass());
                submitBattleResult(user.getId().asString(), true); // true = user won
            } else if (normalizedResponse.equals("i lost")) {
                MultiLogger.logSystem("User reported they lost", getClass());
                submitBattleResult(user.getId().asString(), false); // false = user lost
            } else if (normalizedResponse.equals("back to battle")) {
                MultiLogger.logSystem("User selected Back to Battle", getClass());
                nextWindow("showBattleInProgress");
            } else {
                MultiLogger.logSystem("Unknown response received: '" + response + "' (normalized: '" + normalizedResponse + "')", getClass());
                nextWindow("showReportResults");
            }
        }, false, prompt, options);
    }

    /**
     * Shows the battle completion screen with final results.
     */
    public void showBattleCompleted() {
        MultiLogger.logSystem("showBattleCompleted() called", getClass());
        
        if (currentBattle == null || currentBattle.getStatus() != BattleStatus.COMPLETED) {
            nextWindow("showBattleMenu");
            return;
        }

        String challengerName = getMemberDisplayName(currentBattle.getChallengerId());
        String challengedName = getMemberDisplayName(currentBattle.getChallengedId());
        String winnerId = currentBattle.getWinnerId();
        String winnerName = winnerId != null ? getMemberDisplayName(winnerId) : "Draw";

        String prompt = "**Battle Completed!**\n\n" +
                       "🆔 Battle ID: `" + currentBattle.getBattleId() + "`\n" +
                       "⚔️ **" + challengerName + "** vs **" + challengedName + "**\n\n" +
                       "🏆 **Winner: " + winnerName + "**\n\n" +
                       "✅ ELO ratings have been updated!";

        String[] options = {"Back to Menu"};

        withResponse(response -> {
            MultiLogger.logSystem("showBattleCompleted() received response: " + response, getClass());
            nextWindow("showBattleMenu");
        }, false, prompt, options);
    }

    /**
     * Submits the battle result and completes the battle immediately.
     * This method includes race condition protection to prevent double processing.
     */
    private void submitBattleResult(String userId, boolean userWon) {
        MultiLogger.logSystem("submitBattleResult() called for user: " + userId + ", userWon: " + userWon, getClass());
        
        try {
            // Refresh battle status to check for any changes
            currentBattle = battleManager.get(currentBattle.getBattleId());
            
            // Check if battle is still in progress
            if (currentBattle == null || currentBattle.getStatus() != BattleStatus.IN_PROGRESS) {
                MultiLogger.logSystem("Battle is no longer in progress (status: " + 
                    (currentBattle != null ? currentBattle.getStatus() : "NULL") + "), cannot submit result", getClass());
                
                if (currentBattle != null && currentBattle.getStatus() == BattleStatus.COMPLETED) {
                    sendMessage("This battle has already been completed by the other player!", "Battle Already Finished");
                    showBattleCompleted();
                } else {
                    sendMessage("This battle is no longer active. Please check the battle status.", "Battle Inactive");
                    nextWindow("showBattleMenu");
                }
                return;
            }
            
            // Determine winner ID based on user's result
            String winnerId = null;
            if (userWon) {
                winnerId = userId;
            } else {
                // User lost, so opponent won
                winnerId = currentBattle.getChallengerId().equals(userId) ? 
                    currentBattle.getChallengedId() : currentBattle.getChallengerId();
            }

            MultiLogger.logSystem("Determined winner ID: " + winnerId, getClass());

            // Complete the battle using the existing method (which now prevents double processing)
            boolean battleCompleted = battleManager.completeBattle(currentBattle.getBattleId(), winnerId);
            
            if (battleCompleted) {
                MultiLogger.logSystem("Battle completed successfully", getClass());
                // Refresh battle to get updated info
                currentBattle = battleManager.get(currentBattle.getBattleId());
                sendMessage("Battle result submitted successfully!\nELO ratings have been updated.", "Result Recorded");
                showBattleCompleted();
            } else {
                MultiLogger.logSystem("Battle completion failed", getClass());
                sendMessage("Failed to complete battle. It may have already been completed by the other player.", "Battle Already Completed");
                // Try to refresh and show completion if it exists
                currentBattle = battleManager.get(currentBattle.getBattleId());
                if (currentBattle != null && currentBattle.getStatus() == BattleStatus.COMPLETED) {
                    showBattleCompleted();
                } else {
                    nextWindow("showBattleMenu");
                }
            }
            
        } catch (Exception e) {
            MultiLogger.logSystem("Error submitting battle result: " + e.getMessage(), getClass());
            e.printStackTrace();
            sendMessage("An error occurred while submitting your result. Please try again.", "Error");
            nextWindow("showReportResults");
        }
    }

    /**
     * Shows current battle status information.
     */
    public void showBattleStatus() {
        MultiLogger.logSystem("showBattleStatus() called", getClass());
        MultiLogger.logSystem("currentBattle is: " + (currentBattle != null ? "NOT NULL (ID: " + currentBattle.getBattleId() + ")" : "NULL"), getClass());
        
        if (currentBattle == null) {
            MultiLogger.logSystem("currentBattle is null, returning to battle menu", getClass());
            nextWindow("showBattleMenu");
            return;
        }

        // Refresh battle from database
        MultiLogger.logSystem("Refreshing battle from database", getClass());
        currentBattle = battleManager.get(currentBattle.getBattleId());
        if (currentBattle == null) {
            MultiLogger.logSystem("Battle no longer exists after refresh", getClass());
            sendMessage("Battle no longer exists.", "Battle Not Found");
            nextWindow("showBattleMenu");
            return;
        }

        MultiLogger.logSystem("Battle status: " + currentBattle.getStatus(), getClass());

        String prompt = "**Battle Status**\n\n" +
                       "🆔 Battle ID: `" + currentBattle.getBattleId() + "`\n" +
                       "📊 Status: " + formatBattleStatus(currentBattle.getStatus()) + "\n" +
                       "⚔️ Challenger: " + getMemberDisplayName(currentBattle.getChallengerId()) + "\n" +
                       "🛡️ Challenged: " + getMemberDisplayName(currentBattle.getChallengedId()) + "\n";

        String[] options = {"Refresh", "Back to Menu"};

        MultiLogger.logSystem("showBattleStatus() created options: " + String.join(", ", options), getClass());

        withResponse(response -> {
            MultiLogger.logSystem("showBattleStatus() received response: '" + response + "'", getClass());
            String normalizedResponse = response.toLowerCase();
            MultiLogger.logSystem("Normalized response: '" + normalizedResponse + "'", getClass());
            
            if (normalizedResponse.equals("refresh")) {
                MultiLogger.logSystem("User selected Refresh - calling showBattleStatus again", getClass());
                nextWindow("showBattleStatus");
            } else if (normalizedResponse.equals("back to menu")) {
                MultiLogger.logSystem("User selected Back to Menu - calling nextWindow('showBattleMenu')", getClass());
                nextWindow("showBattleMenu");
            } else {
                MultiLogger.logSystem("Unknown response received: '" + response + "' (normalized: '" + normalizedResponse + "')", getClass());
                MultiLogger.logSystem("Staying on battle status due to unknown response", getClass());
                nextWindow("showBattleStatus");
            }
        }, false, prompt, options);
    }

    /**
     * Shows the current player's ELO rating and statistics.
     */
    public void showPlayerRating() {
        MultiLogger.logSystem("showPlayerRating() called", getClass());
        
        EloRating rating = eloManager.getOrCreateRating(user.getId().asString());
        MultiLogger.logSystem("Retrieved rating for user: " + rating.getRating(), getClass());
        
        String prompt = "**Your ELO Rating**\n\n" +
                       "🏆 **Current Rating:** " + rating.getRating() + "\n" +
                       "🎖️ **Rank:** " + eloManager.getRankForRating(rating.getRating()).name() + "\n" +
                       "📊 **Statistics:**\n" +
                       "• Matches Played: " + rating.getMatchesPlayed() + "\n" +
                       "• Wins: " + rating.getWins() + "\n" +
                       "• Losses: " + rating.getLosses() + "\n" +
                       "• Win Streak: " + rating.getWinStreak() + "\n" +
                       "• Best Rating: " + rating.getBestRating();

        String[] options = {"Back to Menu"};

        withResponse(response -> {
            MultiLogger.logSystem("showPlayerRating() received response: " + response, getClass());
            nextWindow("showBattleMenu");
        }, false, prompt, options);
    }

    /**
     * Resumes an existing battle wizard based on battle state.
     */
    protected void resumeBattleWizard(EloBattle battle) {
        this.currentBattle = battle;
        
        switch (battle.getStatus()) {
            case PENDING_ACCEPTANCE:
                if (battle.getChallengerId().equals(user.getId().asString())) {
                    nextWindow("showBattleCreated");
                } else {
                    showPendingBattles(List.of(battle));
                }
                break;
            case IN_PROGRESS:
                nextWindow("showBattleInProgress");
                break;
            case COMPLETED:
                nextWindow("showBattleCompleted");
                break;
            default:
                nextWindow("showBattleMenu");
                break;
        }
        end();
    }

    /**
     * Creates a new battle with the specified opponent.
     */
    private void createBattle(String opponentId) {
        MultiLogger.logSystem("createBattle() called with opponent ID: " + opponentId, getClass());
        
        try {
            currentBattle = battleManager.createBattle(user.getId().asString(), opponentId);
            MultiLogger.logSystem("Battle created successfully: " + currentBattle.getBattleId(), getClass());
            
            wizardState = wizardStateManager.createWizardState(user.getId().asString());
            wizardState.setBattleId(currentBattle.getBattleId());
            wizardState.setChallengedPlayerId(opponentId);
            wizardStateManager.update(wizardState);
            MultiLogger.logSystem("Wizard state created and updated", getClass());
        } catch (Exception e) {
            MultiLogger.logSystem("Error creating battle: " + e.getMessage(), getClass());
            e.printStackTrace();
        }
    }

    /**
     * Cancels the current battle.
     */
    private void cancelBattle() {
        MultiLogger.logSystem("cancelBattle() called", getClass());
        MultiLogger.logSystem("currentBattle is: " + (currentBattle != null ? "NOT NULL (ID: " + currentBattle.getBattleId() + ")" : "NULL"), getClass());
        MultiLogger.logSystem("wizardState is: " + (wizardState != null ? "NOT NULL" : "NULL"), getClass());
        
        if (currentBattle != null) {
            MultiLogger.logSystem("Calling battleManager.cancelBattle() with battleId: " + currentBattle.getBattleId(), getClass());
            
            try {
                boolean cancelResult = battleManager.cancelBattle(currentBattle.getBattleId());
                MultiLogger.logSystem("cancelBattle() returned: " + cancelResult, getClass());
            } catch (Exception e) {
                MultiLogger.logSystem("ERROR in cancelBattle(): " + e.getMessage(), getClass());
                e.printStackTrace();
            }
            
            if (wizardState != null) {
                MultiLogger.logSystem("Removing wizard state for user: " + wizardState.getUserId(), getClass());
                try {
                    wizardStateManager.removeUserWizardState(wizardState.getUserId());
                    MultiLogger.logSystem("Wizard state removed successfully", getClass());
                } catch (Exception e) {
                    MultiLogger.logSystem("ERROR removing wizard state: " + e.getMessage(), getClass());
                    e.printStackTrace();
                }
            }
            
            MultiLogger.logSystem("Sending cancellation message to user", getClass());
            sendMessage("Battle challenge cancelled.", "Battle Cancelled");
            MultiLogger.logSystem("Cancellation message sent", getClass());
        } else {
            MultiLogger.logSystem("currentBattle is null, skipping decline operation", getClass());
        }
        
        MultiLogger.logSystem("Switching to showBattleMenu", getClass());
        nextWindow("showBattleMenu");
        MultiLogger.logSystem("cancelBattle() completed", getClass());
    }

    /**
     * Shows confirmation dialog for cancelling a battle.
     */
    private void confirmCancelBattle() {
        MultiLogger.logSystem("confirmCancelBattle() called", getClass());
        MultiLogger.logSystem("currentBattle is: " + (currentBattle != null ? "NOT NULL (ID: " + currentBattle.getBattleId() + ", Status: " + currentBattle.getStatus() + ")" : "NULL"), getClass());
        
        String prompt = "**Confirm Battle Cancellation**\n\n" +
                       "Are you sure you want to cancel this battle?\n" +
                       "This action cannot be undone.";

        String[] options = {"Yes, Cancel Battle", "No, Go Back"};

        MultiLogger.logSystem("Showing confirmation dialog with options: " + String.join(", ", options), getClass());

        withResponse(response -> {
            MultiLogger.logSystem("confirmCancelBattle() received response: '" + response + "'", getClass());
            String normalizedResponse = response.toLowerCase();
            MultiLogger.logSystem("Normalized response: '" + normalizedResponse + "'", getClass());
            
            if (normalizedResponse.equals("yes, cancel battle")) {
                MultiLogger.logSystem("User confirmed cancellation - calling cancelBattle()", getClass());
                cancelBattle();
            } else if (normalizedResponse.equals("no, go back")) {
                MultiLogger.logSystem("User chose to go back - returning to showBattleInProgress", getClass());
                nextWindow("showBattleInProgress");
            } else {
                MultiLogger.logSystem("Unknown response received: '" + response + "' (normalized: '" + normalizedResponse + "')", getClass());
                MultiLogger.logSystem("Returning to showBattleInProgress due to unknown response", getClass());
                nextWindow("showBattleInProgress");
            }
        }, false, prompt, options);
    }

    /**
     * Gets display name for a member ID.
     */
    private String getMemberDisplayName(String memberId) {
        Member member = ((GuildInteractionHandler) handler).getMemberById(memberId);
        return member != null ? member.getDisplayName() : "Unknown Player";
    }

    /**
     * Formats battle status for display.
     */
    private String formatBattleStatus(BattleStatus status) {
        switch (status) {
            case PENDING_ACCEPTANCE: return "⏳ Waiting for Acceptance";
            case IN_PROGRESS: return "⚔️ Battle Active";
            case COMPLETED: return "✅ Completed";
            case CANCELLED: return "❌ Cancelled";
            case EXPIRED: return "⏰ Expired";
            default: return status.name();
        }
    }
}
