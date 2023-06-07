package com.georgster.music;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.TrackScheduler;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
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

    private AudioProvider provider;
    private AudioPlayerManager playerManager;
    private AudioPlayer player;
    private TrackScheduler scheduler;
    private static final String PATTERN = "1|R";

    /**
     * Creates a new PlayMusicCommand from the given {@code ClientContext}.
     * 
     * @param context The {@code ClientContext} to get audio components from
     */
    public PlayMusicCommand(ClientContext context) {
        this.provider = context.getAudioContext().getProvider();
        this.playerManager = context.getAudioContext().getPlayerManager();
        this.player = context.getAudioContext().getPlayer();
        this.scheduler = context.getAudioContext().getScheduler();
        this.player.addListener(scheduler);
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        final GuildInteractionHandler handler = event.getGuildInteractionHandler();
        final MultiLogger logger = event.getLogger();
        final CommandParser parser = event.getCommandParser();
        final Member member = event.getDiscordEvent().getAuthorAsMember(); //Makes sure the member is valid

        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) { //They must be in a voice channel
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) { //And that channel must exist
                    logger.append("- Verified Member and Voice Channel, distributing audio to the AudioPlayer and TrackScheduler\n",
                    LogDestination.NONAPI);
                    VoiceConnection connection = channel.join().withProvider(provider).block(); //allows us to modify the bot's connection state
                    scheduler.setChannelData(handler, connection);

                    int retryAttempts = 0;
                    while (!attemptAudioStart(parser.get(0)) && retryAttempts < 3) {
                        logger.append("- Failed to play audio, retrying...\n", LogDestination.NONAPI);
                        retryAttempts++;
                    }
                    if (retryAttempts >= 3) {
                        logger.append("- Failed to play audio, retry limit reached\n", LogDestination.NONAPI);
                    } else {
                        logger.append("- Successfully start audio\n", LogDestination.NONAPI);
                    }
                    logger.append("- Playing audio in a discord channel", LogDestination.API);
                }
            }
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
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder(PATTERN).withoutAutoFormatting().build();
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
        return "Aliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n- !play [AUDIO LINK] to queue an audio track to play in the voice channel you are in" +
        "\n- !skip to skip the current track" +
        "\n- !skip all to skip all tracks in the queue" +
        "\n- !queue to see all tracks in the queue";
    }
    
}
