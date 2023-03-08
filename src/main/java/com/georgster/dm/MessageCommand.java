package com.georgster.dm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A MessageCommand is represents the actions following a "!dm" command.
 */
public class MessageCommand implements Command {

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord

    /**
     * {@inheritDoc}
     */
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<MessageCommand> logger = new MultiLogger<>(manager, MessageCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**",
        LogDestination.NONAPI, LogDestination.API);

        List<String> contents = new ArrayList<>(Arrays.asList(pipeline.getFormattedMessage().split(" ")));
        contents.remove(0);

        StringBuilder response = new StringBuilder();
        for (String i : contents) {
            if (!i.contains(("@"))) {
                response.append(i + " ");
            }
        }
        if (pipeline.getPermissionsManager().hasPermissionSendError(manager, logger, getRequiredPermission(), pipeline.getAuthorAsMember())) {
            if (!pipeline.getPresentUsers().isEmpty()) {
                for (User user : pipeline.getPresentUsers()) {
                    logger.append("\n\tFound User: " + user.getTag() + ", sending DM",
                    LogDestination.NONAPI);
    
                    user.getPrivateChannel().block().createMessage(response.toString()).block();
                    if (pipeline.isChatInteraction()) {
                        ((ChatInputInteractionEvent) pipeline.getEvent()).reply("Message sent to " + user.getTag()).withEphemeral(true).block();
                    }
                }
            } else {
                logger.append("\n\tNo users found, sending help message",
                LogDestination.NONAPI);
                String[] output = SoapUtility.splitFirst(help());
                manager.sendText(output[1], output[0]);
            }
        } else {
            logger.append("\n\tUser does not have permission to use this command", LogDestination.NONAPI);
            manager.sendText("You do not have permission to use this command", "Permission Denied");
        }
        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission() {
        return PermissibleAction.MESSAGECOMMAND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

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
        return "Command: !message" +
        "\nAliases: " + getAliases().toString() +
        "\n\t - !message @[USERS] [MESSAGE]" +
        "\n\t\t Ex: !message @georgster#8086 hello" + 
        "\n\t\t Or: !message @georgster#8086 @Milkmqn#9457 hello";
    }
}
