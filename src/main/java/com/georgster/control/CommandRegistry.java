package com.georgster.control;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.dm.MessageCommand;
import com.georgster.economy.BankCommand;
import com.georgster.events.reserve.ReserveEventCommand;
import com.georgster.events.poll.PollEventCommand;
import com.georgster.events.reserve.ReserveCommand;
import com.georgster.events.reserve.UnreserveCommand;
import com.georgster.game.blackjack.BlackjackCommand;
import com.georgster.game.plinko.PlinkoCommand;
import com.georgster.gpt.GPTCommand;
import com.georgster.misc.EchoCommand;
import com.georgster.misc.HelloWorldCommand;
import com.georgster.misc.HelpCommand;
import com.georgster.misc.PongCommand;
import com.georgster.misc.SoapCommand;
import com.georgster.misc.TheFinalShapeCommand;
import com.georgster.music.PlayMusicCommand;
import com.georgster.music.ShowQueueCommand;
import com.georgster.music.SkipMusicCommand;
import com.georgster.permissions.PermissionsCommand;
import com.georgster.test.TestCommand;
import com.georgster.util.DiscordEvent;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * The CommandRegistry is responsible for handling all of SOAP Bot's {@link Command Commands}.
 */
public class CommandRegistry {
    
    private final ClientContext context;
    private final List<Class<? extends Command>> commands;

    /**
     * Creates a Command Register for the associated SoapClient, 
     * registering all of SOAP Bot's pre-defined commands in it.
     * 
     * @param context the {@link ClientContext} feeding this registry's commands.
     */
    public CommandRegistry(ClientContext context) { //HelpCommand will be unique
        this.context = context;
        context.setCommandRegistry(this);
        
        commands = new ArrayList<>(List.of(
            TestCommand.class,
            PongCommand.class,
            SoapCommand.class,
            HelpCommand.class,
            ReserveCommand.class,
            ReserveEventCommand.class,
            UnreserveCommand.class,
            MessageCommand.class,
            PlinkoCommand.class,
            PlayMusicCommand.class,
            ShowQueueCommand.class,
            SkipMusicCommand.class,
            PermissionsCommand.class,
            HelloWorldCommand.class,
            TheFinalShapeCommand.class,
            PollEventCommand.class,
            GPTCommand.class,
            BankCommand.class,
            EchoCommand.class,
            BlackjackCommand.class
        ));
    }

    /**
     * Attempts to execute a command based on the contents of a Discord {@link Event}.
     * <p>
     * The event will be wrapped in a {@link DiscordEvent}, which can only handle {@code ChatInputInteractionEvents}
     * and/or {@code MessageCreateEvents}.
     * <p>
     * This method is also responsible for creating and firing the {@link CommandExecutionEvent} when the {@link Command}
     * is found and executed.
     * 
     * @param event The {@link Event} that prompted this call.
     */
    public void getAndExecute(Event event) {
        DiscordEvent transformer = new DiscordEvent(event);
        String attemptedCommand = transformer.getCommandName().toLowerCase();
        getCommands().forEach(command -> {
            if (command.getAliases().contains(attemptedCommand)) {
                CommandExecutionEvent executionEvent = new CommandExecutionEvent(transformer, context, command);
                ThreadPoolFactory.scheduleCommandTask(context.getGuild().getId().asString(), executionEvent::executeCommand);
            }
        });
    }

    /**
     * Registers all of SOAP Bot's pre-defined commands as global commands to Discord
     * if the command has a valid {@link ApplicationCommandRequest}, and updates
     * all pre-existing definitions if they are out of date.
     */
    protected void registerGlobalCommands() {
        ThreadPoolFactory.scheduleGlobalDiscordApiTask(() -> { //These tasks are scheduled to run on the global Discord API thread.

            long appId = context.getRestClient().getApplicationId().block();
            
            List<ApplicationCommandRequest> newCommandRequests = new ArrayList<>(); // SOAPBot's records of the commands

            getCommands().forEach(command -> { // Obtains the ApplicationCommandRequest for each command
                ApplicationCommandRequest request = command.getCommandApplicationInformation();
                if (request != null) { // Commands that don't want to be registered will return null
                    newCommandRequests.add(request);
                }
            });

            context.getRestClient().getApplicationService().getGlobalApplicationCommands(appId).collectList().block().forEach(globalCommand -> 
                newCommandRequests.stream().filter(examiner -> !globalCommand.name().equals(examiner.name()))
                                    .forEach(newRequest -> context.getRestClient().getApplicationService().createGlobalApplicationCommand(appId, newRequest).block())
            );

            context.getRestClient().getApplicationService().getGuildApplicationCommands(appId, context.getGuild().getId().asLong()).collectList().block().forEach(guildCommand -> 
                context.getRestClient().getApplicationService().deleteGuildApplicationCommand(appId, context.getGuild().getId().asLong(), guildCommand.id().asLong()).subscribe()
            );

            // Overwrites the global commands with the new ones
            context.getRestClient().getApplicationService().bulkOverwriteGlobalApplicationCommand(appId, newCommandRequests).subscribe();
        });
    }

    /**
     * Factory method which instantiates and returns a list of all of SOAP Bot's pre-defined {@link Command Commands} for a SoapClient.
     * 
     * @return A list of all of SOAP Bot's pre-defined {@link Command Commands} for a SoapClient.
     */
    public List<Command> getCommands() {
        List<Command> commandList = new ArrayList<>();
        commands.forEach(command -> {
            try { // Commands that need access to high level objects (such as the AudioInterface, EventManager, etc.)
                commandList.add(command.getDeclaredConstructor(ClientContext.class).newInstance(context));
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                try { // Commands that don't need access to high level objects
                    commandList.add(command.getDeclaredConstructor().newInstance());
                } catch (Exception e1) { //If Exception is still thrown, the command is invalid.
                    e1.printStackTrace();
                }
            }
        });
        return commandList;
    }

    /**
     * Factory method which returns the first {@link Command} with an alias matching the provided {@code alias}, ignoring case.
     * 
     * @param alias The Command alias to search for.
     * @return The first {@link Command} with the matching alias.
     * @throws IllegalArgumentException If no {@link Command} in this registry has a matching alias.
     */
    public Command getCommand(String alias) throws IllegalArgumentException {
        alias = alias.toLowerCase();
        for (Command command : getCommands()) {
            if (command.getAliases().contains(alias)) {
                return command;
            }
        }
        throw new IllegalArgumentException("No Command found with alias: " + alias);
    }
}
