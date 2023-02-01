package com.georgster.misc;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A PongCommand simply will return all instances of a user's message of "ping"
 * with the same number of "pong"s given the message began with the "!ping" command.
 * 
 * @implements {@code Command} the general definiton for a SoapBot command.
 */
public class PongCommand implements Command {
    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
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
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<PongCommand> logger = new MultiLogger<>(manager, PongCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        List<String> args = CommandParser.parseGeneric(pipeline.getFormattedMessage().toLowerCase());
        StringBuilder fullMessage = new StringBuilder();
        int counter = 0;
        while(args.contains(msg)) {
            args.remove(msg);
            fullMessage.append("pong! ");
            counter++;
        }
        logger.append("\tResponding to a !ping command request with " + counter + " pongs", LogDestination.API, LogDestination.NONAPI);
        manager.sendText(fullMessage.toString().trim(), "You said ping " + counter + " times");

        logger.sendAll();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("ping");
    }

    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Responds with pong! for each 'ping' in your message")
                .build();
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
