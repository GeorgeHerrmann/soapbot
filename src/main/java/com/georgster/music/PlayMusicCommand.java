package com.georgster.music;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.TrackScheduler;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.permissions.PermissibleAction;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;

/**
 * Represents the bot's actions following the !play command.
 */
public class PlayMusicCommand implements ParseableCommand {

    private final AudioProvider provider;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private static final String PATTERN = "1|R";

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord

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
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<PlayMusicCommand> logger = new MultiLogger<>(manager, PlayMusicCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        try {
            CommandParser parser = new CommandParser(PATTERN);
            parser.parse(pipeline.getFormattedMessage());
            if (pipeline.getPermissionsManager().hasPermissionSendError(manager, logger, getRequiredPermission(parser.getArguments()), pipeline.getAuthorAsMember())) {
                logger.append("\tArguments Found: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

                final Member member = pipeline.getAuthorAsMember(); //Makes sure the member is valid
                if (member != null) {
                    final VoiceState voiceState = member.getVoiceState().block();
                    if (voiceState != null) { //They must be in a voice channel
                        final VoiceChannel channel = voiceState.getChannel().block();
                        if (channel != null) { //And that channel must exist
                            logger.append("\tVerified Member and Voice Channel, distributing audio to the AudioPlayer and TrackScheduler\n",
                            LogDestination.NONAPI);
                            VoiceConnection connection = channel.join().withProvider(provider).block(); //allows us to modify the bot's connection state
                            scheduler.setChannelData(manager, connection);

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
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            logger.append("\tSending the help message", LogDestination.NONAPI);
            String[] output = SoapUtility.splitFirst(help());
            manager.sendText(output[1], output[0]);
            manager.sendText(output[1], output[2]);
        }
        logger.sendAll();
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
    @Override
    public CommandParser getCommandParser() {
        return new CommandParser(PATTERN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (!args.isEmpty()) {
            return PermissibleAction.PLAYMUSIC;
        } else {
            return PermissibleAction.DEFAULT;
        }
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
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Play music in a discord channel")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("url")
                        .description("The url of the audio to play")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
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
