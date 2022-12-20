package com.georgster.reserve;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.georgster.profile.ProfileHandler;
import com.georgster.util.SoapGeneralHandler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * Represents the command for reserving to and creating events.
 */
public class ReserveCommand implements Command {
    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event) {
        List<String> message = Arrays.asList(event.getMessage().getContent().split(" "));
        if (message.size() < 2) {
            event.getMessage().getChannel().block().createMessage(help()).block();
            return;
        }
        try {
            TextChannel channel = (TextChannel) event.getMessage().getChannel().block();
            ReserveEvent reserve = assignCorrectCommand(message, event.getGuild().block().getId().asString(), channel.getName());

            if (ProfileHandler.checkEventDuplicates(event.getGuild().block().getId().asString(), reserve)) {
                if (!reserve.isFull()) {
                    event.getMessage().getAuthor().ifPresent(user -> {
                        if (reserve.alreadyReserved(user.getTag())) {
                            event.getMessage().getChannel().block().createMessage("You have already reserved for this event").block();
                        } else {
                            ActionWriter.writeAction("Reserving a user to event " + reserve.getIdentifier() + " in guild " + event.getGuild().block().getId().asString());
                            ProfileHandler.removeEvent(event.getGuild().block().getId().asString(), reserve);
                            reserve.addReserved();
                            reserve.addReservedUser(user.getTag());
                            ProfileHandler.addEvent(event.getGuild().block().getId().asString(), reserve);
                            event.getMessage().getChannel().block().createMessage("Number of people reserved to event " + reserve.getIdentifier() + ": " + reserve.getReserved() + "/" + reserve.getNumPeople()).block();
                        }
                    });
                } else {
                    event.getMessage().getChannel().block().createMessage("Event " + reserve.getIdentifier() + " is full").block();
                }
            } else {
                ActionWriter.writeAction("Creating a new event " + reserve.getIdentifier() + " in guild " + event.getGuild().block().getId().asString());
                event.getMessage().getAuthor().ifPresent(user -> reserve.addReservedUser(user.getTag()));
                ProfileHandler.addEvent(event.getGuild().block().getId().asString(), reserve);
                SoapGeneralHandler.runDaemon(() -> ReserveEventHandler.scheduleEvent(reserve, event.getMessage().getChannel().block(), event.getGuild().block().getId().asString()));
                String messageString = "";
                if (reserve.isTimeless()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled with " + reserve.getNumPeople() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else if (reserve.isUnlimited()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + reserve.getTime()+ "! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + reserve.getTime() + " with " + reserve.getNumPeople() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
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

        if (message.size() == 2) {
            if (ProfileHandler.eventExists(id, message.get(1))) {
                return ProfileHandler.pullEvent(id, message.get(1));
            } else {
                throw new IllegalArgumentException("This event doesn't exist. Type !help reserve to see how to make a new event.");
            }
        } else if (message.size() == 3) {
            try {
                return new ReserveEvent(message.get(1), Integer.parseInt(message.get(2)), channelName);
            } catch (NumberFormatException e) {
                try {
                    return new ReserveEvent(message.get(1), SoapGeneralHandler.timeConverter(message.get(2)), channelName);
                } catch (IllegalArgumentException e2) {
                    throw new IllegalArgumentException(e2.getMessage());
                }
            }
        } else if (message.size() == 4) {
            try {
                return new ReserveEvent(message.get(1), Integer.parseInt(message.get(2)), SoapGeneralHandler.timeConverter(message.get(3)), channelName);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Incorrect reserve event format, type !help reserve to see how to make a new event.");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        throw new IllegalArgumentException("Incorrect Reserve Event Format");
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
