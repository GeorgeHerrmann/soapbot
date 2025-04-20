package com.georgster.music;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.georgster.Command;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.SoapUtility;
import com.georgster.wizard.IterableStringWizard;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the bot's actions following the !queue command.
 */
public class ShowQueueCommand implements Command {
    private LinkedBlockingQueue<AudioTrack> queue;

    /**
     * Creates a new ShowQueueCommand from the given {@code ClientContext}.
     * 
     * @param context The {@code ClientContext} to get audio components from
     */
    public ShowQueueCommand(ClientContext context) {
        this.queue = context.getAudioContext().getScheduler().getQueue();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        StringBuilder response = new StringBuilder();
        int x = 1;

        logger.append("- Showing the current audio track queue\n", LogDestination.API, LogDestination.NONAPI);

        for (AudioTrack i : queue.toArray(new AudioTrack[queue.size()])) {
            response.append("- " + x + ") " + i.getInfo().title + "\n");
            x++;
        }
        
        new IterableStringWizard(event, "Current Audio Queue", SoapUtility.splitAtEvery(response.toString(), 10)).begin();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("queue", "q", "songs");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Show the current audio queue")
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
