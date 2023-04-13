package com.georgster.control.util;

import java.util.Collections;
import java.util.List;

import com.georgster.Command;
import com.georgster.ParseableCommand;
import com.georgster.control.CommandRegistry;
import com.georgster.control.PermissionsManager;
import com.georgster.control.SoapClient;
import com.georgster.control.SoapEventManager;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.AudioInterface;
import com.georgster.util.DiscordEvent;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

/**
 * An Event fired upon the execution of a SOAPBot {@code Command}. This Event packages
 * the data from the Discord Event, as well as various objects a command might need.
 */
public class CommandExecutionEvent {
    
    private MultiLogger logger; // Used to log messages about the Event.
    private Command command; // The Command that is being executed
    private DiscordEvent discordEvent; // A transformer used to extract data from the Discord Event that created this Event
    private SoapClient client; // The SoapClient for the Guild the Event was fired in
    private EventDispatcher dispatcher; // The EventDispatcher for the SoapClientManager that fired the Event in the Pipeline
    private GuildManager manager; // The GuildManager for the Guild the Event was fired in
    private CommandParser parser;

    /**
     * Creates a new CommandExecutionEvent.
     * 
     * @param event the Discord Event that triggered the firing of this Event
     * @param client the SoapClient for the Guild the Event was fired in
     * @param dispatcher the EventDispatcher for the SoapClientManager that fired the Event
     * @param command the Command that is being executed
     */
    public CommandExecutionEvent(DiscordEvent event, SoapClient client, EventDispatcher dispatcher, Command command) {
        this.discordEvent = event;
        this.client = client;
        this.dispatcher = dispatcher;
        this.command = command;
        this.manager = new GuildManager(discordEvent.getGuild()); // Used to manage the Command's interaction with the Guild
        manager.setActiveChannel(discordEvent.getChannel());
        if (discordEvent.isChatInteraction()) { // If the Event was fired from a slash command
            manager.setActiveInteraction((ChatInputInteractionEvent) discordEvent.getEvent());
        }

        this.logger = new MultiLogger(manager, command.getClass()); // Used to log messages about the Event

        if (command instanceof ParseableCommand) { // If the Command is parseable, it gets a CommandParser
            parser = ((ParseableCommand) command).getCommandParser();
        }
    }

    /**
     * Executes the {@code Command} in this Event on the calling thread.
     * Prior to execution, the Command is checked for permission, and if the Command
     * is a {@code ParseableCommand}, it is parsed for arguments.
     * Messages are logged to the event's {@code MultiLogger}, and Guild interactions
     * are handled by the event's {@code GuildManager}.
     */
    public void executeCommand() {
        logger.append("**Executing: " + command.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        List<String> args = null;
        if (command instanceof ParseableCommand) {
            try {
                args = parser.parse(discordEvent.getFormattedMessage());
                logger.append("\tArguments found: " + parser.getArguments().toString() + "\n",LogDestination.NONAPI);
                executeIfPermission(args);
            } catch (Exception e) {
                e.printStackTrace();
                logger.append("\tInvalid arguments, sending a help message\n", LogDestination.NONAPI);
                manager.sendText(command.help(), command.getClass().getSimpleName());
            }
        } else {
            args = Collections.emptyList();
            executeIfPermission(args);
        }

        logger.sendAll();
    }

   /**
    * Executes the {@code Command} in this Event if the user has permission to do so.
    *
    * @param args the arguments for the Command
    */ 
    private void executeIfPermission(List<String> args) {
        if (hasPermission(args)) {
            command.execute(this);
        } else {
            manager.sendText("You need " + command.getRequiredPermission(args) + " to use this command.");
            logger.append("User is missing permission: " + command.getRequiredPermission(args) + " to use this command.", LogDestination.NONAPI);
        }
    }
    
    /**
     * Checks if the user has permission to execute the Command in this Event.
     * 
     * @param args the arguments for the Command
     * @return true if the user has permission to execute the Command, false otherwise
     */
    private boolean hasPermission(List<String> args) {
        return getPermissionsManager().hasPermission(discordEvent.getAuthorAsMember(), command.getRequiredPermission(args));
    }

    /**
     * Returns the {@code DiscordEvent} that triggered the firing of this Event.
     * 
     * @return the {@code DiscordEvent} that triggered the firing of this Event.
     */
    public DiscordEvent getDiscordEvent() {
        return discordEvent;
    }

    /**
     * Returns the {@code PermissionsManager} for the SoapClient in this Event.
     * 
     * @return the {@code PermissionsManager} for the SoapClient in this Event.
     */
    public PermissionsManager getPermissionsManager() {
        return client.getPermissionsManager();
    }

    /**
     * Returns the {@code SoapEventManager} for the SoapClient in this Event.
     * 
     * @return the {@code SoapEventManager} for the SoapClient in this Event.
     */
    public SoapEventManager getEventManager() {
        return client.getEventManager();
    }

    /**
     * Returns the {@code CommandRegistry} for the SoapClient in this Event.
     * 
     * @return the {@code CommandRegistry} for the SoapClient in this Event.
     */
    public CommandRegistry getCommandRegistry() {
        return client.getRegistry();
    }

    /**
     * Returns the {@code AudioInterface} for the SoapClient in this Event.
     * 
     * @return the {@code AudioInterface} for the SoapClient in this Event.
     */
    public AudioInterface getAudioInterface() {
        return client.getAudioInterface();
    }

    /**
     * Returns the {@code EventDispatcher} that fired the Event in the Pipeline.
     * 
     * @return the {@code EventDispatcher} that fired the Event in the Pipeline.
     */
    public EventDispatcher getEventDispatcher() {
        return dispatcher;
    }

    /**
     * Returns the {@code MultiLogger} that is used to log messages about the Event.
     * 
     * @return the {@code MultiLogger} that is used to log messages about the Event.
     */
    public MultiLogger getLogger() {
        return logger;
    }

    /**
     * Returns the {@code GuildManager} that handles the Command's interaction with the Guild.
     * 
     * @return the {@code GuildManager} that handles the Command's interaction with the Guild.
     */
    public GuildManager getGuildManager() {
        return manager;
    }

    /**
     * Returns the {@code CommandParser} for the Command in this Event if
     * the Command is a {@code ParseableCommand}.
     * 
     * @return the {@code CommandParser} for the Command in this Event.
     */
    public CommandParser getCommandParser() {
        return parser;
    }

}
