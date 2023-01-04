package com.georgster.music;

import java.util.concurrent.LinkedBlockingQueue;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.georgster.util.GuildManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Represents the bot's actions following the !queue command.
 */
public class ShowQueueCommand implements Command {

    private LinkedBlockingQueue<AudioTrack> queue;

    /**
     * Will show the current queue of audio tracks.
     * 
     * @param queue the quque held in the TrackScheduler
     */
    public ShowQueueCommand(LinkedBlockingQueue<AudioTrack> queue) {
        this.queue = queue;
    }

    /**
     * Will show the current queue of audio tracks.
     * 
     * @param execute the event that triggered the command
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        StringBuilder response = new StringBuilder("Current Queue:\n");
        int x = 1;
        for (AudioTrack i : queue.toArray(new AudioTrack[queue.size()])) {
            response.append("\t" + x + ") " + i.getInfo().title + "\n");
            if (response.length() >= 1800) {
                manager.sendText(response.toString());
                response = new StringBuilder();
            }
            x++;
        }
        ActionWriter.writeAction("Showing the current audio track queue");
        manager.sendText(response.toString());
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !play, !skip & !queue" +
        "\nUsage:" +
        "\n\t!play [AUDIO LINK] to queue an audio track to play in the voice channel you are in" +
        "\n\t!skip to skip the current track" +
        "\n\t!skip all to skip all tracks in the queue" +
        "\n\t!queue to see all tracks in the queue";
    }
    
}
