package com.georgster.music;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class SkipMusicCommand implements Command {

    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    public SkipMusicCommand(AudioPlayer player, TrackScheduler scheduler) {
        this.player = player;
        this.scheduler = scheduler;
    }

    public void execute(MessageCreateEvent event) {
        if (scheduler.isActive()) {
            String message = event.getMessage().getContent();
            if (message.contains("all")) {
                scheduler.clearQueue();
                event.getMessage().getChannel().block().createMessage("Skipping all tracks in the queue").block();
            } else {
                event.getMessage().getChannel().block().createMessage("Skipping the currently playing track").block();
            }
            player.stopTrack();
            ActionWriter.writeAction("Skipping the currently playing track in a voice channel");
        }
    }

    public String help() {
        return "Command: !play, !skip & !queue" +
        "\nUsage:" +
        "\n\t!play [AUDIO LINK] to queue an audio track to play in the voice channel you are in" +
        "\n\t!skip to skip the current track" +
        "\n\t!skip all to skip all tracks in the queue" +
        "\n\t!queue to see all tracks in the queue";
    }
    
}
