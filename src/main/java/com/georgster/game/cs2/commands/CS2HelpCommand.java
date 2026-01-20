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
                .title("CS2 Faceit Commands")
                .color(BLUE_COLOR)
                
                .addField("Account", 
                        "`/cs2 link <username>` - Faceit username (Case-sensitive)\n" +
                        "`/cs2 unlink` - Unlink your account", false)
                
                .addField("Statistics (No parameters = yourself)", 
                        "`/cs2 stats [@player]` - View player statistics\n" +
                        "`/cs2 match [@player]` - View most recent match\n" +
                        "`/cs2 history [@player]` - View last 10 matches", false)
                
                .addField("Comparison", 
                        "`/cs2 compare @player1 @player2` - Compare two players\n" +
                        "`/cs2 leaderboard` - View server leaderboard", false)
                
                .addField("Player Lookup", 
                        "`@user` - Linked Discord user\n" +
                        "`username` - Faceit username\n" +
                        "`faceit:username` - Explicit Faceit lookup\n" +
                        "`steam:id` - Steam ID lookup", false)
                
                .footer("Data from Faceit API", null)
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
