package com.georgster.music;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;

/**
 * Represents the bot's actions following the !play command.
 */
public class PlayMusicCommand implements Command {

    private final AudioProvider provider;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    /**
     * Plays music in a discord channel.
     * 
     * @param voiceProvider the audio provider
     * @param manager the audio player manager
     * @param musicPlayer the audio player
     * @param scheduler the track scheduler
     */
    public PlayMusicCommand(AudioProvider voiceProvider, AudioPlayerManager manager, AudioPlayer musicPlayer, TrackScheduler scheduler) {
      provider = voiceProvider;
      playerManager = manager;
      player = musicPlayer;
      this.scheduler = scheduler;
      player.addListener(scheduler);
    }

    /**
     * Plays music in a discord channel.
     * 
     * @param event the event that triggered the command
     */
    public void execute(MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" ")); //Each "arument" is separated by a space
        if (command.size() >= 2) { //Checks to see if there were arguments after !play
            final Member member = event.getMember().orElse(null); //Makes sure the member is valid
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) { //They must be in a voice channel
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) { //And that channel must exist
                        ActionWriter.writeAction("Joining a voice channel");
                        VoiceConnection connection = channel.join().withProvider(provider).block(); //allows us to modify the bot's connection state
                        scheduler.setChannelData(event.getMessage().getChannel().block(), connection);
                        playerManager.loadItem(command.get(1), scheduler);
                        ActionWriter.writeAction("Playing audio in a discord channel");
                    }
                }
            }
        } else {
            event.getMessage().getChannel().block().createMessage(help()).block();
        }
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
