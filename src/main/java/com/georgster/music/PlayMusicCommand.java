package com.georgster.music;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.georgster.util.CommandParser;
import com.georgster.util.GuildManager;
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
    private static final String PATTERN = "1|R";

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
    public void execute(MessageCreateEvent event, GuildManager manager) {
        try {
            CommandParser parser = new CommandParser(PATTERN);
            parser.parse(event.getMessage().getContent().toLowerCase());
            final Member member = event.getMember().orElse(null); //Makes sure the member is valid
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) { //They must be in a voice channel
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) { //And that channel must exist
                        ActionWriter.writeAction("Joining a voice channel");
                        VoiceConnection connection = channel.join().withProvider(provider).block(); //allows us to modify the bot's connection state
                        scheduler.setChannelData(event.getMessage().getChannel().block(), connection);
                        playerManager.loadItem(parser.get(1), scheduler);
                        ActionWriter.writeAction("Playing audio in a discord channel");
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            manager.sendText(help());
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
