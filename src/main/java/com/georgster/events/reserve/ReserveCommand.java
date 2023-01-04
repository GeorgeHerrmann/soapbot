package com.georgster.events.reserve;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.georgster.events.SoapEventHandler;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.ProfileType;
import com.georgster.util.GuildManager;
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
        List<String> message = Arrays.asList(event.getMessage().getContent().toLowerCase().split(" "));
        String id = event.getGuild().block().getId().asString();
        if (message.size() < 2) {
            event.getMessage().getChannel().block().createMessage(help()).block();
            return;
        }
        try {
            TextChannel channel = (TextChannel) event.getMessage().getChannel().block();
            ReserveEvent reserve = assignCorrectCommand(message, id, channel.getName());

            if (ProfileHandler.eventExists(id, reserve.getIdentifier())) { //Change to eventExists()
                if (!reserve.isFull()) {
                    event.getMessage().getAuthor().ifPresent(user -> {
                        if (reserve.alreadyReserved(user.getTag())) {
                            event.getMessage().getChannel().block().createMessage("You have already reserved for this event").block();
                        } else {
                            ActionWriter.writeAction("Reserving a user to event " + reserve.getIdentifier() + " in guild " + event.getGuild().block().getId().asString());
                            ProfileHandler.removeObject(id, reserve, ProfileType.EVENTS);
                            reserve.addReserved();
                            reserve.addReservedUser(user.getTag());
                            ProfileHandler.addObject(id, reserve, ProfileType.EVENTS);
                            event.getMessage().getChannel().block().createMessage("Number of people reserved to event " + reserve.getIdentifier() + ": " + reserve.getReserved() + "/" + reserve.getNumPeople()).block();
                        }
                    });
                } else {
                    event.getMessage().getChannel().block().createMessage("Event " + reserve.getIdentifier() + " is full").block();
                }
            } else {
                ActionWriter.writeAction("Creating a new event " + reserve.getIdentifier() + " in guild " + event.getGuild().block().getId().asString());
                event.getMessage().getAuthor().ifPresent(user -> reserve.addReservedUser(user.getTag()));
                ProfileHandler.addObject(id, reserve, ProfileType.EVENTS);
                SoapHandler.runDaemon(() -> SoapEventHandler.scheduleEvent(reserve, event.getMessage().getChannel().block(), event.getGuild().block().getId().asString()));
                String messageString = "";
                if (reserve.isTimeless()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled with " + reserve.getNumPeople() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else if (reserve.isUnlimited()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapHandler.convertToAmPm(reserve.getTime()) + "! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapHandler.convertToAmPm(reserve.getTime()) + " with " + reserve.getNumPeople() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                }
                event.getMessage().getChannel().block().createMessage(messageString).block();
            }
        } catch (IllegalArgumentException e) {
            event.getMessage().getChannel().block().createMessage(e.getMessage()).block();
        }

    }

    /**
     * Assigns the correct ReserveEvents based on the user's command message.
     * 
     * @param message The user's command message
     * @param id The guild's id
     * @param channelName The channel's name
     * @return The ReserveEvent that the user wants to create or reserve to
     * @throws IllegalArgumentException If the user's command message is in the wrong format
     */
    private ReserveEvent assignCorrectCommand(List<String> message, String id, String channelName) throws IllegalArgumentException {

        if (message.size() == 2) { //Means the user is trying to reserve to an event that already exists
            if (ProfileHandler.eventExists(id, message.get(1))) { //If the event exists, we get the event and return it
                return ProfileHandler.pullEvent(id, message.get(1));
            } else {
                throw new IllegalArgumentException("This event doesn't exist. Type !help reserve to see how to make a new event.");
            }
        } else if (message.size() == 3 && !ProfileHandler.eventExists(id, message.get(1))) { //Creating an event that is either Unlimited or Timeless
            try {
                if (Integer.parseInt(message.get(2)) < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                return new ReserveEvent(message.get(1), Integer.parseInt(message.get(2)), channelName); //If the user only inputs a number, the event is Timeless
            } catch (NumberFormatException e) { //If it is not a number, it is a time
                try {
                    return new ReserveEvent(message.get(1), SoapHandler.timeConverter(message.get(2)), channelName); //Creates an Unlimited event
                } catch (IllegalArgumentException e2) { //If both of these fail, the user's command message is in the wrong format
                    throw new IllegalArgumentException(e2.getMessage());
                }
            }
        } else if (message.size() == 4 && !ProfileHandler.eventExists(id, message.get(1))) { //Creates a new event that has a number of slots and a time
            try { //If the event doesn't already exist, we can attempt to create a new one
                if (Integer.parseInt(message.get(2)) < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                //Should be in the format !reserve [EVENTNAME] [NUMPEOPLE] [TIME]
                return new ReserveEvent(message.get(1), Integer.parseInt(message.get(2)), SoapHandler.timeConverter(message.get(3)), channelName);
            } catch (NumberFormatException e) { //If the user's command is in the wrong format
                throw new IllegalArgumentException("Incorrect reserve event format, type !help reserve to see how to make a new event.");
            } catch (IllegalArgumentException e) { //TimeConverter throws an IllegalArgumentException if the time is in the wrong format
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        throw new IllegalArgumentException("Incorrect Reserve Event Format or this event already exists, simply type !reserve [EVENTNAME] if you want to reserve to an event that exists.");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !reserve" +
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
