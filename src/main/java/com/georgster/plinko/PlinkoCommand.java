package com.georgster.plinko;

import java.util.List;

import com.georgster.Command;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Represents the actions following the execution of a "!plinko" command.
 */
public class PlinkoCommand implements Command {
    
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
    private static final String PATTERN = "1|R 1|O";

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        MultiLogger<PlinkoCommand> logger = new MultiLogger<>(manager, PlinkoCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        CommandParser parser = new CommandParser(PATTERN);
        try {
            parser.parse(event.getMessage().getContent().toLowerCase());

            logger.append("\tParsed: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

            PlinkoGame game = new PlinkoGame(event); //Creates a PlinkoGame, to do: Restructure and move this inside the play conditional
            if (parser.get(0).equals("play")) {
                logger.append("\tBeginning the simulation of a plinko game", LogDestination.NONAPI, LogDestination.API);
                game.play();
            } else if (parser.get(0).equals("board")) {
                logger.append("\tShowing a blank Plinko Board", LogDestination.NONAPI, LogDestination.API);
                game.showBoard();
            }
        } catch (Exception e) {
            logger.append("\tInvalid command format", LogDestination.NONAPI);
            manager.sendText(help());
        }

        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasWizard() {
        return false;
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
    public String help() {
        return "Command: !plinko" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- '!plinko play' to simulate a game of plinko" +
        "\n\t- '!plinko board' to show an empty plinko board";
    }

}
