package com.georgster.game.cs2;

import com.georgster.ParseableCommand;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.cs2.commands.*;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Main router command for CS2 Faceit integration.
 * <p>
 * Routes to subcommands based on user input:
 * - link: Link Faceit account
 * - unlink: Unlink Faceit account
 * - help: Display help information
 * - match: View match report (Phase 4)
 * - stats: View player statistics (Phase 5)
 * - compare: Compare players (Phase 6)
 * - history: View match history (Phase 8)
 * - leaderboard: View server leaderboard (Phase 7)
 * - team: View team statistics (Phase 9)
 * <p>
 * Usage: !cs2 <subcommand> [args]
 * <p>
 * Requires CS2COMMAND permission for all subcommands.
 */
public class CS2Command implements ParseableCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(CS2Command.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        
        // Debug prints to trace CS2 command flow and argument casing
        try {
            System.out.println("[DEBUG CS2Command] Raw formatted message: '" + event.getDiscordEvent().getFormattedMessage() + "'");
            System.out.println("[DEBUG CS2Command] Parsed args at router: " + event.getParsedArguments().getArguments());
            System.out.flush();
        } catch (Exception e) {
            System.out.println("[DEBUG CS2Command] Exception in debug prints: " + e.getMessage());
            e.printStackTrace();
        }
        
        SubcommandSystem subcommands = event.createSubcommandSystem();
        
        try {
            // Link subcommand
            subcommands.on(p -> {
                try {
                    CS2LinkCommand linkCommand = new CS2LinkCommand();
                    linkCommand.execute(event);
                } catch (FaceitAPIException e) {
                    logger.error("Failed to initialize CS2LinkCommand: {}", e.getMessage(), e);
                    handler.sendMessage("Failed to initialize Faceit API client. Please contact server admins.", 
                            "CS2", MessageFormatting.ERROR);
                }
            }, "link");
            
            // Unlink subcommand
            subcommands.on(p -> {
                CS2UnlinkCommand unlinkCommand = new CS2UnlinkCommand();
                unlinkCommand.execute(event);
            }, "unlink");
            
            // Help subcommand
            subcommands.on(p -> {
                CS2HelpCommand helpCommand = new CS2HelpCommand();
                helpCommand.execute(event);
            }, "help");
            
            // Match subcommand
            subcommands.on(p -> {
                try {
                    CS2MatchCommand matchCommand = new CS2MatchCommand();
                    matchCommand.execute(event);
                } catch (FaceitAPIException e) {
                    logger.error("Failed to initialize CS2MatchCommand: {}", e.getMessage(), e);
                    handler.sendMessage("Failed to initialize Faceit API client. Please contact server admins.", 
                            "CS2 Match", MessageFormatting.ERROR);
                }
            }, "match");
            
            // Stats subcommand (Phase 5)
            subcommands.on(p -> {
                try {
                    CS2StatsCommand statsCommand = new CS2StatsCommand();
                    statsCommand.execute(event);
                } catch (FaceitAPIException e) {
                    logger.error("Failed to initialize CS2StatsCommand: {}", e.getMessage(), e);
                    handler.sendMessage("Failed to initialize Faceit API client. Please contact server admins.", 
                            "CS2 Stats", MessageFormatting.ERROR);
                }
            }, "stats");
            
            // History subcommand (Phase 8 - not yet implemented)
            subcommands.on(p -> {
                handler.sendMessage("⚠️ **Coming Soon**\n\n" +
                        "The `!cs2 history` command is not yet implemented. " +
                        "This feature will be available in Phase 8.", 
                        "CS2 History", MessageFormatting.INFO);
            }, "history");
            
            // Compare subcommand (Phase 6)
            subcommands.on(p -> {
                try {
                    CS2CompareCommand compareCommand = new CS2CompareCommand();
                    compareCommand.execute(event);
                } catch (FaceitAPIException e) {
                    logger.error("Failed to initialize CS2CompareCommand: {}", e.getMessage(), e);
                    handler.sendMessage("Failed to initialize Faceit API client. Please contact server admins.", 
                            "CS2 Compare", MessageFormatting.ERROR);
                }
            }, "compare");
            
            // Leaderboard subcommand (Phase 7)
            subcommands.on(p -> {
                try {
                    CS2LeaderboardCommand leaderboardCommand = new CS2LeaderboardCommand();
                    leaderboardCommand.execute(event);
                } catch (FaceitAPIException e) {
                    logger.error("Failed to initialize CS2LeaderboardCommand: {}", e.getMessage(), e);
                    handler.sendMessage("Failed to initialize Faceit API client. Please contact server admins.", 
                            "CS2 Leaderboard", MessageFormatting.ERROR);
                }
            }, "leaderboard", "lb");
            
            // Team subcommand (Phase 9 - not yet implemented)
            subcommands.on(p -> {
                handler.sendMessage("⚠️ **Coming Soon**\n\n" +
                        "The `!cs2 team` command is not yet implemented. " +
                        "This feature will be available in Phase 9.", 
                        "CS2 Team", MessageFormatting.INFO);
            }, "team");
            
            // If no subcommand matched and system hasn't executed, show help
            if (!subcommands.hasExecuted()) {
                System.out.println("[DEBUG CS2Command] No subcommand matched! Args were: " + event.getParsedArguments().getArguments());
                CS2HelpCommand helpCommand = new CS2HelpCommand();
                helpCommand.execute(event);
            } else {
                System.out.println("[DEBUG CS2Command] Subcommand executed successfully");
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error in CS2Command router: {}", e.getMessage(), e);
            handler.sendMessage("An unexpected error occurred. Use `!cs2 help` for usage information.", 
                    "CS2", MessageFormatting.ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("cs2");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        System.out.println("[DEBUG CS2Command] getCommandParser() called");
        System.out.flush();
        try {
            // Pattern: 1R (required subcommand - must be an identifier), VO (variable optional args after subcommand)
            // Rules: I (first arg must be identifier), > (second arg must come after identifier)
            // This will parse "compare player1 player2" as ["compare", "player1 player2"]
            // And "compare player1" as ["compare", "player1"]
            // And "compare" as ["compare"]
            CommandParser parser = new ParseBuilder("1R", "VO")
                    .withIdentifiers("link", "unlink", "help", "match", "stats", "compare", "history", "leaderboard", "lb", "team")
                    .withRules("I", ">")
                    .withoutAutoFormatting()
                    .build();
            System.out.println("[DEBUG CS2Command] Parser created successfully with pattern 1R VO");
            System.out.flush();
            return parser;
        } catch (Exception e) {
            System.out.println("[DEBUG CS2Command] ERROR creating parser: " + e.getMessage());
            e.printStackTrace();
            System.out.flush();
            throw e;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name("cs2")
                .description("CS2 Faceit integration commands")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("action")
                        .description("The CS2 action to perform")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("link")
                                .value("link")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("unlink")
                                .value("unlink")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("help")
                                .value("help")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("match")
                                .value("match")
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
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("leaderboard")
                                .value("leaderboard")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("team")
                                .value("team")
                                .build())
                        .build())
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        // Help command is always accessible
        if (!args.isEmpty() && args.get(0).equalsIgnoreCase("help")) {
            return PermissibleAction.DEFAULT;
        }
        return PermissibleAction.CS2COMMAND;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
                "\n- '!cs2 <subcommand> [args]' to interact with CS2 Faceit integration" +
                "\n- Available subcommands: link, unlink, match, stats, history, compare, leaderboard, team, help" +
                "\n- Use '!cs2 help' for detailed usage information" +
                "\n\n*Main command for CS2 Faceit integration. Use help subcommand for full documentation.*";
    }
}
