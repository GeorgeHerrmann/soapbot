package com.georgster.music;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.TrackScheduler;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.CommandParser;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the bot's actions following the !skip command.
 */
public class SkipMusicCommand implements Command {
    private AudioPlayer player;
    private TrackScheduler scheduler;

    /**
     * Creates a new SkipMusicCommand from the given {@code ClientContext}.
     * 
     * @param context The {@code ClientContext} to get audio components from
     */
    public SkipMusicCommand(ClientContext context) {
        this.player = context.getAudioContext().getPlayer();
        this.scheduler = context.getAudioContext().getScheduler();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();

        if (scheduler.isActive()) {
            List<String> message = CommandParser.parseGeneric(event.getDiscordEvent().getFormattedMessage());
            if (message.size() > 1 && message.get(1).equals("all")) { //Ensures we dont go OOB
                scheduler.clearQueue();
                handler.sendText("Skipping all tracks in the queue");
            } else {
                handler.sendText("Skipping the currently playing track");
            }
            player.stopTrack();
            logger.append("- Skipping one or more tracks in a voice channel", LogDestination.API, LogDestination.NONAPI);
        } else {
            logger.append("- No tracks found in queue", LogDestination.NONAPI);
            handler.sendText("No tracks are currently playing");
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("skip");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("skip the currently playing track or all tracks in the queue")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !play [AUDIO LINK] to queue an audio track to play in the voice channel you are in" +
        "\n- !skip to skip the current track" +
        "\n- !skip all to skip all tracks in the queue" +
        "\n- !queue to see all tracks in the queue";
    }
    
}
