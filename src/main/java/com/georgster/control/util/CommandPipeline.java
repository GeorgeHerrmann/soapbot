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
import com.georgster.util.EventTransformer;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

/**
 * This class is pure chaos at the moment but will be really cool once its done.
 */
public class CommandPipeline {
    
    private MultiLogger logger; // A MultiLogger for the Command that is being executed in this Pipeline
    private Command command; // The Command that is being executed in this Pipeline
    private EventTransformer transformer; // The EventTransformer for the Event that triggered the creation of this Pipeline
    private SoapClient client; // The SoapClient for the Guild the Event was fired in
    private EventDispatcher dispatcher; // The EventDispatcher for the SoapClientManager that fired the Event in the Pipeline
    private GuildManager manager; // The GuildManager for the Guild the Event was fired in
    private CommandParser parser;

    /**
     * Creates a new CommandPipeline with the given {@code Event}.
     * 
     * @param event the event
     */
    public CommandPipeline(EventTransformer transformer, SoapClient client, EventDispatcher dispatcher, Command command) {
        this.transformer = transformer;
        this.client = client;
        this.dispatcher = dispatcher;
        this.command = command;
        this.manager = new GuildManager(transformer.getGuild());
        manager.setActiveChannel(transformer.getChannel());
        if (transformer.isChatInteraction()) {
            manager.setActiveInteraction((ChatInputInteractionEvent) transformer.getEvent());
        }

        this.logger = new MultiLogger(manager, command.getClass());

        if (command instanceof ParseableCommand) {
            parser = ((ParseableCommand) command).getCommandParser();
        }
    }

    public void executeCommand() {
        logger.append("**Executing: " + command.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        List<String> args = null;
        if (command instanceof ParseableCommand) {
            try {
                args = parser.parse(transformer.getFormattedMessage());
                logger.append("\tArguments found: " + args.toString() + "\n",LogDestination.NONAPI);
            } catch (IllegalArgumentException e) {
                manager.sendText(command.help());
            }
        } else {
            args = Collections.emptyList();
        }

        if (hasPermission(args)) {
            command.execute(this, manager);
        } else {
            manager.sendText("You need " + command.getRequiredPermission(args) + " to use this command.");
            logger.append("User is missing permission: " + command.getRequiredPermission(args) + " to use this command.", LogDestination.NONAPI);
        }
    }
    
    public boolean hasPermission(List<String> args) {
        return getPermissionsManager().hasPermission(transformer.getAuthorAsMember(), command.getRequiredPermission(args));
    }

    public EventTransformer getEventTransformer() {
        return transformer;
    }

    /**
     * Returns the {@code PermissionsManager} for the SoapClient in this Pipeline.
     * 
     * @return the {@code PermissionsManager} for the SoapClient in this Pipeline.
     */
    public PermissionsManager getPermissionsManager() {
        return client.getPermissionsManager();
    }

    public SoapEventManager getEventManager() {
        return client.getEventManager();
    }

    public CommandRegistry getCommandRegistry() {
        return client.getRegistry();
    }

    public AudioInterface getAudioInterface() {
        return client.getAudioInterface();
    }

    public EventDispatcher getEventDispatcher() {
        return dispatcher;
    }

}
