package com.georgster.events.reserve;

import java.util.List;

import com.georgster.Command;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.ProfileType;
import com.georgster.util.CommandParser;
import com.georgster.util.GuildManager;
import com.georgster.util.ParseBuilder;
import com.georgster.util.SoapHandler;

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
        MultiLogger<EventCommand> logger = new MultiLogger<>(manager, EventCommand.class);
        logger.append("Executing: " + this.getClass().getSimpleName() + "\n",
        LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);

        CommandParser parser = new ParseBuilder(PATTERN).withIdentifiers("list", "unreserve").withRules("X I").build();
        ProfileHandler handler = manager.getHandler();
        try { //Checks to see the command if valid
            parser.parse(event.getMessage().getContent().toLowerCase());
            String tag = event.getMessage().getAuthorAsMember().block().getTag();
            if (parser.getMatchingRule("I").equals("list")) { //Shows the list of events
                logger.append("Showing all events in a text channel", LogDestination.API);
                logger.append("\t" + tag + " is requesting a list of events\n",
                LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);

                StringBuilder response = new StringBuilder();
                if (handler.areEvents()) {
                    logger.append("\tThere are events currently active\n",
                    LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
                    response.append("All events:\n");
                    for (ReserveEvent reserve : handler.getEvents()) {
                        if (reserve.isTimeless()) {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved\n");
                        } else {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved at " + SoapHandler.convertToAmPm(reserve.getTime()) + "\n");
                        }
                    }
                    response.append("Type !events [NAME] for more information about a specific event");
                    manager.sendText(response.toString());
                } else {
                    logger.append("\tThere are no events currently active\n",
                    LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
                    manager.sendText("There are no events currently active");
                }
            } else if (parser.getMatchingRule("I").equals("unreserve")) { //Unreserves from an event
                logger.append("Unreserving a user from an event", LogDestination.API);

                logger.append("\t" + tag + " is attempting to unreserve from an event\n",
                LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
                if (handler.eventExists(parser.get(0))) {
                    for (ReserveEvent reserve : handler.getEvents()) {
                        if (reserve.getIdentifier().equals(parser.get(0))) {

                            handler.removeObject(reserve, ProfileType.EVENTS);
                            event.getMessage().getAuthor().ifPresent(user -> reserve.removeReserved(user.getTag())); //Gets the user's tag and removes them from the list

                            logger.append("\tRemoving " + event.getMessage().getAuthorAsMember().block().getTag() + " from event " + reserve.getIdentifier(),
                            LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
                            if (reserve.getReserved() > 0) {
                                handler.addObject(reserve, ProfileType.EVENTS);
                                manager.sendText("You have unreserved from event " + reserve.getIdentifier());
                            } else {
                                logger.append("\tRemoving event " + reserve.getIdentifier() + " from the list of events",
                                LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
                                manager.sendText("There are no more people reserved to this event, this event has been removed");
                            }
                        }
                    }
                } else {
                    logger.append("\tCould not find event " + parser.get(0),
                    LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
                    manager.sendText("This event does not exist, type !events list for a list of all active events");
                }
            } else { //Shows information about an event
                logger.append("\t" + tag + " wants to see information about a specific event",
                LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
                if (handler.eventExists(parser.get(0))) {
                    logger.append("Showing information about a specific event in a text channel", LogDestination.API);
                    ReserveEvent reserve = handler.pullEvent(parser.get(0));

                    logger.append("Showing information about event: " + reserve.getIdentifier() + "\n",
                    LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);

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
            logger.append("The user did not provide valid arguments, showing the help message\n",
            LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);
            manager.sendText(help());
        }
        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("events", "event");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !events" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- !events list to list all events" +
        "\n\t- !events [NAME] for information about a specific event" +
        "\n\t- !events unreserve [NAME] to unreserve from an event" +
        "\n\t\t - An event will be removed if there are no more people reserved to it" +
        "\nType !help reserve for information about reserving to or creating an event";
    }
    
}
