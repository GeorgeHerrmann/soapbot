package com.georgster;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

/**
 * A PongCommand simply will return all instances of a user's message of "ping"
 * with the same number of "pong"s given the message began with the "!ping" command.
 * 
 * @implements {@code Command} the general definiton for a SoapBot command.
 */
public class PongCommand implements Command {

    String msg;

    /**
     * Creates a PongCommand object. Though this implementation of
     * creating objects for each command seems trivial for this command, it will
     * be more useful for more complex actions. It also allows me to easily make a map
     * to store all the commands in App.java.
     */
    PongCommand() {
        msg = "ping";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(MessageCreateEvent event) {
        Message message = event.getMessage();
        StringBuilder userMessage = new StringBuilder(message.getContent().toLowerCase());
        StringBuilder fullMessage = new StringBuilder();
        for (int i = 0; i < userMessage.length(); i++) {
          if (userMessage.toString().contains(msg)) {
            userMessage.delete(userMessage.indexOf(msg), userMessage.indexOf(msg) + 4);
            fullMessage.append("pong!");
          }
        }
        event.getMessage().getChannel().block().createMessage(fullMessage.toString()).block();
            
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
      return "Command: !ping" +
      "\n!ping to have SOAP Bot respond with pong! for each 'ping' in your message";
    }
}
