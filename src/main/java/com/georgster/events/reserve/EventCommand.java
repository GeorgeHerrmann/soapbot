package com.georgster.events.reserve;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.ProfileType;
import com.georgster.util.CommandParser;
import com.georgster.util.GuildManager;
import com.georgster.util.ParseBuilder;
import com.georgster.util.SoapHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;

/**
 * Represents the command for managing events.
 */
public class EventCommand implements Command {
    private static final String PATTERN = "V|R 1|O";

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        CommandParser parser = new ParseBuilder(PATTERN).withIdentifiers("list", "unresere").withRules("X I").build();
        parser.parse(event.getMessage().getContent().toLowerCase());
        try { //Checks to see the command if valid
            String id = event.getGuild().block().getId().asString();
            if (parser.get(1).equals("list")) { //Shows the list of events
                ActionWriter.writeAction("Showing all events in a text channel");
                StringBuilder response = new StringBuilder();
                if (ProfileHandler.areEvents(id)) {
                    response.append("All events:\n");
                    for (ReserveEvent reserve : ProfileHandler.getEvents(id)) {
                        if (reserve.isTimeless()) {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved\n");
                        } else {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved at " + SoapHandler.convertToAmPm(reserve.getTime()) + "\n");
                        }
                    }
                    response.append("Type !events [NAME] for more information about a specific event");
                    manager.sendText(response.toString());
                } else {
                    manager.sendText("There are no events currently active");
                }
            } else if (parser.get(0).equals("unreserve")) { //Unreserves from an event
                ActionWriter.writeAction("Unreserving a user from an event");
                if (ProfileHandler.eventExists(id, parser.get(2))) {
                    for (ReserveEvent reserve: ProfileHandler.getEvents(id)) {
                        if (reserve.getIdentifier().equals(parser.get(1))) {
                            ProfileHandler.removeObject(id, reserve, ProfileType.EVENTS);
                            event.getMessage().getAuthor().ifPresent(user -> reserve.removeReservedUser(user.getTag())); //Gets the user's tag and removes them from the list
                            reserve.removeReserved();
                            if (reserve.getReserved() > 0) {
                                ProfileHandler.addObject(id, reserve, ProfileType.EVENTS);
                                manager.sendText("You have unreserved from event " + reserve.getIdentifier());
                            } else {
                                manager.sendText("There are no more people reserved to this event, this event has been removed");
                            }
                        }
                    }
                } else {
                    manager.sendText("Event does not exist");
                }
            } else { //Shows information about an event
                if (ProfileHandler.eventExists(id, parser.get(0))) {
                    ActionWriter.writeAction("Showing information about a specific event in a text channel");
                    ReserveEvent reserve = ProfileHandler.pullEvent(id, parser.get(0));
                    StringBuilder response = new StringBuilder();
                    response.append("Event: " + reserve.getIdentifier() + "\n");
                    response.append("\tReserved: " + reserve.getReserved() + "\n");
                    if (reserve.isUnlimited()) {
                        response.append("\t- This event has no limit on the amount of people that can reserve to it\n");
                    } else {
                        response.append("\t- Needed: " + reserve.getNumPeople() + "\n");
                    }
                    if (reserve.isTimeless()) {
                        response.append("\t- This event has no associated time\n");
                        response.append("This event will pop once the needed number of people have reserved to it");
                    } else {
                        response.append("\t- Time: " + SoapHandler.convertToAmPm(reserve.getTime()) + "\n");
                        response.append("This event will pop at " + SoapHandler.convertToAmPm(reserve.getTime()));
                    }
                    response.append("\nReserved users:\n");
                    for (String user : reserve.getReservedUsers()) {
                        Member member = manager.getMember(user);
                        response.append("\t- " + member.getUsername() + "\n");
                    }
                    manager.sendText(response.toString());
                } else {
                    manager.sendText("This event does not exist, type !events list for a list of all active events");
                }
            }
        } catch (IndexOutOfBoundsException e) {
            manager.sendText(help());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !events" +
        "\nUsage:" +
        "\n\t- !events list to list all events" +
        "\n\t- !events [NAME] for information about a specific event" +
        "\n\t- !events unreserve [NAME] to unreserve from an event" +
        "\n\t\t - An event will be removed if there are no more people reserved to it" +
        "\nType !help reserve for information about reserving to or creating an event";
    }
    
}
