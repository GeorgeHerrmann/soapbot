package com.georgster.elo.wizard;

import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.elo.EloCalculator;
import com.georgster.elo.EloRating;
import com.georgster.elo.manager.EloManager;
import com.georgster.profile.UserProfile;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.Member;

/**
 * An {@link InputWizard} for creating and managing Elo battles.
 * Provides a user-friendly interface for setting up battles with wagering options.
 */
public class BattleWizard extends InputWizard {
    private static final String TITLE = "Elo Battle Wizard";

    private final EloManager eloManager;
    private final GuildInteractionHandler guildHandler;
    
    // Battle configuration state
    private String creatorId;
    private String opponentId;
    private Member opponent;
    private long coinWager = 0;
    private boolean eloStakes = false;
    private String battleType = "1v1";

    /**
     * Creates a new BattleWizard for setting up Elo battles.
     * 
     * @param event Command execution event that triggered the wizard
     * @param eloManager The EloManager for this guild
     */
    public BattleWizard(CommandExecutionEvent event, EloManager eloManager) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eloManager = eloManager;
        this.guildHandler = event.getGuildInteractionHandler();
        this.creatorId = event.getDiscordEvent().getAuthorAsMember().getId().asString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin() {
        nextWindow("chooseBattleType");
        end();
    }

    /**
     * First step: Choose the type of battle to create.
     */
    protected void chooseBattleType() {
        withResponse(response -> {
            switch (response.toLowerCase()) {
                case "quick match":
                    battleType = "quick";
                    nextWindow("findQuickOpponent");
                    break;
                case "challenge player":
                    battleType = "challenge";
                    nextWindow("selectOpponent");
                    break;
                case "tournament":
                    battleType = "tournament";
                    sendMessage("Tournament battles coming in Phase 2!", TITLE);
                    nextWindow("chooseBattleType");
                    break;
                default:
                    sendMessage("Invalid option. Please try again.", TITLE);
                    nextWindow("chooseBattleType");
            }
        }, false, "What type of battle would you like to create?", 
        "Quick Match", "Challenge Player", "Tournament");
    }

    /**
     * Find a suitable opponent for quick match based on rating proximity.
     */
    protected void findQuickOpponent() {
        EloRating userRating = eloManager.getOrCreateRating(creatorId);
        List<EloRating> suitableOpponents = eloManager.findSuitableOpponents(creatorId, true);
        
        if (suitableOpponents.isEmpty()) {
            sendMessage("No suitable opponents found for quick match. Try challenging a specific player instead.", TITLE);
            nextWindow("chooseBattleType");
            return;
        }
        
        // Show top 3 suitable opponents
        String[] opponentOptions = new String[Math.min(3, suitableOpponents.size()) + 1];
        StringBuilder description = new StringBuilder("**Suitable opponents based on your rating (" + userRating.getRating() + "):**\n\n");
        
        for (int i = 0; i < Math.min(3, suitableOpponents.size()); i++) {
            EloRating rating = suitableOpponents.get(i);
            Member member = guildHandler.getMemberById(rating.getMemberId());
            String username = member != null ? member.getDisplayName() : "Unknown User";
            
            double winProb = EloCalculator.calculateWinProbability(userRating.getRating(), rating.getRating());
            description.append(String.format("%d. **%s** - %d rating (%.1f%% win chance)\n", 
                i + 1, username, rating.getRating(), winProb));
            
            opponentOptions[i] = username;
        }
        opponentOptions[opponentOptions.length - 1] = "Back";
        
        withResponse(response -> {
            if (response.equals("Back")) {
                nextWindow("chooseBattleType");
                return;
            }
            
            // Find the selected opponent
            for (int i = 0; i < Math.min(3, suitableOpponents.size()); i++) {
                Member member = guildHandler.getMemberById(suitableOpponents.get(i).getMemberId());
                if (member != null && member.getDisplayName().equals(response)) {
                    opponent = member;
                    opponentId = member.getId().asString();
                    nextWindow("selectWagerType");
                    return;
                }
            }
            
            sendMessage("Invalid selection. Please try again.", TITLE);
            nextWindow("findQuickOpponent");
        }, false, description.toString(), opponentOptions);
    }

