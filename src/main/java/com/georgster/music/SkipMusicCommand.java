package com.georgster.music;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.TrackScheduler;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.permissions.PermissibleAction;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the bot's actions following the !skip command.
 */
public class SkipMusicCommand implements Command {

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    /**
     * Will skip the currently playing track or all tracks in the queue.
     * 
     * @param player
     * @param scheduler
     */
    public SkipMusicCommand(AudioPlayer player, TrackScheduler scheduler) {
        this.player = player;
        this.scheduler = scheduler;
    }

    /**
     * Will skip the currently playing track or all tracks in the queue.
     * 
     * @param event the event that triggered the command
     */
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<SkipMusicCommand> logger = new MultiLogger<>(manager, SkipMusicCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        if (scheduler.isActive()) {
            List<String> message = CommandParser.parseGeneric(pipeline.getFormattedMessage());
            logger.append("\tParsed: " + message.toString() + "\n", LogDestination.NONAPI);
            if (message.size() > 1 && message.get(1).equals("all")) { //Ensures we dont go OOB
                scheduler.clearQueue();
                manager.sendText("Skipping all tracks in the queue");
            } else {
                manager.sendText("Skipping the currently playing track");
            }
            player.stopTrack();
            logger.append("\tSkipping one or more tracks in a voice channel", LogDestination.API, LogDestination.NONAPI);
        } else {
            logger.append("\tNo tracks found in queue", LogDestination.NONAPI);
            manager.sendText("No tracks are currently playing");
        }

        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.SKIPMUSIC;
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return false;
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
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("skip the currently playing track or all tracks in the queue")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !play, !skip & !queue" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t!play [AUDIO LINK] to queue an audio track to play in the voice channel you are in" +
        "\n\t!skip to skip the current track" +
        "\n\t!skip all to skip all tracks in the queue" +
        "\n\t!queue to see all tracks in the queue";
    }
    
}
