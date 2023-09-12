package com.georgster.misc;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command which can be used to have SOAP Bot echo a message.
 */
public class EchoCommand implements ParseableCommand {
    
    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();
        String message = event.getParsedArguments().get(0);

        logger.append("- Echoing a message in a TextChannel", LogDestination.NONAPI, LogDestination.API);
        handler.sendPlainMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new ParseBuilder("VR").withoutAutoFormatting().build();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("echo", "say", "repeat");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !echo [MESSAGE] - For SOAP Bot to echo your message";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (!args.isEmpty()) {
            return PermissibleAction.ECHOCOMMAND;
        }
        return PermissibleAction.DEFAULT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {

        return ApplicationCommandRequest.builder()
            .name("echo")
            .description("Echo a message")
            .addOption(ApplicationCommandOptionData.builder()
                        .name("message")
                        .description("The message to echo")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
            .build();
    }
}
