package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.Command;
import com.georgster.dm.MessageCommand;
import com.georgster.events.reserve.EventCommand;
import com.georgster.events.reserve.ReserveCommand;
import com.georgster.misc.HelpCommand;
import com.georgster.misc.PongCommand;
import com.georgster.misc.SoapCommand;
import com.georgster.music.PlayMusicCommand;
import com.georgster.music.ShowQueueCommand;
import com.georgster.music.SkipMusicCommand;
import com.georgster.plinko.PlinkoCommand;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * The CommandRegistry is responsible for handling all of SOAP Bot's commands.
 */
public class CommandRegistry {
    
    private final List<Command> commands = new ArrayList<>();

    /**
     * Creates a Command Register for the associated SoapClient, 
     * registering all of SOAP Bot's pre-defined commands in it.
     * Any command that requires the SoapClient's objects (such as the AudioProvider, AudioPlayer, etc.)
     * can access it through the client parameter.
     * 
     * @param client The SoapClient that is running SOAP Bot.
     */
    public CommandRegistry(SoapClient client) {

        commands.add(new PongCommand());
        commands.add(new SoapCommand());
        commands.add(new HelpCommand(this));
        commands.add(new ReserveCommand());
        commands.add(new EventCommand());
        commands.add(new MessageCommand());
        commands.add(new PlinkoCommand());
        commands.add(new PlayMusicCommand(client.getProvider(), client.getPlayerManager(), client.getPlayer(), client.getScheduler()));
        commands.add(new ShowQueueCommand(client.getScheduler().getQueue()));
        commands.add(new SkipMusicCommand(client.getPlayer(), client.getScheduler()));
    }

    /**
     * Attempts to execute a command based on the contents of a MessageCreateEvent.
     * If a valid command is not found, nothing happens.
     * 
     * @param event the MessageCreateEvent that prompted this call.
     */
    public void getAndExecute(MessageCreateEvent event) {
        String attemptedCommand = event.getMessage().getContent().split(" ")[0].substring(1).toLowerCase();
        for (Command command : commands) {
            if (command.getAliases().contains(attemptedCommand)) {
                GuildManager manager = new GuildManager(event.getGuild().block());
                manager.setActiveChannel((event.getMessage().getChannel().block()));
                SoapUtility.runDaemon(() -> command.execute(event, manager));
            }
        }
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