package com.georgster.music.components;

import java.util.concurrent.LinkedBlockingQueue;

import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import discord4j.voice.VoiceConnection;

/**
 * This class schedules tracks for the audio player. It contains the queue of
 * tracks that need to be played.
 */
public final class TrackScheduler extends AudioEventAdapter implements AudioLoadResultHandler {

    private final AudioPlayer player;
    private GuildManager manager;
    private LinkedBlockingQueue<AudioTrack> queue;
    private VoiceConnection connection;

    /**
     * Creates a track scheduler that will schedule and play tracks with the provided player in the given voice channel.
     * 
     * @param player the audio player that will play the tracks
     */
    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        manager = null;
        connection = null;
        queue = new LinkedBlockingQueue<>();
    }

    /**
     * The event fired after LavaPlayer has loaded a track.
     * SOAP Bot will queue up the track and play it if it is the only track in the queue.
     * 
     * @param track the track that was loaded
     */
    @Override
    public void trackLoaded(final AudioTrack track) {
        try {
            queue.put(track);
            if (queue.size() == 1) {
                player.playTrack(track); //If it is the only track in the queue we will play it here as well.
            } else {
                SoapUtility.runDaemon(() -> manager.sendText("Queued up track: " + track.getInfo().title)); //All actions sent to Discord's API must be done on a separate thread.
            }
        } catch (InterruptedException e) { //There is something wrong with the queue, so we will stop the player.
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * The event fired after LavaPlayer has loaded a playlist.
     * 
     * @param playlist the playlist that was loaded
     */
    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        boolean first = (queue.isEmpty()); //If the queue is empty, we will play the first track in the playlist.
        SoapUtility.runDaemon(() -> manager.sendText("Queued up playlist: " + playlist.getName()));
        for (AudioTrack i : playlist.getTracks()) { //Add all tracks in the playlist to the queue.
            try {
                queue.put(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        if (first) {
            player.playTrack(queue.peek());
        }
    }

    /**
     * The event fired when LavaPlayer has finished playing a track.
     * 
     * @param player the audio player that was playing the track
     * @param track the track that was played
     * @param endReason the reason why the track ended
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        try {
            if (!queue.isEmpty()) queue.take();
            if (!queue.isEmpty()) {
                player.playTrack(queue.peek());
            } else {
                /* The actions of the bot in Discord and LavaPlayer must be kept on separate threads to prevent thread interruptions */
                SoapUtility.runDaemon(() -> connection.disconnect().block());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * The event fired when LavaPlayer has started playing a track.
     * 
     * @param player the audio player that was playing the track
     * @param track the track that was played
     */
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        SoapUtility.runDaemon(() -> manager.sendText("Now Playing: " + track.getInfo().title)); //The queuing and playing happens in onTrackLoaded, so we will just send a message here.
    }

    /**
     * The event fired when LavaPlayer has no matches for the provided arguments.
     */
    @Override
    public void noMatches() {
        SoapUtility.runDaemon(() -> {
            manager.sendText("Failed to grab audio from the provided arguments");
            if (!isActive()) {
                connection.disconnect().block(); //If the queue is empty, we will disconnect from the voice channel.
            }
        });
    }

    /**
     * The event fired when LavaPlayer has an error loading a track.
     */
    @Override
    public void loadFailed(final FriendlyException exception) {
        SoapUtility.runDaemon(() -> manager.sendText("An error occurred: " + exception.getMessage()));
    }

    /**
     * Clears the queue of tracks.
     */
    public void clearQueue() {
        queue.clear();
    }

    /**
     * Returns whether or not the queue is empty.
     * 
     * @return true if the queue is empty, false otherwise
     */
    public boolean isActive() {
        return !queue.isEmpty();
    }

    /**
     * Returns the queue of tracks.
     * 
     * @return the queue of tracks
     */
    public LinkedBlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    /**
     * Sets the active channel and connection for the scheduler.
     * 
     * @param channel the channel to send messages to
     * @param connection the connection to disconnect from
     */
    public void setChannelData(GuildManager manager, VoiceConnection connection) {
        this.manager = manager;
        this.connection = connection;
    }
}
