package com.georgster.events.reserve;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.SoapEventManager;
import com.georgster.control.util.ClientPipeline;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.DiscordEvent;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the command for unreserving to a {@code ReserveEvent}.
 */
public class UnreserveCommand implements ParseableCommand {
    private static final String PATTERN = "V|R";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private SoapEventManager eventManager;

    /**
     * Creates a new {@code UnreserveCommand} with the given {@code ClientPipeline}.
     * 
     * @param pipeline The pipeline to get the {@code EventManager} from
     */
    public UnreserveCommand(ClientPipeline pipeline) {
        this.eventManager = pipeline.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        CommandParser parser = event.getCommandParser();
        DiscordEvent discordEvent = event.getDiscordEvent();

        if (eventManager.eventExists(parser.get(0), TYPE)) {
            ReserveEvent reserve = (ReserveEvent) eventManager.getEvent(parser.get(0));
            if (reserve.alreadyReserved(discordEvent.getAuthorAsMember().getTag())) {

                logger.append("\tRemoving " + discordEvent.getAuthorAsMember().getTag() + " from event " + reserve.getIdentifier(), LogDestination.NONAPI);
                reserve.removeReserved(discordEvent.getAuthorAsMember().getTag());
                if (reserve.getReserved() <= 0) {
                    eventManager.removeEvent(reserve);
                    logger.append("\n\tRemoving event " + reserve.getIdentifier() + " from the list of events", LogDestination.NONAPI);
                    handler.sendText("There are no more people reserved to this event, this event has been removed");
                } else {
                    eventManager.updateEvent(reserve);
                    handler.sendText("You have unreserved from " + reserve.getIdentifier());
                }
            } else {
                handler.sendText("You are not reserved to " + reserve.getIdentifier());
            }
        } else {
            logger.append("\tEvent does not exist", LogDestination.NONAPI);
            handler.sendText("Event " + parser.get(0) + " does not exist, type !events list to see all events");
        }

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
    @Override
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
        return "Aliases: " + getAliases().toString() +
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
    @Override
    public CommandParser getCommandParser() {
        return new CommandParser(PATTERN);
    }
}
