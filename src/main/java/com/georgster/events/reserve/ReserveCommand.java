package com.georgster.events.reserve;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.DiscordEvent;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.ParsedArguments;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the command for reserving to and creating events.
 */
public class ReserveCommand implements ParseableCommand {
    private static final SoapEventType TYPE = SoapEventType.RESERVE;
    
    private SoapEventManager eventManager;

    /**
     * Creates a new {@code ReserveCommand} with the given {@code ClientContext}.
     * 
     * @param context The context to get the {@code EventManager} from
     */
    public ReserveCommand(ClientContext context) {
        this.eventManager = context.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        DiscordEvent discordEvent = event.getDiscordEvent();
        try {
            ReserveEvent reserve = assignCorrectEvent(event);
        
            if (eventManager.exists(reserve.getIdentifier(), TYPE)) {
                if (!reserve.isFull()) {
                    discordEvent.getAuthorOptionally().ifPresent(user -> {
                        if (reserve.alreadyReserved(user.getId().asString())) {
                            handler.sendMessage("You have already reserved for this event, type !unreserve " + reserve.getIdentifier() + " to unreserve");
                        } else {
                            logger.append("- Reserving a user to event " + reserve.getIdentifier() + "\n", LogDestination.API, LogDestination.NONAPI);
                            reserve.addReserved(user.getId().asString());
                            eventManager.update(reserve);
                            handler.sendMessage(user.getUsername() + " has reserved to event " + reserve.getIdentifier(),
                            reserve.getReserved() + "/" + reserve.getNumPeople() + " spots filled");
                        }
                    });
                } else {
                    handler.sendMessage("Event " + reserve.getIdentifier() + " is full");
                }
            } else {
                logger.append("- Creating a new event " + reserve.getIdentifier() + "\n", LogDestination.NONAPI, LogDestination.API);
                discordEvent.getAuthorOptionally().ifPresent(user -> reserve.addReserved(user.getId().asString()));
                eventManager.add(reserve);
                String messageString = "";
                if (reserve.isTimeless()) {
                    logger.append("- This event is timeless\n", LogDestination.NONAPI);
                    messageString = "Event " + reserve.getIdentifier() + " scheduled with " + reserve.getAvailable() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else if (reserve.isUnlimited()) {
                    logger.append("- This event is unlimited\n", LogDestination.NONAPI);
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapUtility.convertToAmPm(reserve.getTime()) + "! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                } else {
                    logger.append("- This event is neither timeless nor unlimited\n", LogDestination.NONAPI);
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + SoapUtility.convertToAmPm(reserve.getTime()) + " with " + reserve.getAvailable() + " spots available! Type !reserve " + reserve.getIdentifier() + " to reserve a spot!";
                }
                messageString += "\n- Scheduled for: " + SoapUtility.formatDate(reserve.getDate());
                handler.sendMessage(messageString, reserve.getIdentifier() + " event created");
            }
        } catch (IllegalArgumentException e) { // assignCorrectEvent will send custom error messages, all other exceptions are handled by the CommandExecutionEvent
            handler.sendMessage(e.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder("VR", "1O", "1O", "VO").withRules("X", "N", "T", "D").build();
    }


    /**
     * Assigns the correct ReserveEvents based on the user's command message.
     * 
     * @param event The CommandExecutionEvent that prompted the creation of this ReserveEvent.
     * @return The ReserveEvent that the user wants to create or reserve to.
     * @throws IllegalArgumentException If the user's command message is in the wrong format.
     */
    private ReserveEvent assignCorrectEvent(CommandExecutionEvent event) throws IllegalArgumentException {
        GuildInteractionHandler manager = event.getGuildInteractionHandler();
        ParsedArguments parser = event.getParsedArguments(); // Uses custom parsing algorithm to handle everything with the event, subcommandsystem not used here

        String channelName = ((TextChannel) manager.getActiveMessageChannel()).getName();

        if (parser.size() == 1) { //Means the user is trying to reserve to an event that already exists
            if (eventManager.exists(parser.get(0), TYPE)) { //If the event exists, we get the event and return it
                return (ReserveEvent) eventManager.get(parser.get(0));
            } else {
                throw new IllegalArgumentException("This event doesn't exist. Type !help reserve to see how to make a new event.");
            }
        } else if (parser.size() == 2 && !eventManager.exists(parser.get(0), TYPE)) { //Creating an event that is either Unlimited or Timeless
            try {
                if (Integer.parseInt(parser.get(1)) < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                return new ReserveEvent(parser.get(0), Integer.parseInt(parser.get(1)), channelName); //If the user only inputs a number, the event is Timeless
            } catch (NumberFormatException e) { //If it is not a number, it is a time
                try {
                    return new ReserveEvent(parser.get(0), SoapUtility.timeConverter(parser.get(1)), channelName); //Creates an Unlimited event
                } catch (IllegalArgumentException e2) { //If both of these fail, the user's command message is in the wrong format
                    throw new IllegalArgumentException(e2.getMessage());
                }
            }
        } else if (parser.size() == 3 && !eventManager.exists(parser.get(0), TYPE)) { //Creates a new event that has a number of slots and a time
            try { //If the event doesn't already exist, we can attempt to create a new one
                int numPeople = Integer.parseInt(parser.get(1));
                if (numPeople < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                //Should be in the format !reserve [EVENTNAME] [NUMPEOPLE] [TIME]
                String time = parser.get(2);
                return new ReserveEvent(parser.get(0), numPeople, time, channelName);
            } catch (Exception ex) {
                try {
                    //Should be in the format !reserve [EVENTNAME] [TIME] [DATE]
                    String time = parser.get(1);
                    String date = parser.get(2);
                    return new ReserveEvent(parser.get(0), time, channelName, date);
                } catch (NumberFormatException e) { //If the user's command is in the wrong format
                    throw new IllegalArgumentException("Incorrect reserve event format, type !help reserve to see how to make a new event.");
                } catch (IllegalArgumentException e) { //TimeConverter throws an IllegalArgumentException if the time is in the wrong format
                    throw new IllegalArgumentException(e.getMessage());
                } catch (IndexOutOfBoundsException e) { //If the name of the event has some issue the parser can't handle
                    throw new IllegalArgumentException("There is an issue with the name of the event. The event name cannot have a number unless it is attached to a word." + 
                    "\n\t- Example: !reserve csgo 1 v 1 5 5:00pm is incorrect, but !reserve csgo 1v1 5 5:00pm is correct.");
                }
            }
        } else if (parser.size() == 4 && !eventManager.exists(parser.get(0), TYPE)) {
            //Should be in the format !reserve [EVENTNAME] [NUMPEOPLE] [TIME] [DATE]
            try {
                int numPeople = Integer.parseInt(parser.get(1));
                if (numPeople < 1) throw new IllegalArgumentException("Number of people must be greater than 0");
                String time = parser.get(2);
                String date = parser.get(3);
                return new ReserveEvent(parser.get(0), numPeople, time, channelName, date);
            } catch (NumberFormatException e) { //If the user's command is in the wrong format
                throw new IllegalArgumentException("Incorrect reserve event format, type !help reserve to see how to make a new event.");
            } catch (IllegalArgumentException e) { //TimeConverter throws an IllegalArgumentException if the time is in the wrong format
                throw new IllegalArgumentException(e.getMessage());
            } catch (IndexOutOfBoundsException e) { //If the name of the event has some issue the parser can't handle
                throw new IllegalArgumentException("There is an issue with the name of the event. The event name cannot have a number unless it is attached to a word." + 
                "\n- Example: !reserve csgo 1 v 1 5 5:00pm is incorrect, but !reserve csgo 1v1 5 5:00pm is correct.");
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
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
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
                .addOption(ApplicationCommandOptionData.builder()
                        .name("date")
                        .description("The date the event will pop")
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
        "\n- '!reserve [EVENTNAME] [PLAYERCOUNT] [TIME] [DATE]' to create a new event with for a specific time and date with a certain number of people" +
        "\n\t - This event will pop when the specified time hits" +
        "\n- '!reserve [EVENTNAME] [PLAYERCOUNT] [TIME]' to create a new event with for a specific time with a certain number of people" +
        "\n\t - This event will pop when the specified time hits" +
        "\n- '!reserve [EVENTNAME] [PLAYERCOUNT]' to create a new event with a certain number of people" +
        "\n\t - This event will pop when the specified number of people have reserved" +
        "\n- '!reserve [EVENTNAME] [TIME] [DATE]' to create a new event for a specific time and date" +
        "\n\t - This event will pop when the specified time hits" +
        "\n- '!reserve [EVENTNAME] [TIME]' to create a new event for a specific time" +
        "\n\t - This event will pop when the specified time hits" +
        "\n- '!reserve [EVENTNAME]' to reserve to an event that already exists" +
        "\n - !events for information about the event command" +
        "\n*Note: If including a date, it should generally be last as to not interfere with the event name*";
    }
}
