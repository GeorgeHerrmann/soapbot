package com.georgster.elo;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.elo.manager.EloManager;
import com.georgster.permissions.PermissibleAction;
import com.georgster.profile.UserProfile;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;

/**
 * A command for interacting with the Elo rating system.
 * Provides subcommands for viewing ratings, leaderboards, and battle history.
 */
public class EloCommand implements ParseableCommand {

    private final UserProfileManager profileManager;
    private final EloManager eloManager;
    
    /**
     * Creates a new EloCommand from the provided context.
     * 
     * @param context The context for this command's SOAPClient
     */
    public EloCommand(ClientContext context) {
        this.profileManager = context.getUserProfileManager();
        this.eloManager = new EloManager(context);
        this.eloManager.load(); // Load existing ratings
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        SubcommandSystem subcommands = event.createSubcommandSystem();
        
        subcommands.on(p -> {
            // Show user's own rating
            showUserRating(event, event.getDiscordEvent().getAuthorAsMember().getId().asString());
        });
        
        subcommands.on(p -> {
            // Show specific user's rating
            String targetUser = p.get(1);
            Member target = handler.getMemberByName(targetUser);
            if (target == null) target = handler.getMemberByTag(targetUser);
            if (target == null) target = handler.getMemberById(targetUser);
            if (target != null) {
                showUserRating(event, target.getId().asString());
            } else {
                handler.sendMessage("User not found: " + targetUser);
            }
        }, "rank");
        
        subcommands.on(p -> {
            showLeaderboard(event);
        }, "leaderboard", "lb");
        
        subcommands.on(p -> {
            showGuildStats(event);
        }, "stats");
        
        subcommands.on(p -> {
            showUserHistory(event, event.getDiscordEvent().getAuthorAsMember().getId().asString());
        }, "history");
        
        subcommands.on(p -> {
            if (p.size() == 2) {
                // Compare self with target
                String targetUser = p.get(1);
                Member target = handler.getMemberByName(targetUser);
                if (target == null) target = handler.getMemberByTag(targetUser);
                if (target == null) target = handler.getMemberById(targetUser);
                if (target != null) {
                    compareRatings(event, event.getDiscordEvent().getAuthorAsMember().getId().asString(), target.getId().asString());
                } else {
                    handler.sendMessage("User not found: " + targetUser);
                }
            } else if (p.size() == 3) {
                // Compare two specific users
                Member user1 = handler.getMemberByName(p.get(1));
                if (user1 == null) user1 = handler.getMemberByTag(p.get(1));
                if (user1 == null) user1 = handler.getMemberById(p.get(1));
                
                Member user2 = handler.getMemberByName(p.get(2));
                if (user2 == null) user2 = handler.getMemberByTag(p.get(2));
                if (user2 == null) user2 = handler.getMemberById(p.get(2));
                
                if (user1 != null && user2 != null) {
                    compareRatings(event, user1.getId().asString(), user2.getId().asString());
                } else {
                    handler.sendMessage("One or both users not found.");
                }
            }
        }, "compare");
    }

    private void showUserRating(CommandExecutionEvent event, String memberId) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        UserProfile profile = profileManager.get(memberId);
        
        if (profile == null) {
            handler.sendMessage("User profile not found.");
            return;
        }
        
        EloRating rating = profile.getEloRating();
        Member member = handler.getMemberById(memberId);
        String username = member != null ? member.getDisplayName() : profile.getUsername();
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(rating.getRank().getColor())
                .title("🏆 " + username + "'s Elo Rating")
                .addField("Current Rating", String.valueOf(rating.getRating()), true)
                .addField("Rank", rating.getRank().getDisplayName(), true)
                .addField("Leaderboard Position", "#" + eloManager.getLeaderboardPosition(memberId), true)
                .addField("Matches Played", String.valueOf(rating.getMatchesPlayed()), true)
                .addField("Win/Loss", rating.getWins() + "/" + rating.getLosses(), true)
                .addField("Win Rate", String.format("%.1f%%", rating.getWinRate()), true)
                .addField("Current Win Streak", String.valueOf(rating.getWinStreak()), true)
                .addField("Best Rating", String.valueOf(rating.getBestRating()), true)
                .build();
        
        if (rating.getRank().getNextRank() != null) {
            int toNext = rating.getRank().getRatingToNextRank(rating.getRating());
            embed = EmbedCreateSpec.builder()
                    .from(embed)
                    .addField("To Next Rank", toNext + " points to " + rating.getRank().getNextRank().getDisplayName(), false)
                    .build();
        }
        
