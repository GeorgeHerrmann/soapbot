package com.georgster.dm;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.DiscordEvent;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParsedArguments;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler;
import com.georgster.util.handler.UserInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A MessageCommand is represents the actions following a "!dm" command.
 */
public class MessageCommand implements ParseableCommand {

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        DiscordEvent discordEvent = event.getDiscordEvent();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        ParsedArguments args = event.getParsedArguments();

        StringBuilder response = new StringBuilder();
        for (String i : args.getArguments()) {
            if (!i.contains(("@"))) {
                response.append(i + " ");
            }
        }
        if (!discordEvent.getPresentUsers().isEmpty()) {
            for (User user : discordEvent.getPresentUsers()) {
                InteractionHandler userHandler = new UserInteractionHandler(user);
                logger.append("\n- Found User: " + user.getTag() + ", sending DM", LogDestination.NONAPI);

                userHandler.sendPlainMessage(response.toString());
                if (discordEvent.isChatInteraction()) {
                    ((ChatInputInteractionEvent) discordEvent.getEvent()).reply("Message sent to " + user.getTag()).withEphemeral(true).block();
                }
            }
        } else {
            logger.append("\n- No users found, sending help message",
            LogDestination.NONAPI);
            String[] output = SoapUtility.splitFirst(help());
            handler.sendMessage(output[1], output[0], MessageFormatting.ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new CommandParser("VR");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (!args.isEmpty()) {
            return PermissibleAction.MESSAGECOMMAND;
        } else {
            return PermissibleAction.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
            .name(getAliases().get(0))
            .description("Sends a private message to a user")
            .addOption(ApplicationCommandOptionData.builder()
                .name("user")
                .description("The user to send the message to")
                .type(ApplicationCommandOption.Type.USER.getValue())
                .required(true)
                .build())
            .addOption(ApplicationCommandOptionData.builder()
                .name("message")
                .description("The message to send")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(true)
                .build())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("dm", "message", "msg");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n - !message @[USERS] [MESSAGE]" +
        "\n\t- Examples: !message @georgster#8086 hello" + 
        "\n\t- Or: !message @georgster#8086 @Milkmqn#9457 hello";
    }
}
