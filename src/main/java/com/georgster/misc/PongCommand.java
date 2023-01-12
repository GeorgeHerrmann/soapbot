package com.georgster.misc;

import java.util.List;

import com.georgster.Command;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.CommandParser;
import com.georgster.util.GuildManager;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * A PongCommand simply will return all instances of a user's message of "ping"
 * with the same number of "pong"s given the message began with the "!ping" command.
 * 
 * @implements {@code Command} the general definiton for a SoapBot command.
 */
public class PongCommand implements Command {

    String msg;

    /**
     * Creates a PongCommand object.
     */
    public PongCommand() {
        msg = "ping";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(MessageCreateEvent event, GuildManager manager) {
        MultiLogger<PongCommand> logger = new MultiLogger<>(manager, PongCommand.class);
        logger.append("Executing: " + this.getClass().getSimpleName() + "\n", LogDestination.NONAPI);

        List<String> args = CommandParser.parseGeneric(event.getMessage().getContent());
        StringBuilder fullMessage = new StringBuilder();
        int counter = 0;
        while(args.contains(msg)) {
            args.remove(msg);
            fullMessage.append("pong! ");
            counter++;
        }
        logger.append("\tResponding to a !ping command request with " + counter + " pongs", LogDestination.API, LogDestination.NONAPI);
        manager.sendText(fullMessage.toString().trim());

        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("ping");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
      return "Command: !ping" +
      "\nAliases: " + getAliases().toString() +
      "\n!ping to have SOAP Bot respond with pong! for each 'ping' in your message";
    }
}
