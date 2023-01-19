package com.georgster.music.components;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import discord4j.voice.AudioProvider;

/**
 * An implementation of {@link AudioProvider} that delegates audio data to a {@link AudioPlayer}.
 */
public class LavaPlayerAudioProvider extends AudioProvider {
    private final AudioPlayer player;
    private final MutableAudioFrame frame = new MutableAudioFrame();
    /**
     * Constructs a LavaPlayerAudioProvider in SOAP Bot with the given audio player.
     * 
     * @param player The audio player this provider uses.
     */
    public LavaPlayerAudioProvider(final AudioPlayer player) {
        // Allocate a ByteBuffer for Discord4J's AudioProvider to hold audio data for Discord
        super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        // Set LavaPlayer's MutableAudioFrame to use the same buffer as the one we just allocated
        frame.setBuffer(getBuffer());
        this.player = player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean provide() {
        // AudioPlayer writes audio data to its AudioFrame
        final boolean didProvide = player.provide(frame);
        // If audio was provided, flip from write-mode to read-mode
        if (didProvide) {
            getBuffer().flip();
        }
        return didProvide;
    }

}
