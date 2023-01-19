package com.georgster.events.reserve;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.SoapEventManager;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Represents the command for managing events.
 */
public class EventCommand implements Command {
    private static final String PATTERN = "V|R 1|O";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private SoapEventManager eventManager;

    /**
     * Creates a new EventCommand with the associated {@code SoapEventManager}.
     * 
     * @param manager the event manager managing the events
     */
    public EventCommand(SoapEventManager manager) {
        eventManager = manager;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        MultiLogger<EventCommand> logger = new MultiLogger<>(manager, EventCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n",
        LogDestination.DISCORD, LogDestination.SYSTEM, LogDestination.FILE);

        CommandParser parser = new ParseBuilder(PATTERN).withIdentifiers("list", "unreserve").withRules("X I").build();
        try { //Checks to see the command if valid
            parser.parse(event.getMessage().getContent().toLowerCase());
            logger.append("\tParsed: " + parser.getArguments().toString() + "\n",LogDestination.NONAPI);

            if (parser.getMatchingRule("I").equals("list")) { //Shows the list of events
                logger.append("Showing all events in a text channel", LogDestination.API);

                StringBuilder response = new StringBuilder();
                if (eventManager.areEvents(TYPE)) {
                    logger.append("\tShowing the user a list of events\n", LogDestination.NONAPI);
                    response.append("All events:\n");
                    List<SoapEvent> events = eventManager.getEvents(TYPE);
                    for (int i = 0; i < events.size(); i++) {
                        /* The EventManager will ensure we get events of the correct type, so casting is safe */
                        ReserveEvent reserve = (ReserveEvent) events.get(i);
                        if (reserve.isTimeless()) {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved\n");
                        } else {
                            response.append("\t" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved at " + SoapUtility.convertToAmPm(reserve.getTime()) + "\n");
                        }
                    }
                    response.append("Type !events [NAME] for more information about a specific event");
                    manager.sendText(response.toString());
                } else {
                    logger.append("\tThere are no events currently active\n", LogDestination.NONAPI);
                    manager.sendText("There are no events currently active");
                }
            } else if (parser.getMatchingRule("I").equals("unreserve")) { //Unreserves from an event
                logger.append("Unreserving a user from an event", LogDestination.API);
                if (eventManager.eventExists(parser.get(0), TYPE)) {
                    List<SoapEvent> events = eventManager.getEvents(SoapEventType.RESERVE);
                    for (int i = 0; i < events.size(); i++) {
                        ReserveEvent reserve = (ReserveEvent) events.get(i);
                        if (reserve.getIdentifier().equals(parser.get(0))) {
                            event.getMessage().getAuthor().ifPresent(user -> reserve.removeReserved(user.getTag())); //Gets the user's tag and removes them from the list

                            logger.append("\tRemoving " + event.getMessage().getAuthorAsMember().block().getTag() + " from event " + reserve.getIdentifier(),
                            LogDestination.NONAPI);
                            if (reserve.getReserved() > 0) {
                                eventManager.updateEvent(reserve);
                                manager.sendText("You have unreserved from event " + reserve.getIdentifier());
                            } else {
                                eventManager.removeEvent(reserve);
                                logger.append("\n\tRemoving event " + reserve.getIdentifier() + " from the list of events", LogDestination.NONAPI);
                                manager.sendText("There are no more people reserved to this event, this event has been removed");
                            }
                        }
                    }
                } else {
                    logger.append("\tCould not find event " + parser.get(0), LogDestination.NONAPI);
                    manager.sendText("This event does not exist, type !events list for a list of all active events");
                }
            } else { //Shows information about an event
                if (eventManager.eventExists(parser.get(0), TYPE)) {
                    logger.append("Showing information about a specific event in a text channel", LogDestination.API);
                    ReserveEvent reserve = (ReserveEvent) eventManager.getEvent(parser.get(0));

                    logger.append("Showing information about event: " + reserve.getIdentifier() + "\n", LogDestination.NONAPI);

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
                        response.append("\t- Time: " + SoapUtility.convertToAmPm(reserve.getTime()) + "\n");
                        response.append("This event will pop at " + SoapUtility.convertToAmPm(reserve.getTime()));
                    }
                    response.append("\nReserved users:\n");
                    for (String user : reserve.getReservedUsers()) {
                        response.append("\t- " + manager.getMember(user).getUsername() + "\n");
                    }
                    manager.sendText(response.toString());
                } else {
                    manager.sendText("This event does not exist, type !events list for a list of all active events");
                }
            }
        } catch (IndexOutOfBoundsException e) {
            logger.append("The user did not provide valid arguments, showing the help message\n", LogDestination.NONAPI);
            manager.sendText(help());
        }
        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasWizard() {
        return false;
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
        "\n\t- !events [NAME] unreserve to unreserve from an event" +
        "\n\t\t - An event will be removed if there are no more people reserved to it" +
        "\nType !help reserve for information about reserving to or creating an event";
    }
    
}