        handler.sendMessage(embed);
    }

    private void showLeaderboard(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        List<EloRating> leaderboard = eloManager.getTopPlayers(10);
        
        if (leaderboard.isEmpty()) {
            handler.sendMessage("No Elo ratings found. Play some matches to start the leaderboard!");
            return;
        }
        
        StringBuilder leaderboardText = new StringBuilder();
        for (int i = 0; i < leaderboard.size(); i++) {
            EloRating rating = leaderboard.get(i);
            Member member = handler.getMemberById(rating.getMemberId());
            String username = member != null ? member.getDisplayName() : "Unknown User";
            
            String medal = "";
            switch (i) {
                case 0: medal = "🥇"; break;
                case 1: medal = "🥈"; break;
                case 2: medal = "🥉"; break;
                default: medal = "#" + (i + 1); break;
            }
            
            leaderboardText.append(String.format("%s **%s** - %d (%s) [%d/%d]\n", 
                    medal, username, rating.getRating(), rating.getRank().getDisplayName(),
                    rating.getWins(), rating.getLosses()));
        }
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(255, 215, 0))
                .title("🏆 Elo Leaderboard")
                .description(leaderboardText.toString())
                .footer("Showing top 10 players", null)
                .build();
        
        handler.sendMessage(embed);
    }

    private void showGuildStats(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        EloManager.EloStats stats = eloManager.getGuildStats();
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("📊 Guild Elo Statistics")
                .addField("Total Players", String.valueOf(stats.getTotalPlayers()), true)
                .addField("Total Matches", String.valueOf(stats.getTotalMatches()), true)
                .addField("Average Rating", String.format("%.0f", stats.getAverageRating()), true)
                .addField("Highest Rating", String.valueOf(stats.getHighestRating()), true)
                .addField("Lowest Rating", String.valueOf(stats.getLowestRating()), true)
                .build();
        
        if (stats.getTopPlayer() != null) {
            Member topMember = handler.getMemberById(stats.getTopPlayer().getMemberId());
            String topPlayerName = topMember != null ? topMember.getDisplayName() : "Unknown User";
            embed = EmbedCreateSpec.builder()
                    .from(embed)
                    .addField("Current Champion", topPlayerName + " (" + stats.getTopPlayer().getRating() + ")", false)
                    .build();
        }
        
        handler.sendMessage(embed);
    }

    private void showUserHistory(CommandExecutionEvent event, String memberId) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        UserProfile profile = profileManager.get(memberId);
        
        if (profile == null) {
            handler.sendMessage("User profile not found.");
            return;
        }
        
        EloRating rating = profile.getEloRating();
        Member member = handler.getMemberById(memberId);
        String username = member != null ? member.getDisplayName() : profile.getUsername();
        
        // For now, show basic history info. In Phase 2, we'll add detailed match history
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(rating.getRank().getColor())
                .title("📈 " + username + "'s Match History")
                .addField("Total Matches", String.valueOf(rating.getMatchesPlayed()), true)
                .addField("Wins", String.valueOf(rating.getWins()), true)
                .addField("Losses", String.valueOf(rating.getLosses()), true)
                .addField("Current Streak", rating.getWinStreak() + " wins", true)
                .addField("Peak Rating", String.valueOf(rating.getBestRating()), true)
                .addField("Rating Change", "+" + (rating.getRating() - 1500) + " from start", true)
                .description("Detailed match history coming in Phase 2!")
                .build();
        
        handler.sendMessage(embed);
    }

    private void compareRatings(CommandExecutionEvent event, String memberId1, String memberId2) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        UserProfile profile1 = profileManager.get(memberId1);
        UserProfile profile2 = profileManager.get(memberId2);
        
        if (profile1 == null || profile2 == null) {
            handler.sendMessage("One or both user profiles not found.");
            return;
        }
        
        EloRating rating1 = profile1.getEloRating();
        EloRating rating2 = profile2.getEloRating();
        Member member1 = handler.getMemberById(memberId1);
        Member member2 = handler.getMemberById(memberId2);
        String username1 = member1 != null ? member1.getDisplayName() : profile1.getUsername();
        String username2 = member2 != null ? member2.getDisplayName() : profile2.getUsername();
        
        double winProb1 = EloCalculator.calculateWinProbability(rating1.getRating(), rating2.getRating());
        double winProb2 = 100.0 - winProb1;
        
        int estimatedGain1 = EloCalculator.estimateRatingChange(rating1, rating2, true);
        int estimatedLoss1 = EloCalculator.estimateRatingChange(rating1, rating2, false);
        
        String comparison = rating1.getRating() > rating2.getRating() ? 
            username1 + " is favored" : 
            rating2.getRating() > rating1.getRating() ? 
                username2 + " is favored" : 
                "Even match";
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(128, 0, 128))
                .title("⚔️ " + username1 + " vs " + username2)
                .description(comparison)
                .addField(username1, 
                    String.format("Rating: %d (%s)\nWin Chance: %.1f%%\nIf wins: +%d\nIf loses: %d", 
                        rating1.getRating(), rating1.getRank().getDisplayName(), 
                        winProb1, estimatedGain1, estimatedLoss1), true)
                .addField(username2, 
                    String.format("Rating: %d (%s)\nWin Chance: %.1f%%", 
                        rating2.getRating(), rating2.getRank().getDisplayName(), winProb2), true)
                .addField("Match Quality", 
                    EloCalculator.isBalancedMatch(rating1.getRating(), rating2.getRating()) ? 
                        "⚖️ Balanced" : "⚠️ Unbalanced", false)
                .build();
        
        handler.sendMessage(embed);
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new ParseBuilder("1O1O1O").withIdentifiers("rank", "leaderboard", "lb", "stats", "history", "compare").build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.ELOCOMMAND;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("elo", "rating", "rank");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- `!elo rank [user]` - View Elo rating (yours or specified user's)" +
        "\n- `!elo leaderboard` - View server Elo leaderboard" +
        "\n- `!elo stats` - View guild Elo statistics" +
        "\n- `!elo history [user]` - View match history" +
        "\n- `!elo compare <user1> [user2]` - Compare ratings and match preview";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("View Elo ratings and leaderboards")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("action")
                        .description("What to do")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("rank")
                            .value("rank")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("leaderboard")
                            .value("leaderboard")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("stats")
                            .value("stats")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("history")
                            .value("history")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("compare")
                            .value("compare")
                            .build())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("Target user (optional)")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(false)
                        .build())
                .build();
    }
}
