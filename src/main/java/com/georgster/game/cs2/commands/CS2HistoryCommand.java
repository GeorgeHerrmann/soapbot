package com.georgster.game.cs2.commands;

import com.georgster.ParseableCommand;
import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.exception.PlayerNotFoundException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.api.faceit.model.MatchDetails;
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
 * Command to view last 10 matches with quick stats (map, score, K/D, ADR, result, time played).
 * <p>
 * Usage: !cs2 history [@username] or /cs2 history [@username]
 * <p>
 * This command:
 * 1. Resolves the target player via PlayerLookup (defaults to command executor)
 * 2. Checks FaceitCache for cached match history (if hit, returns immediately)
 * 3. Displays cached data or loading message
 * 4. Starts CompletableFuture async task to fetch fresh data
 * 5. Updates original embed with fresh match history and recent performance summary
 * <p>
 * Error Handling:
 * - Player not found → PlayerNotFoundException → Error message displayed
 * - No match history → Displays profile info with "No match history" message
 * - API errors → FaceitAPIException → "Service temporarily unavailable" (FR-011)
 * - Missing permission → Permission denied error
 */
public class CS2HistoryCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2HistoryCommand.class);
    
    private final FaceitAPIClient apiClient;
    private final FaceitCache cache;
    
    /**
     * Creates a new CS2HistoryCommand instance.
     * 
     * @throws FaceitAPIException if FaceitAPIClient cannot be initialized
     */
    public CS2HistoryCommand() throws FaceitAPIException {
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
        if (args.isEmpty() || (args.size() == 1 && args.get(0).equalsIgnoreCase("history"))) {
            // No player specified, use executor's linked account
            playerReference = "<@" + executorId + ">";
        } else if (args.size() >= 2 && args.get(0).equalsIgnoreCase("history")) {
            // Command invoked via cs2 router: !cs2 history <player>
            playerReference = args.get(1).trim();
        } else {
            // Command invoked directly: !cs2history <player>
            playerReference = args.get(0).trim();
        }
        
        logger.info("CS2HistoryCommand invoked in guild {} by user {} for player '{}'", 
                guildId, executorId, playerReference);
        
        try {
            // Resolve player via PlayerLookup
            FaceitPlayer player = PlayerLookup.resolveFaceitPlayer(playerReference, profileManager, apiClient);
            String playerId = player.getPlayerId();
            
            logger.info("Resolved player: {} (ID: {})", player.getNickname(), playerId);
            
            // Check cache for match history
            List<MatchDetails> cachedHistory = cache.getMatchHistory(guildId, playerId);
            
            Message responseMessage;
            
            if (cachedHistory != null && !cachedHistory.isEmpty()) {
                // Cache hit - display cached data immediately
                logger.debug("Cache HIT: Displaying cached match history for player {}", playerId);
                EmbedCreateSpec cachedEmbed = CS2EmbedFormatter.formatMatchHistory(cachedHistory, player);
                responseMessage = handler.sendMessage(cachedEmbed);
            } else {
                // Cache miss - display loading message
                logger.debug("Cache MISS: Displaying loading message for player {}", playerId);
                EmbedCreateSpec loadingEmbed = CS2EmbedFormatter.formatLoadingMessage("Fetching match history...");
                responseMessage = handler.sendMessage(loadingEmbed);
            }
            
            // Start async task to fetch fresh data
            final Message finalResponseMessage = responseMessage;
            final String finalPlayerId = playerId;
            final FaceitPlayer finalPlayer = player;
            CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("Starting async fetch for match history of player {} in guild {}", finalPlayerId, guildId);
                    
                    // Fetch fresh match history (last 10)
                    List<MatchDetails> freshHistory = apiClient.fetchMatchHistory(finalPlayerId, 10);
                    
                    if (freshHistory == null || freshHistory.isEmpty()) {
                        // No match history found
                        logger.warn("No match history found for player {}", finalPlayerId);
                        EmbedCreateSpec noHistoryEmbed = CS2EmbedFormatter.formatNoMatchHistory(finalPlayer);
                        
                        if (finalResponseMessage != null) {
                            finalResponseMessage.edit()
                                    .withEmbeds(noHistoryEmbed)
                                    .subscribe(
                                        updatedMsg -> logger.debug("Displayed no match history message"),
                                        error -> logger.error("Failed to update with no history message: {}", error.getMessage())
                                    );
                        }
                        
                        return false;
                    }
                    
                    // Update cache
                    cache.putMatchHistory(guildId, finalPlayerId, freshHistory);
                    
                    logger.info("Successfully fetched {} matches for player {}", freshHistory.size(), finalPlayerId);
                    
                    // Format updated embed
                    EmbedCreateSpec freshEmbed = CS2EmbedFormatter.formatMatchHistory(freshHistory, finalPlayer);
                    
                    // Update original message with fresh data
                    if (finalResponseMessage != null) {
                        finalResponseMessage.edit()
                                .withEmbeds(freshEmbed)
                                .subscribe(
                                    updatedMsg -> logger.debug("Successfully updated embed with fresh match history"),
                                    error -> logger.error("Failed to update embed with fresh match history: {}", error.getMessage())
                                );
                    }
                    
                    return true;
                    
                } catch (FaceitAPIException e) {
                    logger.error("Faceit API error during async history fetch: {}", e.getMessage(), e);
                    
                    // If we have cached data, keep it; otherwise display error
                    if (cachedHistory == null || cachedHistory.isEmpty()) {
                        EmbedCreateSpec errorEmbed = CS2EmbedFormatter.formatError(
                                "⚠️ Service Temporarily Unavailable",
                                "Unable to fetch match history at this time. Please try again later."
                        );
                        
                        if (finalResponseMessage != null) {
                            finalResponseMessage.edit()
                                    .withEmbeds(errorEmbed)
                                    .subscribe(
                                        updatedMsg -> logger.debug("Displayed error message due to API failure"),
                                        error -> logger.error("Failed to update with error message: {}", error.getMessage())
                                    );
                        }
                    } else {
                        logger.debug("Keeping cached match history due to API error");
                    }
                    
                    return false;
                    
                } catch (Exception e) {
                    logger.error("Unexpected error during async history fetch: {}", e.getMessage(), e);
                    return false;
                }
            });
            
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found: {}", e.getMessage());
            handler.sendMessage(
                    CS2EmbedFormatter.formatError(
                            "❌ Player Not Found",
                            "Could not find player matching your input. Please check the username or mention."
                    )
            );
            
        } catch (FaceitAPIException e) {
            logger.error("Faceit API error: {}", e.getMessage(), e);
            handler.sendMessage(
                    CS2EmbedFormatter.formatError(
                            "⚠️ Service Temporarily Unavailable",
                            "Unable to connect to Faceit API. Please try again later."
                    )
            );
            
        } catch (Exception e) {
            logger.error("Unexpected error in CS2HistoryCommand: {}", e.getMessage(), e);
            handler.sendMessage(
                    "An unexpected error occurred. Please contact server admins.",
                    "CS2 History",
                    MessageFormatting.ERROR
            );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2history");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("VO").build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name("cs2history")
                .description("View last 10 matches and recent form")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("player")
                        .description("Discord user or Faceit username (optional)")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(false)
                        .build())
                .build();
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
    public String help() {
        return "Aliases: " + getAliases().toString() +
                "\n- '!cs2 history [@user]' to view last 10 matches" +
                "\n- '!cs2 history faceit:username' to view another player's history" +
                "\n- '/cs2history' as a slash command" +
                "\n\n*View match history with quick stats (map, score, K/D, ADR, result, timestamp). Shows win rate and performance trends.*";
    }
}
