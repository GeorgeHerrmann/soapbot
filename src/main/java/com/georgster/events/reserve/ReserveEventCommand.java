package com.georgster.events.reserve;

import java.util.List;
import java.util.function.Supplier;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.settings.TimezoneOption;
import com.georgster.settings.UserSettings;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.IterableStringWizard;
import com.georgster.wizard.ReserveEventWizard;
import com.georgster.wizard.SinglyAttachmentWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the command for managing reserve events.
 */
public class ReserveEventCommand implements ParseableCommand {
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private final SoapEventManager eventManager;

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
        SubcommandSystem subcommands = event.createSubcommandSystem();
        UserSettings settings = event.getClientContext().getUserSettingsManager().get(event.getDiscordEvent().getUser().getId().asString());

        subcommands.on(() -> {
            logger.append("Showing all reserve events in a text channel", LogDestination.API);

            if (eventManager.hasAny(TYPE)) {
                logger.append("- Showing the user a list of reserve events\n", LogDestination.NONAPI);
                List<String> output = SoapUtility.splitAtEvery(getEventListString(event), 10);
                SoapUtility.appendSuffixToList(output, event.getDiscordEvent().getAuthorAsMember().getMention() + " has reserved to the **bolded events**\n"
                        + "Type *!events [NAME]* for more information about a specific reserve event");
                new IterableStringWizard(event, handler.getGuild().getName() + " Reserve Events", output).begin();
            } else {
                logger.append("- There are no reserve events currently active\n", LogDestination.NONAPI);
                handler.sendMessage("There are no reserve events currently active", MessageFormatting.ERROR);
            }
        });

        subcommands.on(p -> { //Shows the list of events
            logger.append("Showing all reserve events in a text channel", LogDestination.API);

            if (eventManager.hasAny(TYPE)) {
                logger.append("- Showing the user a list of reserve events\n", LogDestination.NONAPI);
                List<String> output = SoapUtility.splitAtEvery(getEventListString(event), 10);
                SoapUtility.appendSuffixToList(output, event.getDiscordEvent().getAuthorAsMember().getMention() + " has reserved to the **bolded events**\n"
                        + "Type *!events [NAME]* for more information about a specific reserve event");
                new IterableStringWizard(event, handler.getGuild().getName() + " Reserve Events", output).begin();
            } else {
                logger.append("- There are no reserve events currently active\n", LogDestination.NONAPI);
                handler.sendMessage("There are no reserve events currently active", MessageFormatting.ERROR);
            }
        }, "list");
        
        subcommands.on(parser -> { 
            if (eventManager.exists(parser.get(0), TYPE)) {
                logger.append("Mentioning all users that have reserved to an event", LogDestination.API);
                ReserveEvent reserve = (ReserveEvent) eventManager.get(parser.get(0));

                logger.append("- Mentioning all users that have reserved to event: " + reserve.getIdentifier() + "\n", LogDestination.NONAPI);

                StringBuilder response = new StringBuilder();
                reserve.getReservedUsers().forEach(user -> response.append(handler.getMemberById(user).getMention() + " "));
                handler.sendPlainMessage(response.toString()); //If sendMessage is used, the embed will prevent users from being mentioned
            } else {
                handler.sendMessage("This reserve event does not exist, type !events list for a list of all active events", MessageFormatting.ERROR);
            }
        }, "mention", "ping");

        subcommands.on(p -> {
            if (eventManager.hasAny(TYPE)) {
                InputWizard wizard = new ReserveEventWizard(event);
                logger.append("- Beginning the reserve event wizard\n", LogDestination.NONAPI);
                wizard.begin();
            } else {
                handler.sendMessage("There are no Reserve Events to manage.", "Reserve Event Wizard", MessageFormatting.ERROR);
                logger.append("- There are no reserve events to manage.", LogDestination.NONAPI);
            }
        }, "manage");

