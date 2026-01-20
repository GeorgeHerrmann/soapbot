package com.georgster.game.cs2.commands;

import com.georgster.ParseableCommand;
import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.exception.PlayerNotFoundException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.api.faceit.model.PlayerStats;
import com.georgster.cache.FaceitCache;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.cs2.util.CS2EmbedFormatter;
import com.georgster.game.cs2.util.PlayerLookup;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.core.object.command.ApplicationCommandOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Command to compare CS2 statistics between two players.
 * <p>
 * Usage: 
 * - !cs2 compare <player> → compares executor to specified player
 * - !cs2 compare <player1> <player2> → compares player1 to player2
 * - /cs2 compare <player1> <player2> (slash command)
 * <p>
 * This command:
 * 1. Resolves both target players via PlayerLookup
 * 2. Checks FaceitCache for cached player statistics for both (partial cache hit acceptable)
 * 3. Fetches fresh stats for both players via FaceitAPIClient.fetchPlayerStats()
 * 4. Formats side-by-side comparison embed with visual indicators (↑ ↓ = emojis)
 * 5. Displays comparison metrics:
 *    - Elo rating
 *    - K/D ratio
 *    - ADR (Average Damage per Round)
 *    - Headshot percentage
 *    - Win rate
 * 6. Includes brief summary statement indicating overall performance difference
 * <p>
 * Error Handling:
 * - One or both players not found → PlayerNotFoundException → Error message displayed with details
 * - API errors → FaceitAPIException → "Service temporarily unavailable" (FR-011)
 * - Missing permission → Permission denied error
 */
