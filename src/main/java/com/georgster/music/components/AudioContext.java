package com.georgster.music.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.AndroidMusicWithThumbnail;
import dev.lavalink.youtube.clients.IosWithThumbnail;
import dev.lavalink.youtube.clients.MWebWithThumbnail;
import dev.lavalink.youtube.clients.MusicWithThumbnail;
import dev.lavalink.youtube.clients.TvHtml5EmbeddedWithThumbnail;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import dev.lavalink.youtube.clients.skeleton.Client;
import discord4j.voice.AudioProvider;

/**
 * An aggregation of all the objects SOAP Bot uses to play audio in a single
 * {@code Guild}. Each {@code SoapClient} has its own {@link AudioContext}.
 */
public final class AudioContext {
    private static final YoutubeAudioSourceManager youtubeSourceManager = new YoutubeAudioSourceManager(/*allowSearch:*/ true, new Client[] { new MusicWithThumbnail(), new MWebWithThumbnail(), new WebWithThumbnail(), new AndroidMusicWithThumbnail(), new IosWithThumbnail(), new TvHtml5EmbeddedWithThumbnail() });

    private static boolean singletonInit = false; // This is a flag that indicates whether the OAuth2 token has been enabled

    private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioProvider provider;

    /**
     * Creates a new {@link AudioContext}.
     */
    public AudioContext() {
        initSingletons();

        player = playerManager.createPlayer(); //How Discord receives audio data
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        provider = new LavaPlayerAudioProvider(player); //Implements LavaPlayer's audio provider in SOAP Bot
    }

    private static void initSingletons() {
        if (singletonInit) { // If the OAuth2 token has already been enabled, return
            return;
        }

        playerManager.registerSourceManager(youtubeSourceManager);
        // This is an optimization strategy that Discord4J can utilize
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager); // This is a Youtube source manager that allows SOAP Bot to play Youtube videos

        String token = "";
        try { // Gets the API key
          token = Files.readString( Path.of(System.getProperty("user.dir"), "youtube_oauth_token.txt") );
        } catch (IOException e) { // If the key file doesn't exist, the program will exit
          e.printStackTrace();
          System.exit(1);
        }
        youtubeSourceManager.useOauth2(token, true);

        singletonInit = true; // Set the flag to true
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
