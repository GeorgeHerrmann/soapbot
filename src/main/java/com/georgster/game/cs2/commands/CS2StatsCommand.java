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
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.core.object.command.ApplicationCommandOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command to view comprehensive CS2 statistics for a player.
 * <p>
 * Usage: !cs2 stats [@username] or /cs2 stats [@username]
 * <p>
 * This command:
 * 1. Resolves the target player via PlayerLookup (defaults to command executor)
 * 2. Checks FaceitCache for cached player statistics (if hit, returns immediately)
 * 3. Displays cached data or loading message
 * 4. Starts CompletableFuture async task to fetch fresh data
 * 5. Updates original embed with comprehensive statistics including:
 *    - Faceit level and Elo
 *    - Total matches, wins, losses, win rate
 *    - K/D ratio, ADR, headshot percentage, MVPs per match
 *    - Top 3 best maps with individual statistics
 *    - Recent form (last 5 match results)
 * <p>
 * Error Handling:
 * - Player not found → PlayerNotFoundException → Error message displayed
 * - Insufficient match history (<5 matches) → Displays available stats with helpful message
 * - API errors → FaceitAPIException → "Service temporarily unavailable" (FR-011)
 * - Missing permission → Permission denied error
 */
public class CS2StatsCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2StatsCommand.class);
    
    private final FaceitAPIClient apiClient;
    private final FaceitCache cache;
    
    /**
     * Creates a new CS2StatsCommand instance.
     * 
     * @throws FaceitAPIException if FaceitAPIClient cannot be initialized
     */
    public CS2StatsCommand() throws FaceitAPIException {
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
        
        // Determine target player reference
        String playerReference;
        if (args.isEmpty() || (args.size() == 1 && args.get(0).equalsIgnoreCase("stats"))) {
            // No player specified, use executor's linked account
            playerReference = "<@" + executorId + ">";
        } else if (args.size() >= 2 && args.get(0).equalsIgnoreCase("stats")) {
            // Command invoked via cs2 router: !cs2 stats <player>
            playerReference = args.get(1).trim();
        } else {
            // Command invoked directly: !cs2stats <player>
            playerReference = args.get(0).trim();
        }
        
        logger.info("CS2StatsCommand invoked in guild {} by user {} for player '{}'", 
                guildId, executorId, playerReference);
        
        try {
            // Resolve player via PlayerLookup
            FaceitPlayer player = PlayerLookup.resolveFaceitPlayer(playerReference, profileManager, apiClient);
            String playerId = player.getPlayerId();
            
            logger.info("Resolved player: {} (ID: {})", player.getNickname(), playerId);
            
            // Check cache for player stats
            PlayerStats cachedStats = cache.getPlayerStats(guildId, playerId);
            
            Message responseMessage;
            
            if (cachedStats != null) {
                // Cache hit - display cached data immediately
                logger.debug("Cache HIT: Displaying cached stats for player {}", playerId);
                EmbedCreateSpec cachedEmbed = CS2EmbedFormatter.formatPlayerStats(cachedStats, player);
                responseMessage = handler.sendMessage(cachedEmbed);
            } else {
                // Cache miss - display loading message
                logger.debug("Cache MISS: Displaying loading message for player {}", playerId);
                EmbedCreateSpec loadingEmbed = CS2EmbedFormatter.formatLoadingMessage("Fetching player statistics...");
                responseMessage = handler.sendMessage(loadingEmbed);
            }
            
            // Start async task to fetch fresh data
            final Message finalResponseMessage = responseMessage;
            final String finalPlayerId = playerId;
            final FaceitPlayer finalPlayer = player;
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Fetch fresh player stats
                    PlayerStats freshStats = apiClient.fetchPlayerStats(finalPlayerId);
                    
                    // Update cache
                    cache.putPlayerStats(guildId, finalPlayerId, freshStats);
                    

                    // Format updated embed
                    EmbedCreateSpec freshEmbed = CS2EmbedFormatter.formatPlayerStats(freshStats, finalPlayer);
                    
                    // Update original message with fresh data
                    if (finalResponseMessage != null) {
                        finalResponseMessage.edit()
                                .withEmbeds(freshEmbed)
                                .subscribe(
                                    updatedMsg -> logger.debug("Successfully updated embed with fresh stats data"),
                                    error -> logger.error("Failed to update embed with fresh stats data: {}", error.getMessage())
                                );
                    }
                    
                    return true;
                    
                } catch (PlayerNotFoundException e) {
                    logger.warn("Insufficient data for player {}", finalPlayerId);
                    
                    // Display "Insufficient data" message with available profile info
                    EmbedCreateSpec insufficientDataEmbed = CS2EmbedFormatter.formatInsufficientData(finalPlayer);
                    if (finalResponseMessage != null) {
                        finalResponseMessage.edit()
                                .withEmbeds(insufficientDataEmbed)
                                .subscribe(
                                    updatedMsg -> logger.debug("Displayed insufficient data message"),
                                    error -> logger.error("Failed to update with insufficient data message: {}", error.getMessage())
                                );
                    }
                    
                    return false;
                    
                } catch (FaceitAPIException e) {
                    logger.error("Faceit API error during async stats fetch: {}", e.getMessage(), e);
                    
                    // If we have cached data, keep it; otherwise display error
                    if (cachedStats == null) {
                        EmbedCreateSpec errorEmbed = CS2EmbedFormatter.formatError(
                                "⚠️ Service Temporarily Unavailable",
                                "Unable to fetch statistics at this time. Please try again later."
                        );
                        if (finalResponseMessage != null) {
                            finalResponseMessage.edit()
                                    .withEmbeds(errorEmbed)
                                    .subscribe(
                                        updatedMsg -> logger.debug("Displayed API error message"),
                                        error -> logger.error("Failed to update with error message: {}", error.getMessage())
                                    );
                        }
                    }
                    
                    return false;
                    
                } catch (Exception e) {
                    logger.error("Unexpected error during async stats fetch: {}", e.getMessage(), e);
                    return false;
                }
            });
            
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found: {}", playerReference);
            handler.sendMessage("❌ **Player Not Found**\n\n" +
                    "Could not find player matching your input.\n\n" +
                    "**Input**: " + playerReference + "\n" +
                    "**Troubleshooting**:\n" +
                    "1. Verify username is correct\n" +
                    "2. Link your account with `/cs2 link <username>`\n" +
                    "3. Try mentioning the Discord user",
                    "CS2 Stats", MessageFormatting.ERROR);
            
        } catch (FaceitAPIException e) {
            logger.error("Faceit API error during stats command: {}", e.getMessage(), e);
            handler.sendMessage("⚠️ **Service Temporarily Unavailable**\n\n" +
                    "Unable to fetch statistics at this time. Please try again later.",
                    "CS2 Stats", MessageFormatting.ERROR);
            
        } catch (Exception e) {
            logger.error("Unexpected error during CS2 stats command: {}", e.getMessage(), e);
            handler.sendMessage("An unexpected error occurred. Please try again later.",
                    "CS2 Stats", MessageFormatting.ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("1+")
                .withIdentifiers("cs2stats", "cs2 stats")
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2stats", "cs2 stats");
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
                .name("cs2stats")
                .description("View comprehensive CS2 statistics")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("player")
                        .description("Discord user or Faceit username (optional)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
                "\n- '!cs2 stats' to view your statistics" +
                "\n- '!cs2 stats @user' to view another player's statistics" +
                "\n- '!cs2 stats faceit:username' to view by Faceit username" +
                "\n- '/cs2stats' as a slash command" +
                "\n\n*View comprehensive CS2 statistics including Faceit level, total matches, win rate, K/D, ADR, headshot %, best maps, and recent form (last 5 matches).*";
    }
}
