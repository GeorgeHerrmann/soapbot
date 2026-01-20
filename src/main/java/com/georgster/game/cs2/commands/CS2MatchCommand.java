package com.georgster.game.cs2.commands;

import com.georgster.ParseableCommand;
import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.exception.PlayerNotFoundException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.api.faceit.model.MatchDetails;
import com.georgster.api.faceit.model.PlayerStats;
import com.georgster.cache.FaceitCache;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.cs2.util.CS2EmbedFormatter;
import com.georgster.game.cs2.util.PlayerLookup;
import com.georgster.game.cs2.wizard.CS2MatchWizard;
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
 * Command to view detailed statistics from the most recent Faceit match.
 * <p>
 * Usage: !cs2 match [@username] or /cs2 match [@username]
 * <p>
 * This command:
 * 1. Resolves the target player via PlayerLookup (defaults to command executor)
 * 2. Checks FaceitCache for cached match details (if hit, returns immediately)
 * 3. Displays cached data or loading message
 * 4. Starts CompletableFuture async task to fetch fresh data
 * 5. Updates original embed with fresh match statistics and comparison vs. lifetime average
 * <p>
 * Error Handling:
 * - Player not found → PlayerNotFoundException → Error message displayed
 * - No recent matches → Displays profile info with "No recent matches" message
 * - API errors → FaceitAPIException → "Service temporarily unavailable" (FR-011)
 * - Missing permission → Permission denied error
 */
