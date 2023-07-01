package com.georgster.events.reserve;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.ClientContext;
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

    private SoapEventManager eventManager;

    /**
     * Creates a new {@code UnreserveCommand} with the given {@code ClientContext}.
     * 
     * @param context The context to get the {@code EventManager} from
     */
    public UnreserveCommand(ClientContext context) {
        this.eventManager = context.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        CommandParser parser = event.getCommandParser();
        DiscordEvent discordEvent = event.getDiscordEvent();

        if (eventManager.exists(parser.get(0), TYPE)) {
            ReserveEvent reserve = (ReserveEvent) eventManager.get(parser.get(0));
            if (reserve.alreadyReserved(discordEvent.getAuthorAsMember().getTag())) {

                logger.append("- Removing " + discordEvent.getAuthorAsMember().getTag() + " from event " + reserve.getIdentifier(), LogDestination.NONAPI);
                reserve.removeReserved(discordEvent.getAuthorAsMember().getTag());
                if (reserve.getReserved() <= 0) {
                    eventManager.remove(reserve);
                    logger.append("\n- Removing event " + reserve.getIdentifier() + " from the list of events", LogDestination.NONAPI);
                    handler.sendText("There are no more people reserved to this event, this event has been removed");
                } else {
                    eventManager.update(reserve);
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
        "\n- !events list to list all events" +
        "\n- !events [NAME] for information about a specific event" +
        "\n- !unreserve [NAME] to unreserve from an event" +
        "\n\t - An event will be removed if there are no more people reserved to it" +
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
