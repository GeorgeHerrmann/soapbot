package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.ClientPipeline;
import com.georgster.control.util.CommandPipeline;
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
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;
import com.georgster.util.permissions.PermissionsCommand;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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
    public void getAndExecute(Event event) {
        CommandPipeline commandPipeline = new CommandPipeline(event); //Will be used to transport and extract data from the event.
        String attemptedCommand = commandPipeline.getCommandName().toLowerCase();
        commands.forEach(command -> {
            if (command.getAliases().contains(attemptedCommand)) {
                GuildManager manager;
                if (command.needsDispatcher()) {
                    manager = new GuildManager(commandPipeline.getGuild(), pipeline.getDispatcher());
                } else {
                    manager = new GuildManager(commandPipeline.getGuild());
                }
                manager.setActiveChannel(commandPipeline.getChannel());
                manager.setActiveInteraction((ChatInputInteractionEvent)event);
                SoapUtility.runDaemon(() -> command.execute(commandPipeline, manager));
            }
        });
    }

    public void registerGlobalCommands() {
        long appId = pipeline.getRestClient().getApplicationId().block();
        long guildId = pipeline.getGuild().getId().asLong();

        commands.forEach(command -> {
            ApplicationCommandRequest cmd = command.getCommandApplicationInformation();

            if (cmd != null) {
                pipeline.getRestClient().getApplicationService().createGuildApplicationCommand(appId, guildId, cmd).block();
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
