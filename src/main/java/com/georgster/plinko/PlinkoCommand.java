package com.georgster.plinko;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the actions following the execution of a "!plinko" command.
 */
public class PlinkoCommand implements ParseableCommand {
    
    /*
     * PlinkoCommand general idea:
     * - !plinko will list all available commands
     * - !plinko play will start a PlinkoGame with the current rewards
     * - !plinko rewards will show the currently saved rewards
     * - !plinko rewards set [NUM] will change a specific reward
     * 
     * A "profile" for a server will be created in a text file to save rewards per server
     * A generic PlinkoReward object will be used to store each reward
     * The initial rewards will only be String messages, but will evolve over time
     * 
     * Note: If you are reading this the full Plinko features are mid development.
     */
    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private static final String PATTERN = "1|R 1|O";

    /**
     * {@inheritDoc}
     */
    public void execute(CommandPipeline pipeline) {
        final GuildManager manager = pipeline.getGuildManager();
        final MultiLogger logger = pipeline.getLogger();
        final CommandParser parser = pipeline.getCommandParser();

        PlinkoGame game = new PlinkoGame(pipeline, manager); //Creates a PlinkoGame, to do: Restructure and move this inside the play conditional
        if (parser.get(0).equals("play")) {
            logger.append("\tBeginning the simulation of a plinko game", LogDestination.NONAPI, LogDestination.API);
            game.play();
        } else if (parser.get(0).equals("board")) {
            logger.append("\tShowing a blank Plinko Board", LogDestination.NONAPI, LogDestination.API);
            game.showBoard();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.contains("play")) {
            return PermissibleAction.PLINKOGAME;
        } else {
            return PermissibleAction.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new CommandParser(PATTERN);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("plinko");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Play a game of Plinko!")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("command")
                        .description("Play a game of Plinko!")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("play")
                                .value("play")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("board")
                                .value("board")
                                .build())
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !plinko" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- '!plinko play' to simulate a game of plinko" +
        "\n\t- '!plinko board' to show an empty plinko board";
    }

}
