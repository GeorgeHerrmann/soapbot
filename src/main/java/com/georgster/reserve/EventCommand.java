package com.georgster.reserve;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.georgster.profile.ProfileHandler;
import com.georgster.util.SoapGeneralHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;

/**
 * Represents the command for managing events.
 */
public class EventCommand implements Command {

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event) {
        List<String> message = Arrays.asList(event.getMessage().getContent().split(" "));
        try { //Checks to see the command if valid
            String id = event.getGuild().block().getId().asString();
            if (message.get(1).equals("list")) { //Shows the list of events
                ActionWriter.writeAction("Showing all events in a text channel");
                StringBuilder response = new StringBuilder();
                if (ProfileHandler.areEvents(id)) {
                    response.append("All events:\n");
                    for (ReserveEvent reserve : ProfileHandler.getEvents(id)) {
                        if (reserve.isTimeless()) {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved\n");
                        } else {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved at " + SoapGeneralHandler.convertToAmPm(reserve.getTime()) + "\n");
                        }
                    }
                    response.append("Type !events [NAME] for more information about a specific event");
                    SoapGeneralHandler.sendTextMessageInChannel(response.toString(), event.getMessage().getChannel().block());
                } else {
                    SoapGeneralHandler.sendTextMessageInChannel("There are no events currently active", event.getMessage().getChannel().block());
                }
            } else if (message.get(1).equals("unreserve")) { //Unreserves from an event
                ActionWriter.writeAction("Unreserving a user from an event");
                if (ProfileHandler.eventExists(id, message.get(2))) {
                    for (ReserveEvent reserve: ProfileHandler.getEvents(id)) {
                        if (reserve.getIdentifier().equals(message.get(2))) {
                            ProfileHandler.removeEvent(id, reserve);
                            event.getMessage().getAuthor().ifPresent(user -> reserve.removeReservedUser(user.getTag())); //Gets the user's tag and removes them from the list
                            reserve.removeReserved();
                            if (reserve.getReserved() > 0) {
                                ProfileHandler.addEvent(id, reserve);
                                SoapGeneralHandler.sendTextMessageInChannel("You have unreserved from event " + reserve.getIdentifier(), event.getMessage().getChannel().block());
                            } else {
                                SoapGeneralHandler.sendTextMessageInChannel("There are no more people reserved to this event, this event has been removed", event.getMessage().getChannel().block());
                            }
                        }
                    }
                } else {
                    SoapGeneralHandler.sendTextMessageInChannel("Event does not exist", event.getMessage().getChannel().block());
                }
            } else { //Shows information about an event
                if (ProfileHandler.eventExists(id, message.get(1))) {
                    ActionWriter.writeAction("Showing information about a specific event in a text channel");
                    Gson parser = new Gson();
                    ReserveEvent reserve = ProfileHandler.pullEvent(id, message.get(1));
                    JsonObject reserveObject = parser.toJsonTree(reserve).getAsJsonObject();
                    StringBuilder response = new StringBuilder();
                    response.append("Event: " + reserveObject.get("identifier").getAsString() + "\n");
                    response.append("\tReserved: " + reserveObject.get("numReserved").getAsString() + "\n");
                    if (reserve.isUnlimited()) {
                        response.append("\t- This event has no limit on the amount of people that can reserve to it\n");
                    } else {
                        response.append("\t- Needed: " + reserveObject.get("numPeople").getAsString() + "\n");
                    }
                    if (reserve.isTimeless()) {
                        response.append("\t- This event has no associated time\n");
                        response.append("This event will pop once the needed number of people have reserved to it");
                    } else {
                        response.append("\t- Time: " + SoapGeneralHandler.convertToAmPm(reserveObject.get("time").getAsString()) + "\n");
                        response.append("This event will pop at " + SoapGeneralHandler.convertToAmPm(reserve.getTime()));
                    }
                    response.append("\nReserved users:\n");
                    for (String user : reserve.getReservedUsers()) {
                        Member member = SoapGeneralHandler.memberMatcher(user, event.getGuild().block().getMembers().buffer().blockFirst());
                        response.append("\t- " + member.getMention() + "\n"); //Will mention the user
                    }
                    SoapGeneralHandler.sendTextMessageInChannel(response.toString(), event.getMessage().getChannel().block());
                } else {
                    SoapGeneralHandler.sendTextMessageInChannel("This event does not exist, type !events list for a list of all active events", event.getMessage().getChannel().block());
                }
            }
        } catch (IndexOutOfBoundsException e) {
            SoapGeneralHandler.sendTextMessageInChannel(help(), event.getMessage().getChannel().block());
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