        subcommands.onIndexLast(eventName -> {
            if (eventManager.exists(eventName, TYPE)) {
                logger.append("Showing information about a specific reserve event in a text channel", LogDestination.API);

                ReserveEvent reserve = (ReserveEvent) eventManager.get(eventName);

                logger.append("- Showing information about reserve event: " + reserve.getIdentifier() + "\n", LogDestination.NONAPI);


                Supplier<String> title = () -> "Event: " + eventName;

                Supplier<String> body = () -> {
                    StringBuilder response = new StringBuilder();
                    response.append("- Reserved: " + reserve.getReserved() + "\n");
                    if (reserve.isUnlimited()) {
                        response.append("\t- This event has no limit on the amount of people that can reserve to it\n");
                    } else {
                        response.append("- Needed: " + reserve.getNumPeople() + "\n");
                    }
                    if (reserve.isTimeless()) {
                        response.append("- This event has no associated time\n");
                        response.append("\t- This event will pop once the needed number of people have reserved to it");
                    } else {
                        response.append("- Time: " + reserve.getFormattedTime(settings) + " " + TimezoneOption.getSettingDisplay(settings.getTimezoneSetting()) +  "\n");
                        response.append("\t- This event will pop at " + reserve.getFormattedTime(settings) + " " + TimezoneOption.getSettingDisplay(settings.getTimezoneSetting()));
                        response.append("\nScheduled for: " + reserve.getFormattedDate(settings));
                    }
                    response.append("\nReserved users:\n");
                    reserve.getReservedUsers().forEach(user -> response.append("- " + handler.getMemberById(user).getMention() + "\n"));
                    return response.toString();
                };

                SinglyAttachmentWizard wizard = new SinglyAttachmentWizard(event, title, body); // will attach the "manage" button
                wizard.addOption("manage", () -> {
                    InputWizard wizard2 = new ReserveEventWizard(event); // brings up the manage wizard on click
                    wizard2.getInputListener().setCurrentMessage(wizard.getInputListener().getCurrentMessage()); // sets the current message to the same message as the first wizard
                    wizard2.begin("manageEvent", reserve);
                });
                wizard.begin();
            } else {
                handler.sendMessage("This reserve event does not exist, type !events list for a list of all active events", MessageFormatting.ERROR);
            }
        }, 0);
    }

    /**
     * Returns a String-based representation output of all {@link ReserveEvent ReserveEvents} when this command executes.
     * 
     * @param event The event that prompted command execution.
     * @return A String-based representation output of all {@link ReserveEvent ReserveEvents}.
     */
    private String getEventListString(CommandExecutionEvent event) {
        StringBuilder response = new StringBuilder();
        List<SoapEvent> events = eventManager.getAll(TYPE);
        UserSettings settings = event.getClientContext().getUserSettingsManager().get(event.getDiscordEvent().getUser().getId().asString());
        for (int i = 0; i < events.size(); i++) {
            /* The EventManager will ensure we get events of the correct type, so casting is safe */
            ReserveEvent reserve = (ReserveEvent) events.get(i);
            String user = event.getDiscordEvent().getAuthorAsMember().getId().asString();
            if (reserve.isTimeless()) {
                if (reserve.alreadyReserved(user)) {
                    response.append("- **" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved**\n");
                } else {
                    response.append("- " + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved\n");
                }
            } else {
                if (reserve.alreadyReserved(user)) {
                    response.append("- **" + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved" +
                                "\n\tReserved for " + reserve.getFormattedTime(settings) + " " + TimezoneOption.getSettingDisplay(settings.getTimezoneSetting()) + " on " + reserve.getFormattedDate(settings) + "**\n");
                } else {
                    response.append("- " + reserve.getIdentifier() + " - " + reserve.getReserved() + "/" + reserve.getNumPeople() + " people reserved" +
                                "\n\tReserved for " + reserve.getFormattedTime(settings) + " " + TimezoneOption.getSettingDisplay(settings.getTimezoneSetting()) + " on " + reserve.getFormattedDate(settings) + "\n");
                }
            }
        }

        return response.toString();
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
        return new ParseBuilder("VO", "1O").withIdentifiers("list", "mention", "ping", "manage").withRules("X", "I").build();
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
