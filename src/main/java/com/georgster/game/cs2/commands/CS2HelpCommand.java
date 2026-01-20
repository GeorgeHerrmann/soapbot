package com.georgster.game.cs2.commands;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.handler.GuildInteractionHandler;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

/**
 * Command to display CS2 Faceit integration help and usage information.
 * <p>
 * Usage: !cs2 help or /cs2 help
 * <p>
 * This command displays:
 * - All available CS2 commands with descriptions
 * - Usage examples for each command
 * - Player lookup format explanations
 * - Permission and performance notes
 * <p>
 * No special permissions required - accessible to all users.
 */
public class CS2HelpCommand implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2HelpCommand.class);
    private static final Color BLUE_COLOR = Color.of(0x3498db);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        
        logger.debug("Displaying CS2 help for user {}", event.getDiscordEvent().getUser().getId().asString());
        
        EmbedCreateSpec helpEmbed = EmbedCreateSpec.builder()
                .title("📖 CS2 Faceit Integration Help")
                .description("Complete guide to all available commands")
                .color(BLUE_COLOR)
                
                // Account Management
                .addField("═══ ACCOUNT MANAGEMENT ═══", "", false)
                .addField("/cs2 link <username>", 
                        "Link your Faceit account to your Discord profile\n" +
                        "Example: `/cs2 link GeorgsterCS`", false)
                .addField("/cs2 unlink", 
                        "Unlink your Faceit account from Discord", false)
                
                // Player Statistics
                .addField("═══ PLAYER STATISTICS ═══", "", false)
                .addField("/cs2 stats [@player]", 
                        "View comprehensive CS2 statistics\n" +
                        "Example: `/cs2 stats` or `/cs2 stats @friend`", false)
                .addField("/cs2 match [@player]", 
                        "View your most recent match report\n" +
                        "Example: `/cs2 match` or `/cs2 match @friend`", false)
                .addField("/cs2 history [@player]", 
                        "View last 10 matches and recent form\n" +
                        "Example: `/cs2 history`", false)
                
                // Comparisons & Leaderboards
                .addField("═══ COMPARISONS & LEADERBOARDS ═══", "", false)
                .addField("/cs2 compare @player1 @player2", 
                        "Compare two players' statistics side-by-side\n" +
                        "Example: `/cs2 compare @you @friend`", false)
                .addField("/cs2 leaderboard", 
                        "View top 10 linked players in this server ranked by Elo", false)
                
                // Team Lookup
                .addField("═══ TEAM LOOKUP ═══", "", false)
                .addField("/cs2 team <team-name>", 
                        "View team's last match statistics\n" +
                        "Example: `/cs2 team Astralis`", false)
                
                // Player Lookup Formats
                .addField("═══ PLAYER LOOKUP FORMATS ═══", "", false)
                .addField("Lookup Reference", 
                        "`@Discord_User` - Use linked account\n" +
                        "`faceit:username` - Explicit Faceit username\n" +
                        "`steam:STEAMID` - Steam ID lookup\n" +
                        "`username` - Default Faceit username", false)
                
                // Notes
                .addField("═══ NOTES ═══", "", false)
                .addField("⏱️ Performance", 
                        "Stats cached for 5 minutes. Leaderboard updates every 10 minutes.", true)
                .addField("🔐 Permissions", 
                        "All commands require `CS2COMMAND` permission", true)
                .addField("📌 API Info", 
                        "Data sourced from Faceit API. Errors handled gracefully with standardized messages.", false)
                
                .footer("Created for SoapBot | CS2 Faceit Integration", null)
                .timestamp(Instant.now())
                .build();
        
        handler.sendMessage(helpEmbed);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2help");
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
                .name("cs2help")
                .description("Display CS2 Faceit integration commands and usage")
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.DEFAULT; // No special permission needed
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
                "\n- '!cs2 help' to display all CS2 commands and usage information" +
                "\n- '/cs2help' as a slash command" +
                "\n\n*Shows comprehensive guide to all CS2 Faceit integration features.*";
    }
}
