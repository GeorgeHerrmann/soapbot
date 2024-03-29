package com.georgster.misc;

import java.util.ArrayList;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the actions following a "!ping" command.
 */
public class PongCommand implements Command {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();

        List<String> args = new ArrayList<>(List.of(event.getDiscordEvent().getFormattedMessage().toLowerCase().replace("!", "").split(" ")));
        StringBuilder fullMessage = new StringBuilder();
        int counter = 0;
        while(args.contains("pong")) {
            args.remove("pong");
            fullMessage.append("pong! ");
            counter++;
        }
        logger.append("- Responding to a !pong command request with " + counter + " pongs", LogDestination.API, LogDestination.NONAPI);
        handler.sendMessage(fullMessage.toString().trim(), "You said pong " + counter + " times");
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("pong");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
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
      return "Aliases: " + getAliases().toString() +
      "\n!pong to have SOAP Bot respond with pong! for each 'ping' in your message";
    }
}
