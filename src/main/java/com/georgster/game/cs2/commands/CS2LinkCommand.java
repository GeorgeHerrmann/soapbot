package com.georgster.game.cs2.commands;

import com.georgster.ParseableCommand;
import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.exception.PlayerNotFoundException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.cs2.util.CS2EmbedFormatter;
import com.georgster.permissions.PermissibleAction;
import com.georgster.profile.UserProfile;
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
 * Command to link a Discord user's Faceit account to their SoapBot profile.
 * <p>
 * Usage: !cs2 link <faceit-username> or /cs2 link <faceit-username>
 * <p>
 * This command:
 * 1. Validates the Faceit username via the Faceit API
 * 2. Creates/updates the user's CS2Profile with Faceit account details
 * 3. Persists the link to MongoDB via UserProfileManager
 * 4. Returns a success embed with player profile (nickname, Elo, level)
 * <p>
 * Error Handling:
 * - Invalid username → PlayerNotFoundException → Error message displayed
 * - API errors → FaceitAPIException → "Service temporarily unavailable" (FR-011)
 * - Missing permission → Permission denied error
 */
public class CS2LinkCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2LinkCommand.class);
    
    private final FaceitAPIClient apiClient;
    
    /**
     * Creates a new CS2LinkCommand instance.
     * 
     * @throws FaceitAPIException if FaceitAPIClient cannot be initialized
     */
    public CS2LinkCommand() throws FaceitAPIException {
        this.apiClient = new FaceitAPIClient();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        UserProfileManager profileManager = event.getClientContext().getUserProfileManager();
        
        List<String> args = event.getParsedArguments().getArguments();
        // Debug prints to trace CS2 link argument flow and casing
        try {
            System.out.println("[DEBUG CS2LinkCommand] Incoming parsed args: " + args);
        } catch (Exception e) {
            // Ignore debug-print issues
        }
        if (args.isEmpty()) {
            handler.sendMessage("Please provide your Faceit username. Usage: `!cs2 link <faceit-username>`", 
                    "CS2 Link", MessageFormatting.ERROR);
            return;
        }
        
        // When invoked via the cs2 router, args typically look like: ["link", "<username ...>"]
        // When invoked directly as /cs2link or !cs2link, args look like: ["<username ...>"]
        String faceitUsername;
        if (args.size() >= 2 && args.get(0).equalsIgnoreCase("link")) {
            faceitUsername = args.get(1).trim();
        } else {
            faceitUsername = args.get(0).trim();
        }
        try {
            System.out.println("[DEBUG CS2LinkCommand] faceitUsername before API call: '" + faceitUsername + "'");
        } catch (Exception e) {
            // Ignore debug-print issues
        }
        String userId = event.getDiscordEvent().getUser().getId().asString();
        
        
        try {
            // Validate account exists via Faceit API
            FaceitPlayer player = apiClient.fetchPlayer(faceitUsername);
            
            // Get or create user profile
            UserProfile profile = profileManager.get(userId);
            if (profile == null) {
                // Create new profile if doesn't exist
                String username = event.getDiscordEvent().getUser().getUsername();
                String guildId = event.getDiscordEvent().getGuild().getId().asString();
                profile = new UserProfile(guildId, userId, username);
            }
            
            // Create or update CS2Profile
            UserProfile.CS2Profile cs2Profile = new UserProfile.CS2Profile();
            cs2Profile.setFaceitPlayerId(player.getPlayerId());
            cs2Profile.setFaceitNickname(player.getNickname());
            cs2Profile.setSteamId(player.getSteamId());
            cs2Profile.setLinked(true);
            cs2Profile.markUpdated();
            
            // Update profile with CS2 linkage
            profile.setCS2Profile(cs2Profile);
            profileManager.update(profile);
            
            
            // Send success embed
            EmbedCreateSpec successEmbed = CS2EmbedFormatter.formatLinkSuccess(player);
            handler.sendMessage(successEmbed);
            
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found: {}", faceitUsername);
            try {
                System.out.println("[DEBUG CS2LinkCommand] PlayerNotFoundException for username: '" + faceitUsername + "'");
            } catch (Exception ex) {
                // Ignore debug-print issues
            }
            handler.sendMessage("❌ **Account Link Failed**\n\n" +
                    "Player not found. Please check your Faceit username and try again.\n\n" +
                    "**Username Provided**: " + faceitUsername + "\n" +
                    "**Troubleshooting**: Visit faceit.com to verify your username", 
                    "CS2 Link", MessageFormatting.ERROR);
            
        } catch (FaceitAPIException e) {
            logger.error("Faceit API error during link: {}", e.getMessage(), e);
            handler.sendMessage("⚠️ **Service Temporarily Unavailable**\n\n" +
                    "Unable to link account at this time. Please try again later.", 
                    "CS2 Link", MessageFormatting.ERROR);
            
        } catch (Exception e) {
            logger.error("Unexpected error during Faceit account link: {}", e.getMessage(), e);
            handler.sendMessage("An unexpected error occurred. Please try again later.", 
                    "CS2 Link", MessageFormatting.ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2link");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        // Disable auto-formatting so Faceit usernames preserve user-provided casing
        return new ParseBuilder("1R").withoutAutoFormatting().build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name("cs2link")
                .description("Link your Faceit account to your Discord profile")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("faceit_username")
                        .description("Your Faceit username (e.g., GeorgsterCS)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .minLength(1)
                        .maxLength(32)
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
                "\n- '!cs2 link <faceit-username>' to link your Faceit account to your Discord profile" +
                "\n- '/cs2link <faceit-username>' as a slash command" +
                "\n\n*Links your Faceit account for easy stat tracking. Example: !cs2 link GeorgsterCS*";
    }
}
