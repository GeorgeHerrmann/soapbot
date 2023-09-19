package com.georgster.control.util;

import java.util.Collections;
import java.util.List;

import com.georgster.Command;
import com.georgster.ParseableCommand;
import com.georgster.control.CommandRegistry;
import com.georgster.control.SoapClient;
import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.AudioContext;
import com.georgster.util.DiscordEvent;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParsedArguments;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.UserInteractionHandler;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.IterableStringWizard;
import com.georgster.wizard.SwappingWizard;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;

/**
 * An Event fired upon the execution of a SOAPBot {@link Command}. This Event packages
 * the data from the {@link DiscordEvent}, as well as various objects a command might need.
 */
public class CommandExecutionEvent {
    
    private MultiLogger logger; // Used to log messages about the Event.
    private Command command; // The Command that is being executed
    private DiscordEvent discordEvent; // A wrapper used to extract data from the Discord Event that created this Event
    private ClientContext context; // The context of the SoapClient associated with the event.
    private GuildInteractionHandler handler;
    private UserInteractionHandler userHandler;
    private ParsedArguments parsedArguments;
    private CommandParser parser;

    /**
     * Creates a new CommandExecutionEvent.
     * 
     * @param event the Discord Event that triggered the firing of this Event
     * @param context the context for the SoapClient for the Guild the Event was fired in
     * @param command the Command that is being executed
     */
    public CommandExecutionEvent(DiscordEvent event, ClientContext context, Command command) {
        this.discordEvent = event;
        this.context = context;
        this.command = command;
        this.handler = new GuildInteractionHandler(discordEvent.getGuild()); // Used to manage the Command's interaction with the Guild
        handler.setActiveMessageChannel((GuildMessageChannel) discordEvent.getChannel());
        this.userHandler = new UserInteractionHandler(discordEvent.getUser());
        if (discordEvent.isChatInteraction()) { // If the Event was fired from a slash command
            handler.setActiveCommandInteraction((ChatInputInteractionEvent) discordEvent.getEvent());
        }

        this.logger = new MultiLogger(handler, command.getClass()); // Used to log messages about the Event

        if (command instanceof ParseableCommand) { // If the Command is parseable, it gets a CommandParser
            this.parser = ((ParseableCommand) command).getCommandParser();
        }
    }

