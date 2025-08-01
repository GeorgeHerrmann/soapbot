package com.georgster.elo;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.elo.manager.EloManager;
import com.georgster.elo.manager.EloBattleManager;
import com.georgster.elo.model.EloRating;
import com.georgster.elo.model.EloRank;
import com.georgster.elo.model.EloBattle;
import com.georgster.elo.wizard.BattleWizard;
import com.georgster.logs.LogDestination;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.IterableStringWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A {@link ParseableCommand} for interacting with the Elo rating system.
 */
public class EloCommand implements ParseableCommand {

    private final EloManager eloManager;
    private final EloBattleManager battleManager;

    /**
     * Creates a new EloCommand from the provided context.
     * 
     * @param context The context for this command's SOAPClient.
     */
    public EloCommand(ClientContext context) {
        this.eloManager = context.getEloManager();
        this.battleManager = context.getEloBattleManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        SubcommandSystem subcommands = event.createSubcommandSystem();

        // Handle rank/rating display (with optional user mention)
        subcommands.on(p -> {
            String targetUserId;
            String targetUsername;
            
            // Try to get mentioned user from Discord message
            List<User> userMentions = event.getDiscordEvent().getPresentUsers();
            if (!userMentions.isEmpty()) {
                User targetUser = userMentions.get(0);
                targetUserId = targetUser.getId().asString();
                Member targetMember = handler.getMemberById(targetUserId);
                targetUsername = targetMember != null ? targetMember.getDisplayName() : targetUser.getUsername();
            } else {
                targetUserId = event.getDiscordEvent().getUser().getId().asString();
                targetUsername = event.getDiscordEvent().getAuthorAsMember().getDisplayName();
            }

            showUserRating(event, targetUserId, targetUsername);
        }, "rank", "rating", "r");

        // Handle leaderboard
        subcommands.on(p -> {
            showLeaderboard(event);
        }, "leaderboard", "lb", "top");

        // Handle battle command
        subcommands.on(p -> {
            handleBattleCommand(event);
        }, "battle", "fight", "duel");

        // Handle battle history
        subcommands.on(p -> {
            String targetUserId;
            String targetUsername;
            
            // Try to get mentioned user from Discord message
            List<User> userMentions = event.getDiscordEvent().getPresentUsers();
            if (!userMentions.isEmpty()) {
                User targetUser = userMentions.get(0);
                targetUserId = targetUser.getId().asString();
                Member targetMember = handler.getMemberById(targetUserId);
                targetUsername = targetMember != null ? targetMember.getDisplayName() : targetUser.getUsername();
            } else {
                targetUserId = event.getDiscordEvent().getUser().getId().asString();
                targetUsername = event.getDiscordEvent().getAuthorAsMember().getDisplayName();
            }

            showBattleHistory(event, targetUserId, targetUsername);
        }, "history", "h", "battles");

        // Handle detailed stats
        subcommands.on(p -> {
            String targetUserId;
            String targetUsername;
            
            // Try to get mentioned user from Discord message
            List<User> userMentions = event.getDiscordEvent().getPresentUsers();
            if (!userMentions.isEmpty()) {
                User targetUser = userMentions.get(0);
                targetUserId = targetUser.getId().asString();
                Member targetMember = handler.getMemberById(targetUserId);
                targetUsername = targetMember != null ? targetMember.getDisplayName() : targetUser.getUsername();
            } else {
                targetUserId = event.getDiscordEvent().getUser().getId().asString();
                targetUsername = event.getDiscordEvent().getAuthorAsMember().getDisplayName();
            }

            showDetailedStats(event, targetUserId, targetUsername);
        }, "stats", "statistics", "s");

        // Handle help command
        subcommands.on(p -> {
            showHelpMenu(event);
        }, "help", "h", "?");

        // Default case - show user's own rating when no arguments
        subcommands.on(() -> {
            String userId = event.getDiscordEvent().getUser().getId().asString();
            String username = event.getDiscordEvent().getAuthorAsMember().getDisplayName();
            showUserRating(event, userId, username);
        });
    }

    /**
     * Shows a user's Elo rating and rank information.
     */
    private void showUserRating(CommandExecutionEvent event, String userId, String username) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        EloRating rating = eloManager.getOrCreateRating(userId);
        
