package com.georgster;

import java.util.List;

import com.georgster.api.ActionWriter;
import com.georgster.events.SoapEventHandler;
import com.georgster.events.reserve.ReserveEvent;
import com.georgster.music.LavaPlayerAudioProvider;
import com.georgster.music.TrackScheduler;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.UserProfile;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.voice.AudioProvider;

/**
 * An aggregation of all the shard-specific objects that SOAP Bot needs to run for
 * a single {@code Guild}. Each SoapClient handles all the events that occur in
 * its associated {@code Guild} and houses its {@code CommandRegistry}.
 */
public final class SoapClient {
    private final Snowflake flake;

    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioProvider provider;

    private final CommandRegistry registry;
    
    /**
     * Creates a new {@code SoapClient} for the associated {@code Guild} represented
     * by its unique {@code Snowflake}. Sets up an audio interface and
     * constructs a new {@code CommandRegistry} for this client.
     */
    protected SoapClient(Snowflake flake) {
        this.flake = flake;

        playerManager = new DefaultAudioPlayerManager();
        // This is an optimization strategy that Discord4J can utilize
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer(); //How Discord receives audio data
        scheduler = new TrackScheduler(player);
        provider = new LavaPlayerAudioProvider(player); //Implements LavaPlayer's audio provider in SOAP Bot

        registry = new CommandRegistry(this);
    }

    /**
     * Defines SOAP Bot's actions when a GuildCreateEvent is fired.
     * Here, we will check if the associated guild and all its members 
     * have up to date profile schemes, and if not, we will create them.
     * 
     * @param event The GuildCreateEvent that was fired.
     */
    protected void onGuildCreate(GuildCreateEvent event) {
        GuildManager manager = new GuildManager(event.getGuild());
        ProfileHandler handler = manager.getHandler();
        /*
         * If the guild this event was fired from has events scheduled, we will restart them.
        */
        if (handler.areEvents()) { //Checks to see if there are any events at all for this guild
          ActionWriter.writeAction("Restarting events for " + manager.getGuild().getName()); //Note that the ActionWriter is what tells SOAP Api what is happening
          //We keep a list of all channels in this guild, where channelMatcher will get us Channel objects from their names
          for (ReserveEvent reserve : handler.getEvents()) { //For each event in the guild's events list
            SoapHandler.runDaemon(() -> 
              SoapEventHandler.scheduleEvent(reserve, manager) //We schedule the event again
            );
          }
        }
        ActionWriter.writeAction("Logging in to server: " + manager.getGuild().getName());
        List<Member> members = manager.getAllMembers(); //Stores all members of the guild this event was fired from in a List
        if (!handler.serverProfileExists()) { //If the guild this event was fired from does not have a profile scheme, or has an out of date profile scheme, we create one
          ActionWriter.writeAction("Creating a profile for " + manager.getGuild().getName());
          handler.createServerProfile();
        }
        for (int i = 0; i < members.size(); i++) { //Same idea for Members
          String member = members.get(i).getId().asString();
          if (!handler.userProfileExists(member)) {
            handler.createUserProfile(member);
          }
          ActionWriter.writeAction("Updating user profile number " + i + " in " + event.getGuild().getName());
          handler.updateUserProfile(new UserProfile(manager.getId(), member, members.get(i).getUsername())); //We will always update the user's profile to make sure it is up to date
        }
    }

    /**
     * Defines SOAP Bot's actions when a MessageCreateEvent is fired.
     * Here, we will send the message to the CommandRegistry to be parsed have it
     * execute the associated command if it is a valid command.
     * 
     * @param event The MessageCreateEvent that was fired.
     */
    protected void onMessageCreate(MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        if (content.startsWith("!") && content.equals(content.toUpperCase())) { //Very necessary
          ActionWriter.writeAction("Getting yelled at");
          GuildManager.sendText("Please stop yelling at me :(", ((TextChannel) event.getMessage().getChannel().block()));
        }
          
        registry.getAndExecute(event);
    }

    /**
     * Gets the {@code Snowflake} for the Guild this SoapClient is controlling.
     * 
     * @return the {@code Snowflake} for the Guild this SoapClient is controlling.
     */
    protected Snowflake getSnowflake() {
        return flake;
    }

    /**
     * Gets the AudioPlayerManager for this SoapClient.
     * 
     * @return The AudioPlayerManager for this SoapClient.
     */
    protected AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Gets the AudioPlayer for this SoapClient.
     * 
     * @return The AudioPlayer for this SoapClient.
     */
    protected AudioPlayer getPlayer() {
        return player;
    }

    /**
     * Gets the TrackScheduler for this SoapClient.
     * 
     * @return The TrackScheduler for this SoapClient.
     */
    protected TrackScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Gets the AudioProvider for this SoapClient.
     * 
     * @return The AudioProvider for this SoapClient.
     */
    protected AudioProvider getProvider() {
        return provider;
    }

    /**
     * Gets the CommandRegistry for this SoapClient.
     * 
     * @return The CommandRegistry for this SoapClient.
     */
    protected CommandRegistry getRegistry() {
        return registry;
    }
}
