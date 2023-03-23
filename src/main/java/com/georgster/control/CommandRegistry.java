package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.ClientPipeline;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.dm.MessageCommand;
import com.georgster.events.reserve.EventCommand;
import com.georgster.events.reserve.ReserveCommand;
import com.georgster.events.reserve.UnreserveCommand;
import com.georgster.misc.HelpCommand;
import com.georgster.misc.PongCommand;
import com.georgster.misc.SoapCommand;
import com.georgster.music.PlayMusicCommand;
import com.georgster.music.ShowQueueCommand;
import com.georgster.music.SkipMusicCommand;
import com.georgster.plinko.PlinkoCommand;
import com.georgster.test.TestCommand;
import com.georgster.util.EventTransformer;
import com.georgster.util.SoapUtility;
import com.georgster.util.permissions.PermissionsCommand;

import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * The CommandRegistry is responsible for handling all of SOAP Bot's commands.
 */
public class CommandRegistry {
    
    private final ClientPipeline pipeline;
    private final List<Class<? extends Command>> commands;

    /**
     * Creates a Command Register for the associated SoapClient, 
     * registering all of SOAP Bot's pre-defined commands in it.
     * Any command that requires the SoapClient's objects (such as the AudioProvider, AudioPlayer, etc.)
     * can access it through the client parameter.
     * 
     * @param pipeline the ClientPipeline feeding this regitry's commands.
     */
    public CommandRegistry(ClientPipeline pipeline) { //HelpCommand will be unique
        this.pipeline = pipeline;
        
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
            TestCommand.class
        ));
    }

    /**
     * Attempts to execute a command based on the contents of a MessageCreateEvent.
     * If a valid command is not found, nothing happens.
     * 
     * @param event the MessageCreateEvent that prompted this call.
     */
    public void getAndExecute(Event event, SoapClient client) {
        EventTransformer transformer = new EventTransformer(event);
        String attemptedCommand = transformer.getCommandName().toLowerCase();
        getCommands().forEach(command -> {
            if (command.getAliases().contains(attemptedCommand)) {
                CommandExecutionEvent executionEvent = new CommandExecutionEvent(transformer, client, pipeline.getDispatcher(), command);
                SoapUtility.runDaemon(executionEvent::executeCommand);
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
     * Returns a list of all of SOAP Bot's pre-defined commands.
     * 
     * @return a list of all of SOAP Bot's pre-defined commands.
     */
    public List<Command> getCommands() {
        List<Command> commandList = new ArrayList<>();
        commands.forEach(command -> {
            try {
                if (command == HelpCommand.class) {
                    commandList.add(new HelpCommand(this));
                } else {
                    commandList.add(command.getDeclaredConstructor().newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return commandList;
    }
}
