package com.georgster.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.object.entity.channel.MessageChannel;

public final class TrackScheduler implements AudioLoadResultHandler {

    private final AudioPlayer player;
    private final MessageChannel channel;

    public TrackScheduler(final AudioPlayer player, MessageChannel channel) {
        this.player = player;
        this.channel = channel;
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        channel.createMessage("Now Playing: " + track.getIdentifier()).block();
        player.playTrack(track);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // LavaPlayer found multiple AudioTracks from some playlist
    }

    @Override
    public void noMatches() {
        channel.createMessage("Failed to grab audio from the provided arguments");
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        channel.createMessage("Failed to grab audio from the provided arguments");
    }
}
