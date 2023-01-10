package com.georgster.events.reserve;

import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.georgster.events.SoapEventHandler;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.ProfileType;
import com.georgster.util.CommandParser;
import com.georgster.util.GuildManager;
import com.georgster.util.ParseBuilder;
import com.georgster.util.SoapHandler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * Represents the command for reserving to and creating events.
 */
public class ReserveCommand implements Command {
    private static final String PATTERN = "V|R 1|O 1|O";
    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        CommandParser parser = new ParseBuilder(PATTERN).withRules("X N|T T").build();
        List<String> message = parser.parse(event.getMessage().getContent());
        ProfileHandler handler = manager.getHandler();

        if (message.isEmpty()) {
            manager.sendText(help());
            return;
        }
        try {
            ReserveEvent reserve = assignCorrectEvent(manager, parser);

            if (handler.eventExists(reserve.getIdentifier())) {
                if (!reserve.isFull()) {
                    event.getMessage().getAuthor().ifPresent(user -> {
                        if (reserve.alreadyReserved(user.getTag())) {
                            manager.sendText("You have already reserved for this event");
                        } else {
                            ActionWriter.writeAction("Reserving a user to event " + reserve.getIdentifier());
                            handler.removeObject(reserve, ProfileType.EVENTS);
                            reserve.addReserved(user.getTag());
                            handler.addObject(reserve, ProfileType.EVENTS);
                           manager.sendText("Number of people reserved to event " + reserve.getIdentifier() + ": " + reserve.getReserved() + "/" + reserve.getNumPeople());
                        }
                    });
                } else {
                    manager.sendText("Event " + reserve.getIdentifier() + " is full");
                }
            } else {
                ActionWriter.writeAction("Creating a new event " + reserve.getIdentifier());
                event.getMessage().getAuthor().ifPresent(user -> reserve.addReserved(user.getTag()));
                handler.addObject(reserve, ProfileType.EVENTS);
                SoapHandler.runDaemon(() -> SoapEventHandler.scheduleEvent(reserve, manager));
                String messageString = "";
                if (reserve.isTimeless()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled with " + reserve.getNumPeople() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else if (reserve.isUnlimited()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapHandler.convertToAmPm(reserve.getTime()) + "! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapHandler.convertToAmPm(reserve.getTime()) + " with " + reserve.getNumPeople() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                }
                manager.sendText(messageString);
            }
        } catch (IllegalArgumentException e) {
            manager.sendText(e.getMessage());
        }

    }


    /**
     * Assigns the correct ReserveEvents based on the user's command message.
     * 
     * @param manager The GuildManager that is managing the guild
     * @param parser The CommandParser that is parsing the user's command message
     * @return The ReserveEvent that the user wants to create or reserve to
     * @throws IllegalArgumentException If the user's command message is in the wrong format
     */
    private ReserveEvent assignCorrectEvent(GuildManager manager, CommandParser parser) throws IllegalArgumentException {

        List<String> message = parser.getArguments();
        ProfileHandler handler = manager.getHandler();
        String channelName = ((TextChannel) manager.getActiveChannel()).getName();
        if (message.size() == 1) { //Means the user is trying to reserve to an event that already exists
            if (handler.eventExists(message.get(0))) { //If the event exists, we get the event and return it
                return handler.pullEvent(message.get(0));
            } else {
                throw new IllegalArgumentException("This event doesn't exist. Type !help reserve to see how to make a new event.");
            }
        } else if (message.size() == 2 && !handler.eventExists(message.get(0))) { //Creating an event that is either Unlimited or Timeless
            try {
                if (Integer.parseInt(message.get(1)) < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                return new ReserveEvent(message.get(0), Integer.parseInt(message.get(1)), channelName); //If the user only inputs a number, the event is Timeless
            } catch (NumberFormatException e) { //If it is not a number, it is a time
                try {
                    return new ReserveEvent(message.get(0), SoapHandler.timeConverter(message.get(1)), channelName); //Creates an Unlimited event
                } catch (IllegalArgumentException e2) { //If both of these fail, the user's command message is in the wrong format
                    throw new IllegalArgumentException(e2.getMessage());
                }
            }
        } else if (message.size() == 3 && !handler.eventExists(message.get(0))) { //Creates a new event that has a number of slots and a time
            try { //If the event doesn't already exist, we can attempt to create a new one
                if (Integer.parseInt(message.get(1)) < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                //Should be in the format !reserve [EVENTNAME] [NUMPEOPLE] [TIME]
                List<String> temp = parser.getMatchingRules("N");
                int numPeople = Integer.parseInt(temp.get(temp.size() - 1));
                manager.sendText(parser.getArguments().toString());
                temp = parser.getMatchingRules("T");
                String time = temp.get(temp.size() - 1);
                return new ReserveEvent(message.get(0), numPeople, SoapHandler.timeConverter(time), channelName);
            } catch (NumberFormatException e) { //If the user's command is in the wrong format
                throw new IllegalArgumentException("Incorrect reserve event format, type !help reserve to see how to make a new event.");
            } catch (IllegalArgumentException e) { //TimeConverter throws an IllegalArgumentException if the time is in the wrong format
                throw new IllegalArgumentException(e.getMessage());
            } catch (IndexOutOfBoundsException e) { //If the name of the event has some issue the parser can't handle
                throw new IllegalArgumentException("There is an issue with the name of the event. The event name cannot have a number unless is attached to a word." + 
                "\n\t- Example: !reserve 1 v 1 5 5:00pm is incorrect, but !reserve 1v1 5 5:00pm is correct.");
            }
        }
        throw new IllegalArgumentException("Incorrect Reserve Event Format or this event already exists, simply type !reserve [EVENTNAME] if you want to reserve to an event that exists.");
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("reserve", "res");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !reserve" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- '!reserve [EVENTNAME] [PLAYERCOUNT] [TIME]' to create a new event with for a specific time with a certain number of people" +
        "\n\t\t - This event will pop when the specified time hits" +
        "\n\t- '!reserve [EVENTNAME] [PLAYERCOUNT]' to create a new event with a certain number of people" +
        "\n\t\t - This event will pop when the specified number of people have reserved" +
        "\n\t- '!reserve [EVENTNAME] [TIME]' to create a new event for a specific time" +
        "\n\t\t - This event will pop when the specified time hits" +
        "\n\t- '!reserve [EVENTNAME]' to reserve to an event that already exists" +
        "\n\t - !events for information about the event command";
    }
}
