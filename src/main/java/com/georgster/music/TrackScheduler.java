package com.georgster.music;

import java.util.concurrent.LinkedBlockingQueue;

import com.georgster.App;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.voice.VoiceConnection;

public final class TrackScheduler extends AudioEventAdapter implements AudioLoadResultHandler {

    private final AudioPlayer player;
    private MessageChannel channel;
    private LinkedBlockingQueue<AudioTrack> queue;
    private VoiceConnection connection;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        channel = null;
        connection = null;
        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        try {
            queue.put(track);
            if (queue.size() == 1) {
                player.playTrack(track);
            } else {
                App.runNow(() -> sendMessageInChannel("Queued up track: " + track.getInfo().title));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        boolean first = (queue.isEmpty());
        App.runNow(() -> sendMessageInChannel("Queued up playlist: " + playlist.getName()));
        for (AudioTrack i : playlist.getTracks()) {
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

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        try {
            if (!queue.isEmpty()) queue.take();
            if (!queue.isEmpty()) {
                player.playTrack(queue.peek());
            } else {
                /* The actions of the bot in Discord and LavaPlayer must be kept on separate threads to prevent thread interruptions */
                App.runNow(() -> connection.disconnect().block());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        App.runNow(() -> sendMessageInChannel("Now Playing: " + track.getInfo().title));
    }

    @Override
    public void noMatches() {
        App.runNow(() -> {
            sendMessageInChannel("Failed to grab audio from the provided arguments");
            if (!isActive()) {
                connection.disconnect().block();
            }
        });
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        App.runNow(() -> sendMessageInChannel("An error occurred: " + exception.getMessage()));
    }

    protected void clearQueue() {
        queue.clear();
    }

    public boolean isActive() {
        return !queue.isEmpty();
    }

    public LinkedBlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void setChannelData(MessageChannel channel, VoiceConnection connection) {
        this.channel = channel;
        this.connection = connection;
    }

    private void sendMessageInChannel(String message) {
        channel.createMessage(message).block();
    }
}
