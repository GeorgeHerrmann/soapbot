package com.georgster.music.components;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.voice.AudioProvider;

/**
 * An aggregation of all the objects SOAP Bot uses to play audio in a single
 * {@code Guild}. Each {@code SoapClient} has its own {@code AudioInterface}.
 */
public class AudioInterface {
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioProvider provider;

    /**
     * Creates a new {@code AudioInterface}.
     */
    public AudioInterface() {
        playerManager = new DefaultAudioPlayerManager();
        // This is an optimization strategy that Discord4J can utilize
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer(); //How Discord receives audio data
        scheduler = new TrackScheduler(player);
        provider = new LavaPlayerAudioProvider(player); //Implements LavaPlayer's audio provider in SOAP Bot
    }

    /**
     * Returns the {@code AudioPlayerManager} used by this {@code AudioInterface}.
     * 
     * @return the {@code AudioPlayerManager} used by this {@code AudioInterface}
     */
    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Returns the {@code AudioPlayer} used by this {@code AudioInterface}.
     * 
     * @return the {@code AudioPlayer} used by this {@code AudioInterface}
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * Returns the {@code TrackScheduler} used by this {@code AudioInterface}.
     * 
     * @return the {@code TrackScheduler} used by this {@code AudioInterface}
     */
    public TrackScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Returns the {@code AudioProvider} used by this {@code AudioInterface}.
     * 
     * @return the {@code AudioProvider} used by this {@code AudioInterface}
     */
    public AudioProvider getProvider() {
        return provider;
    }
}