public class CS2CompareCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2CompareCommand.class);
    
    private final FaceitAPIClient apiClient;
    private final FaceitCache cache;
    
    /**
     * Creates a new CS2CompareCommand instance.
     * 
     * @throws FaceitAPIException if FaceitAPIClient cannot be initialized
     */
    public CS2CompareCommand() throws FaceitAPIException {
        this.apiClient = new FaceitAPIClient();
        this.cache = new FaceitCache();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        UserProfileManager profileManager = event.getClientContext().getUserProfileManager();
        
        String guildId = event.getDiscordEvent().getGuild().getId().asString();
        String executorId = event.getDiscordEvent().getUser().getId().asString();
        
        List<String> args = event.getParsedArguments().getArguments();
        
        // Determine player references (following CS2StatsCommand pattern)
        String player1Reference;
        String player2Reference;
        
        if (args.isEmpty() || (args.size() == 1 && args.get(0).equalsIgnoreCase("compare"))) {
            // No players specified - show usage
            handler.sendMessage("Usage: `!cs2 compare <player1>` or `!cs2 compare <player1> <player2>`\n\n" +
                    "Compare your stats to another player, or compare two players.\n" +
                    "• One player: compares you to that player\n" +
                    "• Two players: compares player1 to player2", 
                    MessageFormatting.ERROR);
            return;
        } else if (args.size() >= 2 && args.get(0).equalsIgnoreCase("compare")) {
            // Command invoked via cs2 router: !cs2 compare <player1> [<player2>]
            // args.get(1) contains remaining text with player references separated by spaces
            String remainingText = args.get(1).trim();
            String[] playerTokens = remainingText.split("\\s+");
            
            if (playerTokens.length == 1) {
                // Single player: compare executor to that player
                player1Reference = "<@" + executorId + ">";
                player2Reference = event.getParsedArguments().getOriginalMessage().split(" ")[2].trim();
            } else {
                // Two or more players: compare player1 to player2
                String[] playerNamesMessage = event.getParsedArguments().getOriginalMessage().split(" ");
                player1Reference = playerNamesMessage[2];
                player2Reference = playerNamesMessage[3];
            }
        } else if (args.size() == 1) {
            // Command invoked directly: !cs2compare <player>
            player1Reference = "<@" + executorId + ">";
            player2Reference = args.get(0).trim();
        } else {
            // Command invoked directly: !cs2compare <player1> <player2>
            player1Reference = args.get(0).trim();
            player2Reference = args.get(1).trim();
        }
        
        logger.info("CS2CompareCommand invoked in guild {} by user {} for players '{}' vs '{}'", 
                guildId, executorId, player1Reference, player2Reference);
        
        // Send loading message
        handler.sendMessage("🔄 Comparing players...", MessageFormatting.INFO);
        
        try {
            // Resolve both players via PlayerLookup
            FaceitPlayer player1;
            FaceitPlayer player2;
            
            try {
                player1 = PlayerLookup.resolveFaceitPlayer(player1Reference, profileManager, apiClient);
            } catch (PlayerNotFoundException e) {
                logger.warn("Player 1 not found: {}", player1Reference);
                handler.sendMessage(
                    CS2EmbedFormatter.formatError(
                        "Player Not Found", 
                        String.format("Could not find player: **%s**", player1Reference)
                    )
                );
                return;
            }
            
            try {
                player2 = PlayerLookup.resolveFaceitPlayer(player2Reference, profileManager, apiClient);
            } catch (PlayerNotFoundException e) {
                logger.warn("Player 2 not found: {}", player2Reference);
                handler.sendMessage(
                    CS2EmbedFormatter.formatError(
                        "Player Not Found", 
                        String.format("Could not find player: **%s**", player2Reference)
                    )
                );
                return;
            }
            
            logger.info("Both players resolved: {} (ID: {}) vs {} (ID: {})", 
                    player1.getNickname(), player1.getPlayerId(),
                    player2.getNickname(), player2.getPlayerId());
            
            // Check cache for both players' stats
            PlayerStats stats1 = cache.getPlayerStats(guildId, player1.getPlayerId());
            PlayerStats stats2 = cache.getPlayerStats(guildId, player2.getPlayerId());
            
            // Fetch fresh stats for both players (if not cached)
            logger.info("Fetching stats for both players (cache check)");
            
            if (stats1 == null) {
                stats1 = apiClient.fetchPlayerStats(player1.getPlayerId());
                cache.putPlayerStats(guildId, player1.getPlayerId(), stats1);
            }
            
            if (stats2 == null) {
                stats2 = apiClient.fetchPlayerStats(player2.getPlayerId());
                cache.putPlayerStats(guildId, player2.getPlayerId(), stats2);
            }
            
            // Format and display comparison
            EmbedCreateSpec embed = CS2EmbedFormatter.formatComparison(stats1, stats2, player1, player2);
            handler.sendMessage(embed);
            
        } catch (PlayerNotFoundException e) {
            logger.error("Player not found during comparison: {}", e.getMessage());
            handler.sendMessage(
                CS2EmbedFormatter.formatError(
                    "Player Not Found",
                    e.getMessage()
                )
            );
        } catch (FaceitAPIException e) {
            logger.error("Faceit API error during comparison: {}", e.getMessage(), e);
            handler.sendMessage(
                CS2EmbedFormatter.formatError(
                    "Service Temporarily Unavailable",
                    "The Faceit API is currently unavailable. Please try again later."
                )
            );
        } catch (Exception e) {
            logger.error("Unexpected error during player comparison", e);
            handler.sendMessage(
                CS2EmbedFormatter.formatError(
                    "Error",
                    "An unexpected error occurred while comparing players."
                )
            );
        }
    }
    

    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("1-2")
                .withIdentifiers("cs2compare", "cs2 compare")
                .withoutAutoFormatting()
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2compare", "cs2 compare");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.CS2COMMAND;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name("cs2compare")
                .description("Compare two players' CS2 statistics side-by-side")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("player1")
                        .description("First player (mention or Faceit username)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("player2")
                        .description("Second player (mention or Faceit username)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
                "\n- '!cs2 compare <player>' to compare yourself to another player" +
                "\n- '!cs2 compare <player1> <player2>' to compare two players" +
                "\n- '!cs2 compare @user' to compare yourself to a Discord user" +
                "\n- '!cs2 compare faceit:username' to compare yourself to a Faceit username" +
                "\n- '/cs2compare' as a slash command" +
                "\n\n*Compare CS2 statistics side-by-side including Elo, K/D, ADR, headshot %, win rate, and overall performance assessment.*";
    }
}
