package com.georgster.plinko;

import com.georgster.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;

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
     */

    public void execute(MessageCreateEvent event) {
        StringBuilder message = new StringBuilder(event.getMessage().getContent());
        message.delete(message.indexOf("!plinko"), message.indexOf("!plinko") + 8);
        if (message.toString().startsWith("play")) {
            PlinkoGame game = new PlinkoGame(event);
            if (game.getGuild() != null) {
                game.play();
            } else {
                event.getMessage().getChannel().block().createMessage("I could not get the id of this server").block();
            }
        } else if (message.toString().startsWith("rewards")) {
            if (message.toString().contains("set")) {
                
            }
        } else {
            help();
        }
    }

    public String help() {
        return "Placeholder instructions for PlinkoCommand()";
    }

}