    /**
     * Executes the {@link Command} in this Event on the calling thread.
     * <p>
     * Prior to execution, the Command is checked for permission, and if the Command
     * is a {@link ParseableCommand}, it is parsed for arguments.
     * <p>
     * Messages are logged to the event's {@link MultiLogger}, and Guild interactions
     * are handled by the event's {@link GuildInteractionHandler}.
     */
    public void executeCommand() {
        logger.append("**Executing: " + command.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);
        logger.append("- Platform: " + discordEvent.getPlatform().toString() + "\n", LogDestination.NONAPI);

        List<String> args = null;
        if (command instanceof ParseableCommand) {
            try {
                parsedArguments = parser.parse(discordEvent.getFormattedMessage());
                args = parsedArguments.getArguments();
                logger.append("- Arguments found: " + args.toString() + "\n",LogDestination.NONAPI);
                deferIfNecessary();
                executeIfPermission(args);
            } catch (Exception e) {
                logger.append("- Failed to execute, sending a help message\n", LogDestination.NONAPI);
                logger.append("Caught " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n", LogDestination.SYSTEM, LogDestination.FILE);
                for (StackTraceElement element : e.getStackTrace()) {
                    logger.append("\t" + element.toString() + "\n", LogDestination.FILE, LogDestination.SYSTEM);
                }

                InputWizard helpWizard = new IterableStringWizard(this, command.getClass().getSimpleName(), SoapUtility.splitHelpString(command.help()));
                Message msg = getGuildInteractionHandler().sendMessage(command.help(), command.getClass().getSimpleName());
                InputWizard switcher = new SwappingWizard(this, msg, helpWizard);
                switcher.begin();
            }
        } else {
            args = Collections.emptyList();
            executeIfPermission(args);
        }

        logger.sendAll();
    }

    /**
     * Defers the reply of the inner {@code Event} in this event's {@link DiscordEvent} if
     * the event can be deferred and this event's {@link Command} requests a deferral,
     * enabling this event's {@link GuildInteractionHandler} reply deferral mode.
     * 
     * @see {@link GuildInteractionHandler#deferReply(DeferrableInteractionEvent)}
     */
    private void deferIfNecessary() {
        if (discordEvent.getEvent() instanceof DeferrableInteractionEvent && command.shouldDefer()) {
            DeferrableInteractionEvent event = (DeferrableInteractionEvent) discordEvent.getEvent();
            handler.deferReply(event);
        }
    }

   /**
    * Executes the {@link Command} in this Event if the user has permission to do so.
    *
    * @param args the arguments for the Command
    */ 
    private void executeIfPermission(List<String> args) {
        if (hasPermission(args)) {
            command.execute(this);
        } else {
            handler.sendMessage("You need " + command.getRequiredPermission(args) + " to use this command.");
            logger.append("- User is missing permission: " + command.getRequiredPermission(args) + " to use this command.", LogDestination.NONAPI);
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
     * Returns the {@link DiscordEvent} that triggered the firing of this Event.
     * 
     * @return the {@link DiscordEvent} that triggered the firing of this Event.
     */
    public DiscordEvent getDiscordEvent() {
        return discordEvent;
    }

    /**
     * Returns the {@link PermissionsManager} for the SoapClient in this Event.
     * 
     * @return the {@link PermissionsManager} for the SoapClient in this Event.
     */
    public PermissionsManager getPermissionsManager() {
        return context.getPermissionsManager();
    }

    /**
     * Returns the {@link SoapEventManager} for the SoapClient in this Event.
     * 
     * @return the {@link SoapEventManager} for the SoapClient in this Event.
     */
    public SoapEventManager getEventManager() {
        return context.getEventManager();
    }

    /**
     * Returns the {@link UserProfileManager} for the SoapClient in this Event.
     * 
     * @return the {@link UserProfileManager} for the SoapClient in this Event.
     */
    public UserProfileManager getUserProfileManager() {
        return context.getUserProfileManager();
    }

    /**
     * Returns the {@link CommandRegistry} for the SoapClient in this Event.
     * 
     * @return the {@link CommandRegistry} for the SoapClient in this Event.
     */
    public CommandRegistry getCommandRegistry() {
        return context.getCommandRegistry();
    }

    /**
     * Returns the {@link AudioContext} for the SoapClient in this Event.
     * 
     * @return the {@link AudioContext} for the SoapClient in this Event.
     */
    public AudioContext getAudioContext() {
        return context.getAudioContext();
    }

    /**
     * Returns the {@link ClientContext} for the {@link SoapClient} this
     * event originated from.
     * 
     * @return The context of the originating SoapClient.
     */
    public ClientContext getClientContext() {
        return context;
    }

    /**
     * Returns the {@link EventDispatcher} that fired the Event in the Pipeline.
     * 
     * @return the {@link EventDispatcher} that fired the Event in the Pipeline.
     */
    public EventDispatcher getEventDispatcher() {
        return context.getDispatcher();
    }

    /**
     * Returns the {@link MultiLogger} that is used to log messages about the Event.
     * 
     * @return the {@link MultiLogger} that is used to log messages about the Event.
     */
    public MultiLogger getLogger() {
        return logger;
    }

    /**
     * Returns the {@link GuildInteractionHandler} that handles the Command's interaction with the Guild.
     * 
     * @return the {@link GuildInteractionHandler} that handles the Command's interaction with the Guild.
     */
    public GuildInteractionHandler getGuildInteractionHandler() {
        return handler;
    }

    /**
     * Returns the {@link UserInteractionHandler} that handles the Command's interaction with the {@link User} who executed the command.
     * 
     * @return The {@link UserInteractionHandler} that handles the Command's interaction with the {@link User} who executed the command.
     */
    public UserInteractionHandler getUserInteractionHandler() {
        return userHandler;
    }

    /**
     * Returns the {@link ParsedArguments} for this event.
     * 
     * @return The {@link ParsedArguments} for this event.
     */
    public ParsedArguments getParsedArguments() {
        return parsedArguments;
    }

    /**
     * Returns the {@link CommandParser} for the Command in this Event if
     * the Command is a {@link ParseableCommand}.
     * 
     * @return the {@link CommandParser} for the Command in this Event.
     */
    public CommandParser getCommandParser() {
        return parser;
    }

    /**
     * Factory method for creating a {@link SubcommandSystem} if the {@link Command}
     * in this event is a {@link ParseableCommand}, throwing an {@link UnsupportedOperationException} if not.
     * 
     * @return A {@link SubcommandSystem} equipped to make subcommands for the {@link ParseableCommand} in this event.
     * @throws UnsupportedOperationException If this event's {@link Command} is not a {@link ParseableCommand}.
     */
    public SubcommandSystem createSubcommandSystem() {
        if (command instanceof ParseableCommand) {
            return new SubcommandSystem(this);
        }
        throw new UnsupportedOperationException(command.getClass().getSimpleName() + " is not a ParseableCommand");
    }

}
