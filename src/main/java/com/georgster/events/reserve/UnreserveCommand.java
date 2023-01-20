package com.georgster.events.reserve;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.SoapEventManager;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;

import discord4j.core.event.domain.message.MessageCreateEvent;
//!unreserve [event name]
public class UnreserveCommand implements Command {
    private static final String PATTERN = "V|R";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private SoapEventManager eventManager;

    public UnreserveCommand(SoapEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void execute(MessageCreateEvent event, GuildManager manager) {
        MultiLogger<UnreserveCommand> logger = new MultiLogger<>(manager, UnreserveCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        CommandParser parser = new CommandParser(PATTERN);

        try {
            parser.parse(event.getMessage().getContent());

            logger.append("\tArguments found: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

            if (eventManager.eventExists(parser.get(0), TYPE)) {
                ReserveEvent reserve = (ReserveEvent) eventManager.getEvent(parser.get(0));
                if (reserve.alreadyReserved(event.getMessage().getAuthorAsMember().block().getTag())) {

                    logger.append("\tRemoving " + event.getMessage().getAuthorAsMember().block().getTag() + " from event " + reserve.getIdentifier(),
                            LogDestination.NONAPI);
                    reserve.removeReserved(event.getMessage().getAuthorAsMember().block().getTag());
                    if (reserve.getReserved() <= 0) {
                        eventManager.removeEvent(reserve);
                        logger.append("\n\tRemoving event " + reserve.getIdentifier() + " from the list of events", LogDestination.NONAPI);
                        manager.sendText("There are no more people reserved to this event, this event has been removed");
                    } else {
                        eventManager.updateEvent(reserve);
                        manager.sendText("You have unreserved from " + reserve.getIdentifier());
                    }
                } else {
                    manager.sendText("You are not reserved to " + reserve.getIdentifier());
                }
            } else {
                logger.append("\tEvent does not exist", LogDestination.NONAPI);
                manager.sendText("Event " + parser.get(0) + " does not exist, type !events list to see all events");
            }
        } catch (Exception e) {
            logger.append("\tSending an error message", LogDestination.NONAPI);
            manager.sendText(help());
        }
        logger.sendAll();
    }

    public List<String> getAliases() {
        return List.of("unreserve", "ur", "unres");
    }

    public String help() {
        return "Command: !events & !unreserve" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- !events list to list all events" +
        "\n\t- !events [NAME] for information about a specific event" +
        "\n\t- !unreserve [NAME] to unreserve from an event" +
        "\n\t\t - An event will be removed if there are no more people reserved to it" +
        "\nType !help reserve for information about reserving to or creating an event";
    }

    public boolean hasWizard() {
        return false;
    }
}
