package com.georgster.plinko;

import com.georgster.Command;
import com.georgster.api.ActionWriter;

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

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event) {
        ActionWriter.writeAction("Having the Plinko Command parser check the contents of a !plinko command");
        StringBuilder message = new StringBuilder(event.getMessage().getContent().toLowerCase()); //This is the user's message that prompted this execution
        message.delete(message.indexOf("!plinko"), message.indexOf("!plinko") + 8);
        PlinkoGame game = new PlinkoGame(event); //Creates a PlinkoGame, to do: Restructure and move this inside the play conditional
        if (message.toString().startsWith("play")) {
            message.delete(message.indexOf("play"), message.indexOf("play") + 5);
            ActionWriter.writeAction("Beginning the simulation of a plinko game");
            game.play();
        } else if (message.toString().startsWith("board")) {
            ActionWriter.writeAction("Showing a blank Plinko Board");
            game.showBoard();
        } else {
            ActionWriter.writeAction("Showing information on how to use the plinko command");
            event.getMessage().getChannel().block().createMessage(help()).block();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !plinko" +
        "\nUsage:" +
        "\n\t- '!plinko play' to simulate a game of plinko" +
        "\n\t- '!plinko board' to show an empty plinko board";
    }

}
