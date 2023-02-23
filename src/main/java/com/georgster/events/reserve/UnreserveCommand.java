package com.georgster.events.reserve;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.SoapEventManager;
import com.georgster.control.util.CommandPipeline;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the command for unreserving to a {@code ReserveEvent}.
 */
public class UnreserveCommand implements Command {
    private static final String PATTERN = "V|R";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private SoapEventManager eventManager;

    /**
     * Creates a new UnreserveCommand with the associated {@code SoapEventManager}.
     * 
     * @param eventManager the event manager managing the events
     */
    public UnreserveCommand(SoapEventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<UnreserveCommand> logger = new MultiLogger<>(manager, UnreserveCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        CommandParser parser = new CommandParser(PATTERN);

        try {
            parser.parse(pipeline.getFormattedMessage());

            if (pipeline.getPermissionsManager().hasPermissionSendError(manager, logger, getRequiredPermission(parser.getArguments()), pipeline.getAuthorAsMember())) {
                logger.append("\tArguments found: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

                if (eventManager.eventExists(parser.get(0), TYPE)) {
                    ReserveEvent reserve = (ReserveEvent) eventManager.getEvent(parser.get(0));
                    if (reserve.alreadyReserved(pipeline.getAuthorAsMember().getTag())) {

                        logger.append("\tRemoving " + pipeline.getAuthorAsMember().getTag() + " from event " + reserve.getIdentifier(), LogDestination.NONAPI);
                        reserve.removeReserved(pipeline.getAuthorAsMember().getTag());
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
            }
        } catch (Exception e) {
            logger.append("\tSending an error message", LogDestination.NONAPI);
            manager.sendText(help());
        }
        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.size() == 1) {
            return PermissibleAction.RESERVEEVENT;
        } else {
            return PermissibleAction.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("unreserve", "ur", "unres");
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Unreserve from an event you have reserved to")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("event")
                        .description("The name of the event")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return false;
    }
}
