package com.georgster.game.cs2.commands;

import com.georgster.ParseableCommand;
import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.model.ServerLeaderboard;
import com.georgster.cache.FaceitCache;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.cs2.util.CS2EmbedFormatter;
import com.georgster.game.cs2.util.ServerLeaderboardManager;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Command to view the server's CS2 leaderboard ranked by Faceit Elo.
 * <p>
 * Usage: !cs2 leaderboard or /cs2 leaderboard
 * <p>
 * This command:
 * 1. Checks FaceitCache for cached leaderboard (if hit, returns immediately)
 * 2. If cache miss, generates fresh leaderboard via ServerLeaderboardManager
 * 3. Displays top 10 linked players ranked by Elo with:
 *    - Rank position with medal emoji (🥇🥈🥉)
 *    - Faceit nickname and level
 *    - Elo rating
 *    - K/D ratio
 *    - Discord mention for player
 * 4. Shows server statistics:
 *    - Total linked players
 *    - Server average Elo
 * 5. Highlights requesting user's rank if in top 10
 * <p>
 * Cache Behavior:
 * - Leaderboard cached with 10-minute TTL
 * - Background refresh task updates leaderboard every 10 minutes
 * - Manual refresh triggered on cache miss
 * <p>
 * Error Handling:
 * - No linked players → "No linked players yet" message
 * - API errors → FaceitAPIException → "Service temporarily unavailable" (FR-011)
 * - Missing permission → Permission denied error
 */
public class CS2LeaderboardCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2LeaderboardCommand.class);
    
    private final FaceitAPIClient apiClient;
    private final FaceitCache cache;
    
    /**
     * Creates a new CS2LeaderboardCommand instance.
     * 
     * @throws FaceitAPIException if FaceitAPIClient cannot be initialized
     */
    public CS2LeaderboardCommand() throws FaceitAPIException {
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
        
        logger.info("CS2LeaderboardCommand invoked in guild {} by user {}", guildId, executorId);
        
        try {
            // Check cache for leaderboard
            ServerLeaderboard cachedLeaderboard = cache.getLeaderboard(guildId);
            
            ServerLeaderboard leaderboard;
            
            if (cachedLeaderboard != null && !cachedLeaderboard.isEmpty()) {
                // Cache hit - display cached leaderboard immediately
                logger.debug("Cache HIT: Displaying cached leaderboard for guild {}", guildId);
                leaderboard = cachedLeaderboard;
            } else {
                // Cache miss - generate fresh leaderboard
                logger.debug("Cache MISS: Generating fresh leaderboard for guild {}", guildId);
                
                // Display "Generating leaderboard..." message
                handler.sendMessage("⏳ Generating leaderboard...", MessageFormatting.INFO);
                
                // Generate leaderboard (this also stores it in cache)
                leaderboard = ServerLeaderboardManager.generateLeaderboard(
                    guildId, 
                    profileManager, 
                    cache, 
                    apiClient
                );
            }
            
            // Check if leaderboard is empty (no linked players)
            if (leaderboard.isEmpty()) {
                logger.info("No linked players found for guild {}", guildId);
                handler.sendMessage("📊 **No Linked Players Yet**\n\n" +
                        "No members have linked their Faceit accounts. Use `/cs2 link <username>` to get started!", 
                        MessageFormatting.INFO);
                return;
            }
            
            // Format and display leaderboard embed
            EmbedCreateSpec leaderboardEmbed = CS2EmbedFormatter.formatLeaderboard(leaderboard);
            handler.sendMessage(leaderboardEmbed);
            
            logger.info("Successfully displayed leaderboard for guild {} with {} entries", 
                guildId, leaderboard.size());
            
        } catch (Exception e) {
            logger.error("Error during leaderboard command execution: {}", e.getMessage(), e);
            
            EmbedCreateSpec errorEmbed = CS2EmbedFormatter.formatError(
                    "⚠️ Service Temporarily Unavailable",
                    "Unable to generate leaderboard at this time. Please try again later."
            );
            handler.sendMessage(errorEmbed);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("0")
                .withIdentifiers("cs2leaderboard", "cs2 leaderboard")
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2leaderboard", "cs2 leaderboard");
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
                .name("cs2leaderboard")
                .description("View the server's CS2 leaderboard ranked by Faceit Elo")
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
                "\n- '!cs2 leaderboard' to view the server leaderboard" +
                "\n- '/cs2leaderboard' as a slash command" +
                "\n\n*View the top 10 linked players ranked by Faceit Elo with rank position, " +
                "level, K/D ratio, and server average Elo. Leaderboard updates every 10 minutes.*";
    }
}
