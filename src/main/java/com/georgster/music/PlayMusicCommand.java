package com.georgster.music;

import java.util.List;

import com.georgster.Command;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.TrackScheduler;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;
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
        MultiLogger<PlayMusicCommand> logger = new MultiLogger<>(manager, PlayMusicCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        try {
            CommandParser parser = new CommandParser(PATTERN);
            parser.parse(event.getMessage().getContent());
            logger.append("\tParsed: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

            final Member member = event.getMember().orElse(null); //Makes sure the member is valid
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) { //They must be in a voice channel
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) { //And that channel must exist
                        logger.append("\tVerified Member and Voice Channel, distributing audio to the AudioPlayer and TrackScheduler\n",
                        LogDestination.NONAPI);
                        VoiceConnection connection = channel.join().withProvider(provider).block(); //allows us to modify the bot's connection state
                        scheduler.setChannelData(event.getMessage().getChannel().block(), connection);

                        int retryAttempts = 0;
                        while (!attemptAudioStart(parser.get(0)) && retryAttempts < 3) {
                            logger.append("\tFailed to play audio, retrying...\n", LogDestination.NONAPI);
                            retryAttempts++;
                        }
                        if (retryAttempts >= 3) {
                            logger.append("\tFailed to play audio, retry limit reached\n", LogDestination.NONAPI);
                        } else {
                            logger.append("\tSuccessfully start audio\n", LogDestination.NONAPI);
                        }
                        logger.append("Playing audio in a discord channel", LogDestination.API);
                    }
                }
            }

            logger.sendAll();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            manager.sendText(help());
        }
    }

    /**
     * Attempts to start audio playback given the url.
     * 
     * @param url the url of the audio to play
     * @return true if the audio started successfully, false otherwise
     */
    private boolean attemptAudioStart(String url) {
        try {
            playerManager.loadItem(url, scheduler);
            return true;
        } catch (Exception e) {
            return false;
        }
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
        return List.of("play");
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
