package com.georgster.music;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.api.ActionWriter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;

public class PlayMusicCommand implements Command {

    private AudioProvider provider;
    private AudioPlayerManager playerManager;
    private AudioPlayer player;

    public PlayMusicCommand(AudioProvider voiceProvider, AudioPlayerManager manager, AudioPlayer musicPlayer) {
      provider = voiceProvider;
      playerManager = manager;
      player = musicPlayer;
    }

    public void execute(MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        final List<String> command = Arrays.asList(content.split(" "));
        if (command.size() >= 2) { //Checks to see if there were arguments after !play
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        ActionWriter.writeAction("Joining a voice channel");
                        channel.join().withProvider(provider).block();
                    }
                }
            }
            final TrackScheduler scheduler = new TrackScheduler(player, event.getMessage().getChannel().block());
            playerManager.loadItem(command.get(1), scheduler);
            ActionWriter.writeAction("Playing audio in a discord channel");
        } else {
            event.getMessage().getChannel().block().createMessage(help()).block();
        }
    }

    public String help() {
        return "Command: !play & !stop" +
        "\nUsage:" +
        "\n\t!play [YOUTUBE LINK] to play audio from a YouTube Video in the voice channel you are in" +
        "\n\t!stop to stop playing audio";
    }
    
}
