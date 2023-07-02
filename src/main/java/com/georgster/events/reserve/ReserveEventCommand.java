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
import com.georgster.util.commands.wizard.InputWizard;
import com.georgster.util.commands.wizard.ReserveEventWizard;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the command for managing reserve events.
 */
public class ReserveEventCommand implements ParseableCommand {
    private static final String PATTERN = "V|R 1|O";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private SoapEventManager eventManager;

    /**
     * Creates a new {@code ReserveEventCommand} with the given {@code ClientContext}.
     * 
     * @param context The context to get the {@code EventManager} from
     */
    public ReserveEventCommand(ClientContext context) {
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
            logger.append("Showing all reserve events in a text channel", LogDestination.API);

            StringBuilder response = new StringBuilder();
            if (eventManager.hasAny(TYPE)) {
                logger.append("- Showing the user a list of reserve events\n", LogDestination.NONAPI);
                response.append("All reserve events:\n");
                List<SoapEvent> events = eventManager.getAll(TYPE);
                for (int i = 0; i < events.size(); i++) {
                    /* The EventManager will ensure we get events of the correct type, so casting is safe */
                    ReserveEvent reserve = (ReserveEvent) events.get(i);
                    if (reserve.isTimeless()) {
                        response.append("- " + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved\n");
                    } else {
                        response.append("- " + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved at " + SoapUtility.convertToAmPm(reserve.getTime()) + "\n");
                    }
                }
                response.append("Type !events [NAME] for more information about a specific reserve event");
                String[] output = SoapUtility.splitFirst(response.toString());
                handler.sendText(output[1], output[0]);
            } else {
                logger.append("- There are no reserve events currently active\n", LogDestination.NONAPI);
                handler.sendText("There are no reserve events currently active");
            }
        } else if (parser.getMatchingRule("I").equals("mention") || parser.getMatchingRule("I").equals("ping")) {
            if (eventManager.exists(parser.get(0), TYPE)) {
                logger.append("Mentioning all users that have reserved to an event", LogDestination.API);
                ReserveEvent reserve = (ReserveEvent) eventManager.get(parser.get(0));

                logger.append("- Mentioning all users that have reserved to event: " + reserve.getIdentifier() + "\n", LogDestination.NONAPI);

                StringBuilder response = new StringBuilder();
                reserve.getReservedUsers().forEach(user -> response.append(handler.getMember(user).getMention() + " "));
                handler.sendPlainText(response.toString()); //If sendText is used, the embed will prevent users from being mentioned
            } else {
                handler.sendText("This reserve event does not exist, type !events list for a list of all active events");
            }
        } else if (parser.getMatchingRule("I").equals("manage")) {
            if (eventManager.hasAny(TYPE)) {
                InputWizard wizard = new ReserveEventWizard(event);
                logger.append("- Beginning the reserve event wizard\n", LogDestination.NONAPI);
                wizard.begin();
            } else {
                handler.sendText("There are no Reserve Events to manage.", "Reserve Event Wizard");
                logger.append("- There are no reserve events to manage.", LogDestination.NONAPI);
            }
        } else { //Shows information about an event
            if (eventManager.exists(parser.get(0), TYPE)) {
                logger.append("Showing information about a specific reserve event in a text channel", LogDestination.API);
                ReserveEvent reserve = (ReserveEvent) eventManager.get(parser.get(0));

                logger.append("Showing information about reserve event: " + reserve.getIdentifier() + "\n", LogDestination.NONAPI);

                String[] output = SoapUtility.splitFirst(reserve.toString());
                handler.sendText(output[1], output[0]);
            } else {
                handler.sendText("This reserve event does not exist, type !events list for a list of all active events");
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
        } else if (args.contains("manage")) {
            return PermissibleAction.MANAGEEVENTS;
        } else {
            return PermissibleAction.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder(PATTERN).withIdentifiers("list", "mention", "ping", "manage").withRules("X I").build();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("events", "event", "reserveevents", "reserveevent", "re", "resevent", "rese", "revent");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Shows information about reserve events")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("event")
                        .description("The reserve event to show information about")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("option")
                        .description("Select what to do with the reserve event")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("mention")
                                .value("mention")
                                .build())
                        .required(false)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !events list to list all reserve events" +
        "\n- !events [NAME] for information about a specific reserve event" +
        "\n- !events [NAME] mention to mention all users that have reserved to an event" +
        "\n- !unreserve [NAME] to unreserve from an event" +
        "\n\t - An event will be removed if there are no more people reserved to it" +
        "\n*Type !help reserve for information about reserving to or creating an event*";
    }
    
}