public class CS2MatchCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2MatchCommand.class);
    
    private final FaceitAPIClient apiClient;
    private final FaceitCache cache;
    
    /**
     * Creates a new CS2MatchCommand instance.
     * 
     * @throws FaceitAPIException if FaceitAPIClient cannot be initialized
     */
    public CS2MatchCommand() throws FaceitAPIException {
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
        if (args.isEmpty() || (args.size() == 1 && args.get(0).equalsIgnoreCase("match"))) {
            // No player specified, use executor's linked account
            playerReference = "<@" + executorId + ">";
        } else if (args.size() >= 2 && args.get(0).equalsIgnoreCase("match")) {
            // Command invoked via cs2 router: !cs2 match <player>
            playerReference = args.get(1).trim();
        } else {
            // Command invoked directly: !cs2match <player>
            playerReference = args.get(0).trim();
        }
        
        logger.info("CS2MatchCommand invoked in guild {} by user {} for player '{}'", 
                guildId, executorId, playerReference);
        
        try {
            // Resolve player via PlayerLookup
            FaceitPlayer player = PlayerLookup.resolveFaceitPlayer(playerReference, profileManager, apiClient);
            String playerId = player.getPlayerId();
            
            logger.info("Resolved player: {} (ID: {})", player.getNickname(), playerId);
            
            // Check cache for match details
            MatchDetails cachedMatch = cache.getMatchReport(guildId, playerId);
            PlayerStats cachedStats = cache.getPlayerStats(guildId, playerId);
            
            Message responseMessage;
            
            if (cachedMatch != null && cachedStats != null) {
                // Cache hit - display cached data immediately
                logger.debug("Cache HIT: Displaying cached match for player {}", playerId);
                EmbedCreateSpec cachedEmbed = CS2EmbedFormatter.formatMatchReport(cachedMatch, cachedStats, player);
                responseMessage = handler.sendMessage(cachedEmbed);
            } else {
                // Cache miss - display loading message
                logger.debug("Cache MISS: Displaying loading message for player {}", playerId);
                EmbedCreateSpec loadingEmbed = CS2EmbedFormatter.formatLoadingMessage("Fetching match data...");
                responseMessage = handler.sendMessage(loadingEmbed);
            }
            
            // Start async task to fetch fresh data
            final Message finalResponseMessage = responseMessage;
            final String finalPlayerId = playerId;
            final FaceitPlayer finalPlayer = player;
            final CommandExecutionEvent finalEvent = event;
            final MatchDetails[] freshMatchHolder = new MatchDetails[1]; // Holder to pass match to wizard
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Fetch fresh match details and player stats
                    MatchDetails freshMatch = apiClient.fetchLastMatch(finalPlayerId);
                    freshMatchHolder[0] = freshMatch; // Store for wizard
                    
                    PlayerStats freshStats = apiClient.fetchPlayerStats(finalPlayerId);
                    
                    // Update cache
                    cache.putMatchReport(guildId, finalPlayerId, freshMatch);
                    cache.putPlayerStats(guildId, finalPlayerId, freshStats);
                    
                    // Format updated embed
                    EmbedCreateSpec freshEmbed = CS2EmbedFormatter.formatMatchReport(freshMatch, freshStats, finalPlayer);
                    
                    // Update original message with fresh data
                    if (finalResponseMessage != null) {
                        finalResponseMessage.edit()
                                .withEmbeds(freshEmbed)
                                .subscribe(
                                    updatedMsg -> {
                                        logger.debug("Successfully updated embed with fresh match data");
                                        
                                        // After successful update, launch wizard for full match view
                                        if (freshMatchHolder[0] != null) {
                                            try {
                                                CS2MatchWizard wizard = new CS2MatchWizard(
                                                    finalEvent, 
                                                    freshMatchHolder[0], 
                                                    apiClient, 
                                                    cache, 
                                                    guildId
                                                );
                                                wizard.begin();
                                            } catch (Exception wizardError) {
                                                logger.error("Failed to launch CS2MatchWizard: {}", wizardError.getMessage());
                                            }
                                        }
                                    },
                                    error -> logger.error("Failed to update embed with fresh match data: {}", error.getMessage())
                                );
                    }
                    
                    return true;
                    
                } catch (PlayerNotFoundException e) {
                    logger.warn("No recent matches found for player {}", playerId);
                    
                    // Display "No recent matches" message with profile info
                    EmbedCreateSpec noMatchEmbed = CS2EmbedFormatter.formatNoRecentMatches(player);
                    if (finalResponseMessage != null) {
                        finalResponseMessage.edit()
                                .withEmbeds(noMatchEmbed)
                                .subscribe(
                                    updatedMsg -> logger.debug("Displayed no recent matches message"),
                                    error -> logger.error("Failed to update with no matches message: {}", error.getMessage())
                                );
                    }
                    
                    return false;
                    
                } catch (FaceitAPIException e) {
                    logger.error("Faceit API error during async match fetch: {}", e.getMessage(), e);
                    
                    // If we have cached data, keep it; otherwise display error
                    if (cachedMatch == null) {
                        EmbedCreateSpec errorEmbed = CS2EmbedFormatter.formatError(
                                "⚠️ Service Temporarily Unavailable",
                                "Unable to fetch match data at this time. Please try again later."
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
                    logger.error("Unexpected error during async match fetch: {}", e.getMessage(), e);
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
                    "CS2 Match", MessageFormatting.ERROR);
            
        } catch (FaceitAPIException e) {
            logger.error("Faceit API error during match command: {}", e.getMessage(), e);
            handler.sendMessage("⚠️ **Service Temporarily Unavailable**\n\n" +
                    "Unable to fetch match data at this time. Please try again later.",
                    "CS2 Match", MessageFormatting.ERROR);
            
        } catch (Exception e) {
            logger.error("Unexpected error during CS2 match command: {}", e.getMessage(), e);
            handler.sendMessage("An unexpected error occurred. Please try again later.",
                    "CS2 Match", MessageFormatting.ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("1+")
                .withIdentifiers("cs2match", "cs2 match")
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2match", "cs2 match");
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
                .name("cs2match")
                .description("View your most recent CS2 match statistics")
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
                "\n- '!cs2 match' to view your last match" +
                "\n- '!cs2 match @user' to view another player's last match" +
                "\n- '!cs2 match faceit:username' to view by Faceit username" +
                "\n- '/cs2match' as a slash command" +
                "\n\n*View detailed statistics from your most recent Faceit match, including K/D, ADR, headshot %, MVPs, and comparison to your lifetime average.*";
    }
}