        event.getLogger().append("- Displaying Elo rating for user: " + username, LogDestination.NONAPI);
        
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(username).append("'s Elo Rating**\n\n");
        sb.append("**Rating:** ").append(rating.getRating()).append("\n");
        sb.append("**Rank:** ").append(rating.getRank().getDisplayName()).append("\n");
        sb.append("**Position:** #").append(eloManager.getLeaderboardPosition(userId)).append("\n\n");
        
        sb.append("**Record:**\n");
        sb.append("Matches Played: ").append(rating.getMatchesPlayed()).append("\n");
        sb.append("Wins: ").append(rating.getWins()).append("\n");
        sb.append("Losses: ").append(rating.getLosses()).append("\n");
        
        if (rating.getMatchesPlayed() > 0) {
            double winRate = (double) rating.getWins() / rating.getMatchesPlayed() * 100;
            sb.append("Win Rate: ").append(String.format("%.1f%%", winRate)).append("\n");
        }
        
        sb.append("\n**Statistics:**\n");
        sb.append("Current Win Streak: ").append(rating.getWinStreak()).append("\n");
        sb.append("Best Rating: ").append(rating.getBestRating()).append("\n");
        
        if (rating.getLastMatch() != null) {
            sb.append("Last Match: ").append(rating.getLastMatch().getFormattedDate()).append("\n");
        }