    /**
     * Manual opponent selection for challenge battles.
     */
    protected void selectOpponent() {
        withResponse(response -> {
            if (response.equals("back")) {
                nextWindow("chooseBattleType");
                return;
            }
            
            // Try to find the member by various identifiers
            Member target = guildHandler.getMemberByName(response);
            if (target == null) target = guildHandler.getMemberByTag(response);
            if (target == null) target = guildHandler.getMemberById(response);
            
            if (target == null) {
                sendMessage("User not found: " + response + ". Please try again.", TITLE);
                nextWindow("selectOpponent");
                return;
            }
            
            if (target.getId().asString().equals(creatorId)) {
                sendMessage("You cannot challenge yourself! Please select a different opponent.", TITLE);
                nextWindow("selectOpponent");
                return;
            }
            
            opponent = target;
            opponentId = target.getId().asString();
            nextWindow("showMatchPreview");
            
        }, true, "Enter the username, tag, or ID of the player you want to challenge:", "back");
    }

    /**
     * Show a preview of the match with win probabilities and rating changes.
     */
    protected void showMatchPreview() {
        EloRating userRating = eloManager.getOrCreateRating(creatorId);
        EloRating opponentRating = eloManager.getOrCreateRating(opponentId);
        
        double userWinProb = EloCalculator.calculateWinProbability(userRating.getRating(), opponentRating.getRating());
        int userWinGain = EloCalculator.estimateRatingChange(userRating, opponentRating, true);
        int userLossChange = EloCalculator.estimateRatingChange(userRating, opponentRating, false);
        
        String matchQuality = EloCalculator.isBalancedMatch(userRating.getRating(), opponentRating.getRating()) ? 
            "⚖️ Balanced Match" : "⚠️ Unbalanced Match";
        
        String preview = String.format(
            "**Battle Preview: You vs %s**\n\n" +
            "**Your Stats:**\n" +
            "Rating: %d (%s)\n" +
            "Win Chance: %.1f%%\n" +
            "If you win: +%d points\n" +
            "If you lose: %d points\n\n" +
            "**Opponent Stats:**\n" +
            "Rating: %d (%s)\n" +
            "Win Chance: %.1f%%\n\n" +
            "%s",
            opponent.getDisplayName(),
            userRating.getRating(), userRating.getRank().getDisplayName(),
            userWinProb, userWinGain, userLossChange,
            opponentRating.getRating(), opponentRating.getRank().getDisplayName(),
            100.0 - userWinProb,
            matchQuality
        );
        
        withResponse(response -> {
            switch (response.toLowerCase()) {
                case "proceed":
                    nextWindow("selectWagerType");
                    break;
                case "choose different opponent":
                    nextWindow("selectOpponent");
                    break;
                case "cancel":
                    sendMessage("Battle creation cancelled.", TITLE);
                    break;
                default:
                    sendMessage("Invalid option. Please try again.", TITLE);
                    nextWindow("showMatchPreview");
            }
        }, false, preview, "Proceed", "Choose Different Opponent", "Cancel");
    }

    /**
     * Select the type of wager for the battle.
     */
    protected void selectWagerType() {
        withResponse(response -> {
            switch (response.toLowerCase()) {
                case "no wager":
                    coinWager = 0;
                    eloStakes = false;
                    nextWindow("confirmBattle");
                    break;
                case "coin wager":
                    nextWindow("setCoinWager");
                    break;
                case "elo stakes":
                    eloStakes = true;
                    coinWager = 0;
                    nextWindow("confirmBattle");
                    break;
                case "back":
                    nextWindow("showMatchPreview");
                    break;
                default:
                    sendMessage("Invalid option. Please try again.", TITLE);
                    nextWindow("selectWagerType");
            }
        }, false, "What type of wager would you like for this battle?",
        "No Wager", "Coin Wager", "Elo Stakes", "Back");
    }

