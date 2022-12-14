package com.georgster.dm;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

public class MessageCommand implements Command {
    public void execute(MessageCreateEvent event) {
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
                user.getPrivateChannel().block().createMessage(response.toString()).block();
                ActionWriter.writeAction("Sending a DM to a user");
            }
        } else {
            message.getChannel().block().createMessage(help()).block();
        }
    }

    public String help() {
        return "Command: !message" +
        "\n\t - !message @[USERS] [MESSAGE]" +
        "\n\t\t Ex: !message @georgster#8086 hello" + 
        "\n\t\t Or: !message @georgster#8086 @Milkmqn#9457 hello";
    }
}
