package com.georgster.misc;

import java.util.ArrayList;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
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
    public void execute(CommandPipeline pipeline) {
        MultiLogger logger = pipeline.getLogger();
        GuildManager manager = pipeline.getGuildManager();

        List<String> args = new ArrayList<>(List.of(pipeline.getEventTransformer().getFormattedMessage().toLowerCase().replace("!", "").split(" ")));
        StringBuilder fullMessage = new StringBuilder();
        int counter = 0;
        while(args.contains(msg)) {
            args.remove(msg);
            fullMessage.append("pong! ");
            counter++;
        }
        logger.append("\tResponding to a !ping command request with " + counter + " pongs", LogDestination.API, LogDestination.NONAPI);
        manager.sendText(fullMessage.toString().trim(), "You said ping " + counter + " times");
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
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Responds with pong! for each 'ping' in your message")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("pings")
                        .description("Additional pings to respond to")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
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
