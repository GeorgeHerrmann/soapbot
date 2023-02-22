package com.georgster.events.reserve;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.SoapEventManager;
import com.georgster.control.util.CommandPipeline;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the command for reserving to and creating events.
 */
public class ReserveCommand implements Command {
    private static final String PATTERN = "V|R 1|O 1|O";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private SoapEventManager eventManager;

    /**
     * Creates a new ReserveCommand with the associated {@code SoapEventManager}.
     * 
     * @param manager the event manager managing the events
     */
    public ReserveCommand(SoapEventManager manager) {
        eventManager = manager;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<ReserveCommand> logger = new MultiLogger<>(manager, ReserveCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        try {
            CommandParser parser = new ParseBuilder(PATTERN).withRules("X N|T N|T").build();
            List<String> message = parser.parse(pipeline.getFormattedMessage().toLowerCase());

            if (message.isEmpty()) {
                logger.append("\tNo arguments found, sending help message", LogDestination.NONAPI);
                logger.sendAll();
                String[] output = SoapUtility.splitFirst(help());
                manager.sendText(output[1], output[0]);
                return;
            }

            logger.append("\tArguments found: " + message.toString() + "\n", LogDestination.NONAPI);

            ReserveEvent reserve = assignCorrectEvent(manager, parser);

            if (eventManager.eventExists(reserve.getIdentifier(), TYPE)) {
                if (!reserve.isFull()) {
                    pipeline.getAuthorOptionally().ifPresent(user -> {
                        if (reserve.alreadyReserved(user.getTag())) {
                            manager.sendText("You have already reserved for this event, type !event " + reserve.getIdentifier() + " unreserve to unreserve");
                        } else {
                            logger.append("Reserving a user to event " + reserve.getIdentifier() + "\n", LogDestination.API, LogDestination.NONAPI);
                            reserve.addReserved(user.getTag());
                            eventManager.updateEvent(reserve);
                            manager.sendText(user.getUsername() + " has reserved to event " + reserve.getIdentifier(),
                            reserve.getReserved() + "/" + reserve.getNumPeople() + " spots filled");
                        }
                    });
                } else {
                    manager.sendText("Event " + reserve.getIdentifier() + " is full");
                }
            } else {
                logger.append("\tCreating a new event " + reserve.getIdentifier() + "\n", LogDestination.NONAPI, LogDestination.API);
                pipeline.getAuthorOptionally().ifPresent(user -> reserve.addReserved(user.getTag()));
                eventManager.addEvent(reserve);
                String messageString = "";
                if (reserve.isTimeless()) {
                    logger.append("\tThis event is timeless\n", LogDestination.NONAPI);
                    messageString = "Event " + reserve.getIdentifier() + " scheduled with " + reserve.getAvailable() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else if (reserve.isUnlimited()) {
                    logger.append("\tThis event is unlimited\n", LogDestination.NONAPI);
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapUtility.convertToAmPm(reserve.getTime()) + "! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else {
                    logger.append("\tThis event is neither timeless nor unlimited\n", LogDestination.NONAPI);
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapUtility.convertToAmPm(reserve.getTime()) + " with " + reserve.getAvailable() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                }
                manager.sendText(messageString, reserve.getIdentifier() + " event created");
            }
        } catch (IllegalArgumentException e) {
            logger.append("\tSending an error message", LogDestination.NONAPI);
            manager.sendText(e.getMessage());
        }
        logger.sendAll();

    }

    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return false;
    }


    /**
     * Assigns the correct ReserveEvents based on the user's command message.
     * 
     * @param manager The GuildManager that is managing the guild
     * @param parser The CommandParser that is parsing the user's command message
     * @return The ReserveEvent that the user wants to create or reserve to
     * @throws IllegalArgumentException If the user's command message is in the wrong format
     */
    private ReserveEvent assignCorrectEvent(GuildManager manager, CommandParser parser) throws IllegalArgumentException {

        List<String> message = parser.getArguments();
        String channelName = ((TextChannel) manager.getActiveChannel()).getName();

        if (message.size() == 1) { //Means the user is trying to reserve to an event that already exists
            if (eventManager.eventExists(message.get(0), TYPE)) { //If the event exists, we get the event and return it
                return (ReserveEvent) eventManager.getEvent(message.get(0));
            } else {
                throw new IllegalArgumentException("This event doesn't exist. Type !help reserve to see how to make a new event.");
            }
        } else if (message.size() == 2 && !eventManager.eventExists(message.get(0), TYPE)) { //Creating an event that is either Unlimited or Timeless
            try {
                if (Integer.parseInt(message.get(1)) < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                return new ReserveEvent(message.get(0), Integer.parseInt(message.get(1)), channelName); //If the user only inputs a number, the event is Timeless
            } catch (NumberFormatException e) { //If it is not a number, it is a time
                try {
                    return new ReserveEvent(message.get(0), SoapUtility.timeConverter(message.get(1)), channelName); //Creates an Unlimited event
                } catch (IllegalArgumentException e2) { //If both of these fail, the user's command message is in the wrong format
                    throw new IllegalArgumentException(e2.getMessage());
                }
            }
        } else if (message.size() == 3 && !eventManager.eventExists(message.get(0), TYPE)) { //Creates a new event that has a number of slots and a time
            try { //If the event doesn't already exist, we can attempt to create a new one
                if (Integer.parseInt(parser.getMatchingRule("N")) < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                //Should be in the format !reserve [EVENTNAME] [NUMPEOPLE] [TIME]
                List<String> temp = parser.getMatchingRules("N");
                int numPeople = Integer.parseInt(temp.get(temp.size() - 1));
                temp = parser.getMatchingRules("T");
                String time = temp.get(temp.size() - 1);
                return new ReserveEvent(message.get(0), numPeople, SoapUtility.timeConverter(time), channelName);
            } catch (NumberFormatException e) { //If the user's command is in the wrong format
                throw new IllegalArgumentException("Incorrect reserve event format, type !help reserve to see how to make a new event.");
            } catch (IllegalArgumentException e) { //TimeConverter throws an IllegalArgumentException if the time is in the wrong format
                throw new IllegalArgumentException(e.getMessage());
            } catch (IndexOutOfBoundsException e) { //If the name of the event has some issue the parser can't handle
                throw new IllegalArgumentException("There is an issue with the name of the event. The event name cannot have a number unless it is attached to a word." + 
                "\n\t- Example: !reserve csgo 1 v 1 5 5:00pm is incorrect, but !reserve csgo 1v1 5 5:00pm is correct.");
            }
        }
        throw new IllegalArgumentException("Incorrect Reserve Event Format or this event already exists, simply type !reserve [EVENTNAME] if you want to reserve to an event that exists.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.size() > 1) {
            return PermissibleAction.CREATEEVENT;
        } else if (args.size() == 1) {
            return PermissibleAction.RESERVEEVENT;
        } else {
            return PermissibleAction.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("reserve", "res", "r");
    }


    /**
     * {@inheritDoc}
     */
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Reserve to or create a new reserve event")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("event")
                        .description("The name of the event")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("playercount")
                        .description("The number of people needed to reserve to the event")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("time")
                        .description("The time the event will pop")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !reserve" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- '!reserve [EVENTNAME] [PLAYERCOUNT] [TIME]' to create a new event with for a specific time with a certain number of people" +
        "\n\t\t - This event will pop when the specified time hits" +
        "\n\t- '!reserve [EVENTNAME] [PLAYERCOUNT]' to create a new event with a certain number of people" +
        "\n\t\t - This event will pop when the specified number of people have reserved" +
        "\n\t- '!reserve [EVENTNAME] [TIME]' to create a new event for a specific time" +
        "\n\t\t - This event will pop when the specified time hits" +
        "\n\t- '!reserve [EVENTNAME]' to reserve to an event that already exists" +
        "\n\t - !events for information about the event command";
    }
}
