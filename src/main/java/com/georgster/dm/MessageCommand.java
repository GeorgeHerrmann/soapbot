package com.georgster.dm;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class MessageCommand implements Command {

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord

    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<MessageCommand> logger = new MultiLogger<>(manager, MessageCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**",
        LogDestination.NONAPI, LogDestination.API);

        List<String> contents = Arrays.asList(pipeline.getFormattedMessage());

        StringBuilder response = new StringBuilder();
        for (String i : contents) {
            if (!i.contains("dm") && !i.contains(("@"))) {
                response.append(i + " ");
            }
        }
        if (!pipeline.getPresentUsers().isEmpty()) {
            for (User user : pipeline.getPresentUsers()) {
                logger.append("\n\tFound User: " + user.getTag() + ", sending DM",
                LogDestination.NONAPI);

                user.getPrivateChannel().block().createMessage(response.toString()).block();
            }
        } else {
            logger.append("\n\tNo users found, sending help message",
            LogDestination.NONAPI);
            String[] output = SoapUtility.splitFirst(help());
            manager.sendText(output[1], output[0]);
        }
        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
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
    public boolean needsDispatcher() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("dm", "message", "msg");
    }

    public String help() {
        return "Command: !message" +
        "\nAliases: " + getAliases().toString() +
        "\n\t - !message @[USERS] [MESSAGE]" +
        "\n\t\t Ex: !message @georgster#8086 hello" + 
        "\n\t\t Or: !message @georgster#8086 @Milkmqn#9457 hello";
    }
}
