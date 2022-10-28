package com.georgster;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class PongCommand implements Command {
    String msg;
    PongCommand() {
        msg = "!ping";
    }
    @Override
    public void execute(MessageCreateEvent event) {
        Message message = event.getMessage();
            StringBuilder userMessage = new StringBuilder(message.getContent());
            StringBuilder fullMessage = new StringBuilder();
            for (int i = 0; i < userMessage.length(); i++) {
              if (userMessage.toString().contains(msg)) {
                userMessage.delete(userMessage.indexOf(msg), userMessage.indexOf(msg) + 5);
                fullMessage.append("pong!");
              }
            }
            event.getMessage().getChannel().block().createMessage(fullMessage.toString()).block();
            
      }
}