    /**
     * Set the coin wager amount.
     */
    protected void setCoinWager() {
        UserProfile userProfile = event.getUserProfileManager().get(creatorId);
        long userBalance = userProfile.getBank().getBalance();
        
        String prompt = String.format(
            "How many coins would you like to wager?\n" +
            "Your current balance: %d coins\n" +
            "Enter amount or 'back' to return:",
            userBalance
        );
        
        withResponse(response -> {
            if (response.equalsIgnoreCase("back")) {
                nextWindow("selectWagerType");
                return;
            }
            
            try {
                long amount = Long.parseLong(response);
                
                if (amount <= 0) {
                    sendMessage("Wager amount must be positive. Please try again.", TITLE);
                    nextWindow("setCoinWager");
                    return;
                }
                
                if (amount > userBalance) {
                    sendMessage("You don't have enough coins. Please enter a smaller amount.", TITLE);
                    nextWindow("setCoinWager");
                    return;
                }
                
                coinWager = amount;
                eloStakes = false;
                nextWindow("confirmBattle");
                
            } catch (NumberFormatException e) {
                sendMessage("Invalid number format. Please enter a valid coin amount.", TITLE);
                nextWindow("setCoinWager");
            }
        }, true, prompt);
    }

    /**
     * Final confirmation before creating the battle.
     */
    protected void confirmBattle() {
        String wagerDescription;
        if (eloStakes) {
            wagerDescription = "Elo stakes (additional Elo points on the line)";
        } else if (coinWager > 0) {
            wagerDescription = coinWager + " coins";
        } else {
            wagerDescription = "No wager";
        }
        
        String confirmation = String.format(
            "**Confirm Battle Creation**\n\n" +
            "**Type:** %s\n" +
            "**Opponent:** %s\n" +
            "**Wager:** %s\n\n" +
            "Create this battle?",
            battleType.equals("quick") ? "Quick Match" : "Challenge",
            opponent.getDisplayName(),
            wagerDescription
        );
        
        withResponse(response -> {
            switch (response.toLowerCase()) {
                case "create battle":
                    createBattle();
                    break;
                case "modify":
                    nextWindow("selectWagerType");
                    break;
                case "cancel":
                    sendMessage("Battle creation cancelled.", TITLE);
                    break;
                default:
                    sendMessage("Invalid option. Please try again.", TITLE);
                    nextWindow("confirmBattle");
            }
        }, false, confirmation, "Create Battle", "Modify", "Cancel");
    }

    /**
     * Creates the battle and sends notification to the opponent.
     */
    private void createBattle() {
        // For Phase 1, we'll just simulate a battle and process the result immediately
        // In Phase 2, this will create an actual EloBattle object and wait for acceptance
        
        String battleSummary = String.format(
            "🏆 **Battle Created!**\n\n" +
            "**Participants:** %s vs %s\n" +
            "**Type:** %s\n" +
            "**Wager:** %s\n\n" +
            "*In Phase 1, this is a simulation. Use `!elotest declareWinner` to process results.*",
            guildHandler.getMemberById(creatorId).getDisplayName(),
            opponent.getDisplayName(),
            battleType.equals("quick") ? "Quick Match" : "Challenge",
            eloStakes ? "Elo stakes" : (coinWager > 0 ? coinWager + " coins" : "No wager")
        );
        
        sendMessage(battleSummary, "Battle Created");
        
        // Notify the opponent
        String opponentNotification = String.format(
            "🥊 **Battle Challenge!**\n\n" +
            "%s has challenged you to an Elo battle!\n" +
            "Type: %s\n" +
            "Wager: %s\n\n" +
            "*Battle system is in Phase 1 - use `!elotest declareWinner` when ready.*",
            guildHandler.getMemberById(creatorId).getDisplayName(),
            battleType.equals("quick") ? "Quick Match" : "Challenge",
            eloStakes ? "Elo stakes" : (coinWager > 0 ? coinWager + " coins" : "No wager")
        );
        
        try {
            opponent.getPrivateChannel().block().createMessage(opponentNotification).block();
        } catch (Exception e) {
            // If DM fails, send in channel
            guildHandler.sendMessage(opponent.getMention() + " " + opponentNotification);
        }
    }
}
