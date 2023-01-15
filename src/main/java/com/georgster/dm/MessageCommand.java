package com.georgster.dm;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

public class MessageCommand implements Command {

    public void execute(MessageCreateEvent event, GuildManager manager) {
        MultiLogger<MessageCommand> logger = new MultiLogger<>(manager, MessageCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**",
        LogDestination.NONAPI, LogDestination.API);

        Message message = event.getMessage();
        List<String> contents = Arrays.asList(message.getContent().split(" "));

        StringBuilder response = new StringBuilder();
        for (String i : contents) {
            if (!i.equals("!dm") && !i.contains(("@"))) {
                response.append(i + " ");
            }
        }
        if (!message.getUserMentions().isEmpty()) {
            for (User user : message.getUserMentions()) {
                logger.append("\n\tFound User: " + user.getTag() + ", sending DM",
                LogDestination.NONAPI);

                user.getPrivateChannel().block().createMessage(response.toString()).block();
            }
        } else {
            logger.append("\n\tNo users found, sending help message",
            LogDestination.NONAPI);
            message.getChannel().block().createMessage(help()).block();
        }
        logger.sendAll();
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
