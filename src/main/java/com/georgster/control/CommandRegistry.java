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
import com.georgster.music.components.AudioInterface;
import com.georgster.plinko.PlinkoCommand;
import com.georgster.test.TestCommand;
import com.georgster.util.EventTransformer;
import com.georgster.util.permissions.PermissionsCommand;

import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * The CommandRegistry is responsible for handling all of SOAP Bot's commands.
 */
public class CommandRegistry {
    
    private final ClientPipeline pipeline;
    private final List<Command> commands = new ArrayList<>();

    /**
     * Creates a Command Register for the associated SoapClient, 
     * registering all of SOAP Bot's pre-defined commands in it.
     * Any command that requires the SoapClient's objects (such as the AudioProvider, AudioPlayer, etc.)
     * can access it through the client parameter.
     * 
     * @param pipeline the ClientPipeline feeding this regitry's commands.
     */
    public CommandRegistry(ClientPipeline pipeline) {
        this.pipeline = pipeline;
        AudioInterface clientsInterface = pipeline.getAudioInterface();
        commands.add(new PongCommand());
        commands.add(new SoapCommand());
        commands.add(new HelpCommand(this));
        commands.add(new ReserveCommand(pipeline.getEventManager()));
        commands.add(new EventCommand(pipeline.getEventManager()));
        commands.add(new UnreserveCommand(pipeline.getEventManager()));
        commands.add(new MessageCommand());
        commands.add(new PlinkoCommand());
        commands.add(new PlayMusicCommand(clientsInterface.getProvider(), clientsInterface.getPlayerManager(), clientsInterface.getPlayer(), clientsInterface.getScheduler()));
        commands.add(new ShowQueueCommand(clientsInterface.getScheduler().getQueue()));
        commands.add(new SkipMusicCommand(clientsInterface.getPlayer(), clientsInterface.getScheduler()));
        commands.add(new PermissionsCommand(pipeline.getPermissionsManager()));
        commands.add(new TestCommand());
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
        commands.forEach(command -> {
            if (command.getAliases().contains(attemptedCommand)) {
                CommandExecutionEvent commandPipeline = new CommandExecutionEvent(transformer, client, pipeline.getDispatcher(), command);
                commandPipeline.executeCommand();
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

        commands.forEach(command -> {
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
        return commands;
    }
}
