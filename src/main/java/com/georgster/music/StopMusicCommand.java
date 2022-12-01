package com.georgster.music;

import com.georgster.Command;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class StopMusicCommand implements Command {

    private final AudioPlayer player;

    public StopMusicCommand(AudioPlayer player) {
        this.player = player;
    }

    public void execute(MessageCreateEvent event) {
        player.stopTrack();
    }

    public String help() {
        return "Command: !play & !stop" +
        "\nUsage:" +
        "\n\t!play [YOUTUBE LINK] to play audio from a YouTube Video in the voice channel you are in" +
        "\n\t!stop to stop playing audio";
    }
    
}
