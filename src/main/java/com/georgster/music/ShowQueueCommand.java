package com.georgster.music;

import java.util.concurrent.LinkedBlockingQueue;

import com.georgster.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ShowQueueCommand implements Command {

    private LinkedBlockingQueue<AudioTrack> queue;

    public ShowQueueCommand(LinkedBlockingQueue<AudioTrack> queue) {
        this.queue = queue;
    }

    public void execute(MessageCreateEvent event) {
        StringBuilder response = new StringBuilder("Current Queue:\n");
        int x = 1;
        for (AudioTrack i : queue.toArray(new AudioTrack[queue.size()])) {
            response.append("\t" + x + ") " + i.getInfo().title + "\n");
            if (response.length() >= 1800) {
                event.getMessage().getChannel().block().createMessage(response.toString()).block();
                response = new StringBuilder();
            }
            x++;
        }
        event.getMessage().getChannel().block().createMessage(response.toString()).block();
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
