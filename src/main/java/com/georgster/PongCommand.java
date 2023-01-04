package com.georgster;

import java.util.List;

import com.georgster.api.ActionWriter;
import com.georgster.util.CommandParser;
import com.georgster.util.GuildManager;

import discord4j.core.event.domain.message.MessageCreateEvent;

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
    public void execute(MessageCreateEvent event, GuildManager manager) {
        List<String> args = CommandParser.parseGeneric(event.getMessage().getContent());
        StringBuilder fullMessage = new StringBuilder();
        ActionWriter.writeAction("Having the pong command parser check the contents of a !ping command");
        int counter = 0;
        while(args.contains(msg)) {
            args.remove(msg);
            fullMessage.append("pong! ");
            counter++;
        }
        ActionWriter.writeAction("Responding to a !ping command request with " + counter + " pongs");
        manager.sendText(fullMessage.toString());
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
      return "Command: !ping" +
      "\n!ping to have SOAP Bot respond with pong! for each 'ping' in your message";
    }
}
