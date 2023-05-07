package com.georgster.control;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.dm.MessageCommand;
import com.georgster.events.reserve.EventCommand;
import com.georgster.events.reserve.ReserveCommand;
import com.georgster.events.reserve.UnreserveCommand;
import com.georgster.misc.HelloWorldCommand;
import com.georgster.misc.HelpCommand;
import com.georgster.misc.PongCommand;
import com.georgster.misc.SoapCommand;
import com.georgster.music.PlayMusicCommand;
import com.georgster.music.ShowQueueCommand;
import com.georgster.music.SkipMusicCommand;
import com.georgster.plinko.PlinkoCommand;
import com.georgster.test.TestCommand;
import com.georgster.util.DiscordEvent;
import com.georgster.util.permissions.PermissionsCommand;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * The CommandRegistry is responsible for handling all of SOAP Bot's commands.
 */
public class CommandRegistry {
    
    private final ClientContext pipeline;
    private final List<Class<? extends Command>> commands;

    /**
     * Creates a Command Register for the associated SoapClient, 
     * registering all of SOAP Bot's pre-defined commands in it.
     * Any command that requires the SoapClient's objects (such as the AudioProvider, AudioPlayer, etc.)
     * can access it through the client parameter.
     * 
     * @param pipeline the ClientPipeline feeding this registry's commands.
     */
    public CommandRegistry(ClientContext pipeline) { //HelpCommand will be unique
        this.pipeline = pipeline;
        pipeline.setCommandRegistry(this);
        
        commands = new ArrayList<>(List.of(
            PongCommand.class,
            SoapCommand.class,
            HelpCommand.class,
            ReserveCommand.class,
            EventCommand.class,
            UnreserveCommand.class,
            MessageCommand.class,
            PlinkoCommand.class,
            PlayMusicCommand.class,
            ShowQueueCommand.class,
            SkipMusicCommand.class,
            PermissionsCommand.class,
            TestCommand.class,
            HelloWorldCommand.class
        ));
    }

    /**
     * Attempts to execute a command based on the contents of a Discord {@code Event}.
     * The event will be wrapped in a {@code DiscordEvent} object, therefore only events
     * supported by the DiscordEvent class will be able to be executed.
     * 
     * @param event the Event that prompted this call.
     */
    public void getAndExecute(Event event, SoapClient client) {
        DiscordEvent transformer = new DiscordEvent(event);
        String attemptedCommand = transformer.getCommandName().toLowerCase();
        getCommands().forEach(command -> {
            if (command.getAliases().contains(attemptedCommand)) {
                CommandExecutionEvent executionEvent = new CommandExecutionEvent(transformer, client, pipeline.getDispatcher(), command);
                ThreadPoolFactory.scheduleCommandTask(client.getSnowflake().asString(), executionEvent::executeCommand);
            }
        });
    }

    /**
     * Registers all of SOAP Bot's pre-defined commands as global commands to Discord
     * if the command has a valid ApplicationCommandRequest, which they will if
     * the command states it needs a new registration.
     */
    public void registerGlobalCommands() {
        long appId = pipeline.getRestClient().getApplicationId().block();

        getCommands().forEach(command -> {
            ApplicationCommandRequest cmd = command.getCommandApplicationInformation();

            if (cmd != null) { //If the command doesn't want to be registered to discord, it will return null.
                pipeline.getRestClient().getApplicationService().createGlobalApplicationCommand(appId, cmd).block();
            }
        });
    }

    /**
     * Instantiates and returns a list of all of SOAP Bot's pre-defined commands.
     * 
     * @return a list of all of SOAP Bot's pre-defined commands.
     */
    public List<Command> getCommands() {
        List<Command> commandList = new ArrayList<>();
        commands.forEach(command -> {
            try { // Commands that need access to high level objects (such as the AudioInterface, EventManager, etc.)
                commandList.add(command.getDeclaredConstructor(ClientContext.class).newInstance(pipeline));
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
}
