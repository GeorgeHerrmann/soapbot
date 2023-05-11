package com.georgster.events.reserve;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the command for managing events.
 */
public class EventCommand implements ParseableCommand {
    private static final String PATTERN = "V|R 1|O";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private SoapEventManager eventManager;

    /**
     * Creates a new {@code EventCommand} with the given {@code ClientContext}.
     * 
     * @param context The context to get the {@code EventManager} from
     */
    public EventCommand(ClientContext context) {
        this.eventManager = context.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        CommandParser parser = event.getCommandParser();

        if (parser.getMatchingRule("I").equals("list")) { //Shows the list of events
            logger.append("Showing all events in a text channel", LogDestination.API);

            StringBuilder response = new StringBuilder();
            if (eventManager.hasAny(TYPE)) {
                logger.append("\tShowing the user a list of events\n", LogDestination.NONAPI);
                response.append("All events:\n");
                List<SoapEvent> events = eventManager.getAll(TYPE);
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
                String[] output = SoapUtility.splitFirst(response.toString());
                handler.sendText(output[1], output[0]);
            } else {
                logger.append("\tThere are no events currently active\n", LogDestination.NONAPI);
                handler.sendText("There are no events currently active");
            }
        } else if (parser.getMatchingRule("I").equals("mention") || parser.getMatchingRule("I").equals("ping")) {
            if (eventManager.exists(parser.get(0), TYPE)) {
                logger.append("Mentioning all users that have reserved to an event", LogDestination.API);
                ReserveEvent reserve = (ReserveEvent) eventManager.get(parser.get(0));

                logger.append("\tMentioning all users that have reserved to event: " + reserve.getIdentifier() + "\n", LogDestination.NONAPI);

                StringBuilder response = new StringBuilder();
                reserve.getReservedUsers().forEach(user -> response.append(handler.getMember(user).getMention() + " "));
                handler.sendPlainText(response.toString()); //If sendText is used, the embed will prevent users from being mentioned
            } else {
                handler.sendText("This event does not exist, type !events list for a list of all active events");
            }
        } else { //Shows information about an event
            if (eventManager.exists(parser.get(0), TYPE)) {
                logger.append("Showing information about a specific event in a text channel", LogDestination.API);
                ReserveEvent reserve = (ReserveEvent) eventManager.get(parser.get(0));

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
                response.append("\nScheduled for: " + SoapUtility.formatDate(reserve.getDate()));
                response.append("\nReserved users:\n");
                reserve.getReservedUsers().forEach(user -> response.append("\t- " + handler.getMember(user).getUsername() + "\n"));
                String[] output = SoapUtility.splitFirst(response.toString());
                handler.sendText(output[1], output[0]);
            } else {
                handler.sendText("This event does not exist, type !events list for a list of all active events");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.contains("mention") || args.contains("ping")) {
            return PermissibleAction.MENTIONEVENT;
        } else {
            return PermissibleAction.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder(PATTERN).withIdentifiers("list", "mention", "ping").withRules("X I").build();
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
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Shows information about events")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("event")
                        .description("The event to show information about")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- !events list to list all events" +
        "\n\t- !events [NAME] for information about a specific event" +
        "\n\t- !events [NAME] mention to mention all users that have reserved to an event" +
        "\n\t- !unreserve [NAME] to unreserve from an event" +
        "\n\t\t - An event will be removed if there are no more people reserved to it" +
        "\nType !help reserve for information about reserving to or creating an event";
    }
    
}