        handler.sendMessage(sb.toString(), username + "'s Elo Profile");
    }

    /**
     * Shows the server's Elo leaderboard.
     */
    private void showLeaderboard(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        List<EloRating> leaderboard = eloManager.getLeaderboard();
        
        event.getLogger().append("- Displaying Elo leaderboard for " + handler.getGuild().getName(), LogDestination.NONAPI);
        
        if (leaderboard.isEmpty()) {
            handler.sendMessage("No players have played any ranked battles yet!", "Elo Leaderboard");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("**").append(handler.getGuild().getName()).append(" Elo Leaderboard**\n\n");
        
        for (int i = 0; i < Math.min(leaderboard.size(), 25); i++) {
            EloRating rating = leaderboard.get(i);
            Member member = handler.getMemberById(rating.getMemberId());
            String displayName = member != null ? member.getDisplayName() : "Unknown User";
            
            sb.append("**").append(i + 1).append(".** ");
            sb.append(displayName).append(" - ");
            sb.append(rating.getRating()).append(" ");
            sb.append("(").append(rating.getWins()).append("W/").append(rating.getLosses()).append("L)");
            sb.append("\n");
        }
        
        // Split into pages if needed
        List<String> pages = SoapUtility.splitAtEvery(sb.toString(), 15);
        InputWizard wizard = new IterableStringWizard(event, handler.getGuild().getName() + " Elo Leaderboard", pages);
        wizard.begin();
    }

    /**
     * Starts a new battle wizard.
     */
    private void handleBattleCommand(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        event.getLogger().append("- Starting battle wizard", LogDestination.NONAPI);
        
        // Check for existing active battle
        String userId = event.getDiscordEvent().getUser().getId().asString();
        boolean hasActiveBattle = battleManager.isPlayerInActiveBattle(userId);
        
        if (hasActiveBattle) {
            handler.sendMessage("You already have an active battle! Use the battle interface to continue, or wait for it to complete.", "Active Battle");
            return;
        }
        
        // Start new battle wizard
        BattleWizard wizard = new BattleWizard(event);
        wizard.begin();
    }

    /**
     * Shows a user's battle history.
     */
    private void showBattleHistory(CommandExecutionEvent event, String userId, String username) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        List<EloBattle> history = battleManager.getBattlesForPlayer(userId);
        
        event.getLogger().append("- Displaying battle history for user: " + username, LogDestination.NONAPI);
        
        if (history.isEmpty()) {
            handler.sendMessage(username + " has not participated in any battles yet.", username + "'s Battle History");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("**").append(username).append("'s Battle History**\n\n");
        
        for (int i = 0; i < Math.min(history.size(), 20); i++) {
            EloBattle battle = history.get(i);
            sb.append("**").append(i + 1).append(".** ");
            
            // Determine opponent
            String opponentId = battle.getChallengerId().equals(userId) ? battle.getChallengedId() : battle.getChallengerId();
            Member opponent = handler.getMemberById(opponentId);
            String opponentName = opponent != null ? opponent.getDisplayName() : "Unknown User";
            
            // Battle result
            String result;
            if (battle.getWinnerId() == null) {
                result = "No Result";
            } else if (battle.getWinnerId().equals(userId)) {
                result = "**WIN**";
            } else {
                result = "LOSS";
            }
            
            sb.append("vs ").append(opponentName).append(" - ").append(result);
            sb.append(" (").append(battle.getStatus().toString()).append(")");
            
            if (battle.getCreatedAt() != null) {
                sb.append(" - ").append(battle.getCreatedAt().getFormattedDate());
            }
            
            sb.append("\n");
        }
        
        if (history.size() > 20) {
            sb.append("\n*Showing most recent 20 battles*");
        }

        // Split into pages if needed
        List<String> pages = SoapUtility.splitAtEvery(sb.toString(), 15);
        InputWizard wizard = new IterableStringWizard(event, username + "'s Battle History", pages);
        wizard.begin();
    }

    /**
     * Shows detailed statistics for a user.
     */
    private void showDetailedStats(CommandExecutionEvent event, String userId, String username) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        EloRating rating = eloManager.getOrCreateRating(userId);
        List<EloBattle> history = battleManager.getBattlesForPlayer(userId);
        
        event.getLogger().append("- Displaying detailed stats for user: " + username, LogDestination.NONAPI);
        
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(username).append("'s Detailed Statistics**\n\n");
        
        // Basic Rating Info
        sb.append("**Current Rating:** ").append(rating.getRating()).append("\n");
        sb.append("**Rank:** ").append(rating.getRank().getDisplayName()).append("\n");
        sb.append("**Leaderboard Position:** #").append(eloManager.getLeaderboardPosition(userId)).append("\n\n");
        
        // Match Statistics
        sb.append("**Match Statistics:**\n");
        sb.append("Total Matches: ").append(rating.getMatchesPlayed()).append("\n");
        sb.append("Wins: ").append(rating.getWins()).append("\n");
        sb.append("Losses: ").append(rating.getLosses()).append("\n");
        
        if (rating.getMatchesPlayed() > 0) {
            double winRate = (double) rating.getWins() / rating.getMatchesPlayed() * 100;
            sb.append("Win Rate: ").append(String.format("%.2f%%", winRate)).append("\n");
        }
        
        sb.append("\n**Performance:**\n");
        sb.append("Current Win Streak: ").append(rating.getWinStreak()).append("\n");
        sb.append("Best Rating: ").append(rating.getBestRating()).append("\n");
        
        // Calculate rating change from start
        int ratingChange = rating.getRating() - 1500;
        sb.append("Rating Change from Start: ");
        if (ratingChange > 0) {
            sb.append("+").append(ratingChange);
        } else {
            sb.append(ratingChange);
        }
        sb.append("\n");
        
        // Battle counts
        int completedBattles = battleManager.getCompletedBattlesCount(userId);
        int wonBattles = battleManager.getWonBattlesCount(userId);
        
        sb.append("\n**Battle Breakdown:**\n");
        sb.append("Total Battles: ").append(history.size()).append("\n");
        sb.append("Completed Battles: ").append(completedBattles).append("\n");
        sb.append("Won Battles: ").append(wonBattles).append("\n");
        
        if (rating.getLastMatch() != null) {
            sb.append("Last Battle: ").append(rating.getLastMatch().getFormattedDate()).append("\n");
        }
        
        // Rank progress
        EloRank currentRank = rating.getRank();
        if (currentRank != EloRank.GRANDMASTER) {
            EloRank nextRank = EloRank.values()[currentRank.ordinal() + 1];
            int pointsToNext = nextRank.getMinRating() - rating.getRating();
            sb.append("\n**Rank Progress:**\n");
            sb.append("Points to ").append(nextRank.getDisplayName()).append(": ").append(Math.max(0, pointsToNext)).append("\n");
        }

        handler.sendMessage(sb.toString(), username + "'s Detailed Stats");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("1O", "1O", "1O")
                .withIdentifiers("rank", "rating", "r", "leaderboard", "lb", "top", 
                                "battle", "fight", "duel", "history", "h", "battles", 
                                "stats", "statistics", "s")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.DEFAULT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("elo", "rating", "rank");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
               "\n- '!elo' - Show your current Elo rating" +
               "\n- '!elo rank [@user]' - Show rating for yourself or mentioned user" +
               "\n- '!elo leaderboard' - Show server Elo leaderboard" +
               "\n- '!elo battle' - Start a new battle" +
               "\n- '!elo history [@user]' - Show battle history" +
               "\n- '!elo stats [@user]' - Show detailed statistics" +
               "\n*Compete in ranked battles to improve your Elo rating!*";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Elo rating and battle system commands")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("subcommand")
                        .description("Choose what to do")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("Show My Rating")
                            .value("rating")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("Leaderboard")
                            .value("leaderboard")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("Start Battle")
                            .value("battle")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("Battle History")
                            .value("history")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("Statistics")
                            .value("stats")
                            .build())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("Target user (for rank, history, stats)")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(false)
                        .build())
                .build();
    }

    /**
     * Shows a comprehensive help menu for all ELO commands.
     */
    private void showHelpMenu(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("# 🏆 ELO Rating System Help\n\n");
        
        helpMessage.append("## 📊 **View Commands**\n");
        helpMessage.append("• `!elo` - Show your current ELO rating and rank\n");
        helpMessage.append("• `!elo rank [@user]` - Show your (or another user's) ELO rating\n");
        helpMessage.append("• `!elo leaderboard` - View the server ELO leaderboard\n");
        helpMessage.append("• `!elo stats [@user]` - Detailed statistics for you (or another user)\n");
        helpMessage.append("• `!elo history [@user]` - Battle history for you (or another user)\n\n");
        
        helpMessage.append("## ⚔️ **Battle Commands**\n");
        helpMessage.append("• `!!elo battle` - Start a new ELO battle against another player\n");
        helpMessage.append("• `!!battle` - Alternative command to start battles\n\n");
        
        helpMessage.append("## 🎯 **How Battles Work**\n");
        helpMessage.append("1. **Challenge** - Use `!!elo battle` to start a battle wizard\n");
        helpMessage.append("2. **Select Opponent** - @mention the player you want to challenge\n");
        helpMessage.append("3. **Wait for Acceptance** - Your opponent must accept the challenge\n");
        helpMessage.append("4. **Battle** - Fight your battle outside Discord (game, competition, etc.)\n");
        helpMessage.append("5. **Report Results** - Use the \"Report Results\" button to declare the winner\n");
        helpMessage.append("6. **ELO Update** - Ratings are automatically updated based on the result\n\n");
        
        helpMessage.append("## 🏅 **Ranking System**\n");
        helpMessage.append("• **Bronze** (0-999) - Starting rank for new players\n");
        helpMessage.append("• **Silver** (1000-1399) - Intermediate level\n");
        helpMessage.append("• **Gold** (1400-1799) - Advanced level\n");
        helpMessage.append("• **Platinum** (1800-2199) - Expert level\n");
        helpMessage.append("• **Diamond** (2200+) - Master level\n\n");
        
        helpMessage.append("## ℹ️ **Additional Info**\n");
        helpMessage.append("• New players start with 1000 ELO rating\n");
        helpMessage.append("• Rating changes depend on the skill difference between players\n");
        helpMessage.append("• Battles expire after 30 minutes if not completed\n");
        helpMessage.append("• You can only have one active battle at a time\n");
        helpMessage.append("• **Only one result submission per battle** - First valid result is final\n\n");
        
        helpMessage.append("## 🔗 **Command Aliases**\n");
        helpMessage.append("• `!elo r` = `!elo rank`\n");
        helpMessage.append("• `!elo lb` = `!elo leaderboard`\n");
        helpMessage.append("• `!elo s` = `!elo stats`\n");
        helpMessage.append("• `!elo h` = `!elo history`\n");
        helpMessage.append("• `!elo help` = `!elo ?` - Show this help menu\n\n");
        
        helpMessage.append("*Need more help? Contact a server administrator.*");

        handler.sendMessage(helpMessage.toString());
        event.getLogger().append("- ELO help menu displayed", LogDestination.NONAPI);
    }
}
