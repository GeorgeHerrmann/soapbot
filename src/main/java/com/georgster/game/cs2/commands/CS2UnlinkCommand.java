package com.georgster.game.cs2.commands;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.permissions.PermissibleAction;
import com.georgster.profile.UserProfile;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Command to unlink a Discord user's Faceit account from their SoapBot profile.
 * <p>
 * Usage: !cs2 unlink or /cs2 unlink
 * <p>
 * This command:
 * 1. Sets the user's CS2Profile to null (removes Faceit linkage)
 * 2. Persists the change to MongoDB via UserProfileManager
 * 3. Returns a confirmation message
 * <p>
 * Error Handling:
 * - User not linked → Informational message
 * - Missing permission → Permission denied error
 */
public class CS2UnlinkCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2UnlinkCommand.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        UserProfileManager profileManager = event.getClientContext().getUserProfileManager();
        
        String userId = event.getDiscordEvent().getUser().getId().asString();
        
        logger.info("Attempting to unlink Faceit account for Discord user {}", userId);
        
        try {
            // Get user profile
            UserProfile profile = profileManager.get(userId);
            
            if (profile == null || !profile.hasLinkedFaceit()) {
                logger.info("User {} has no linked Faceit account", userId);
                handler.sendMessage("ℹ️ **No Linked Account**\n\n" +
                        "You don't have a Faceit account linked to your profile.\n" +
                        "Use `!cs2 link <faceit-username>` to link an account.", 
                        "CS2 Unlink", MessageFormatting.INFO);
                return;
            }
            
            String faceitNickname = profile.getCS2Profile().getFaceitNickname();
            
            // Remove CS2 linkage
            profile.setCS2Profile(null);
            profileManager.update(profile);
            
            
            // Send confirmation
            handler.sendMessage("✅ **Faceit Account Unlinked**\n\n" +
                    "Your Faceit account (**" + faceitNickname + "**) has been unlinked from your Discord profile.\n" +
                    "You can link a new account at any time using `!cs2 link <faceit-username>`.", 
                    "CS2 Unlink", MessageFormatting.INFO);
            
        } catch (Exception e) {
            logger.error("Unexpected error during Faceit account unlink: {}", e.getMessage(), e);
            handler.sendMessage("An unexpected error occurred. Please try again later.", 
                    "CS2 Unlink", MessageFormatting.ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2unlink");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("0").build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name("cs2unlink")
                .description("Unlink your Faceit account from your Discord profile")
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
                "\n- '!cs2 unlink' to unlink your Faceit account from your Discord profile" +
                "\n- '/cs2unlink' as a slash command" +
                "\n\n*Removes the Faceit account linkage from your profile. You can link a new account at any time.*";
    }
}
