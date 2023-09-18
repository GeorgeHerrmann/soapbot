package com.georgster.music.components;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.voice.AudioProvider;

/**
 * An aggregation of all the objects SOAP Bot uses to play audio in a single
 * {@code Guild}. Each {@code SoapClient} has its own {@link AudioContext}.
 */
public class AudioContext {
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioProvider provider;

    /**
     * Creates a new {@ AudioContext}.
     */
    public AudioContext() {
        playerManager = new DefaultAudioPlayerManager();
        // This is an optimization strategy that Discord4J can utilize
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer(); //How Discord receives audio data
        scheduler = new TrackScheduler(player);
        provider = new LavaPlayerAudioProvider(player); //Implements LavaPlayer's audio provider in SOAP Bot
    }

    /**
     * Returns the {@link AudioPlayerManager} used by this {@link AudioContext}.
     * 
     * @return the {@link AudioPlayerManager} used by this {@link AudioContext}
     */
    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Returns the {@link AudioPlayer} used by this {@link AudioContext}.
     * 
     * @return the {@link AudioPlayer} used by this {@link AudioContext}
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * Returns the {@link TrackScheduler} used by this {@link AudioContext}.
     * 
     * @return the {@link TrackScheduler} used by this {@link AudioContext}
     */
    public TrackScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Returns the {@link AudioProvider} used by this {@link AudioContext}.
     * 
     * @return the {@link AudioProvider} used by this {@link AudioContext}
     */
    public AudioProvider getProvider() {
        return provider;
    }
}
