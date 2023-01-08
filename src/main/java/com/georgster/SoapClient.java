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
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.voice.AudioProvider;

/**
 * An aggregation of all the shard-specific objects that SOAP Bot needs to run.
 * Controls the audio interface for SOAP Bot, houses the GatewayDiscordClient and CommandRegistry, and
 * subscribes to Discord's event stream.
 */
public class SoapClient {
    /* This sets up an audio configuration so SOAP Bot can join and "speak" in channels */
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioProvider provider;

    private final CommandRegistry registry;

    private final GatewayDiscordClient client;
    
    /**
     * Creates a new SoapClient with the given discord token to log in with.
     * Upon creation, this SoapClient will immediately attempt to log in to Discord.
     * 
     * @param token The discord token to log in with.
     */
    public SoapClient(String token) {
        playerManager = new DefaultAudioPlayerManager();
        // This is an optimization strategy that Discord4J can utilize
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        player = playerManager.createPlayer(); //How Discord receives audio data
        scheduler = new TrackScheduler(player);
        provider = new LavaPlayerAudioProvider(player); //Implements LavaPlayer's audio provider in SOAP Bot

        registry = new CommandRegistry(this);

        client = DiscordClientBuilder.create(token).build().gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, //The intents the bot will work with
        Intent.GUILD_MESSAGES, Intent.GUILD_PRESENCES,
        Intent.GUILDS,
        Intent.GUILD_MESSAGE_TYPING,
        Intent.GUILD_VOICE_STATES))
        .login().block();
    }

    /**
     * Begins listening to all Discord's events that SOAP Bot needs to handle.
     * These events are currently:
     * <ul>
     * <li>GuildCreateEvent</li>
     * <li>MessageCreateEvent</li>
     * </ul>
     */
    public void listenToEvents() {
        /*
         * The GuildCreateEvent is fired each time SOAP Bot logs in to a server, either upon the program start or when it is added to a new server.
         * It is here that we use the Profile Handler to generate a profile for a guild and its members if one doesn't exist yet.
         */
        client.getEventDispatcher()
        .on(GuildCreateEvent.class)
        .subscribe(this::onGuildCreate); //Executes onGuildCreate when a GuildCreateEvent is fired
        /* 
         * A MessageCreateEvent is fired each time a message is sent in a server and channel SOAP Bot has access to.
         * We will parse the message and execute the associated command if it is a valid command.
         */
        client.getEventDispatcher()
        .on(MessageCreateEvent.class)
        .subscribe(this::onMessageCreate); //Executes onMessageCreate when a MessageCreateEvent is fired
    }

    private void onGuildCreate(GuildCreateEvent event) {
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

    private void onMessageCreate(MessageCreateEvent event) {
        final String content = event.getMessage().getContent();
        if (content.startsWith("!") && content.equals(content.toUpperCase())) { //Very necessary
          ActionWriter.writeAction("Getting yelled at");
          GuildManager.sendText("Please stop yelling at me :(", ((TextChannel) event.getMessage().getChannel().block()));
        }
          
        registry.getAndExecute(event);
    }



    /**
     * Starts this Soap Client. This will block the current thread until the client disconnects.
     * If this method is not called, the client will not connect successfully connect to Discord.
     */
    public void start() {
        client.onDisconnect().block();
    }

    /**
     * Gets the AudioPlayerManager for this SoapClient.
     * 
     * @return The AudioPlayerManager for this SoapClient.
     */
    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Gets the AudioPlayer for this SoapClient.
     * 
     * @return The AudioPlayer for this SoapClient.
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * Gets the TrackScheduler for this SoapClient.
     * 
     * @return The TrackScheduler for this SoapClient.
     */
    public TrackScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Gets the AudioProvider for this SoapClient.
     * 
     * @return The AudioProvider for this SoapClient.
     */
    public AudioProvider getProvider() {
        return provider;
    }

    /**
     * Gets the CommandRegistry for this SoapClient.
     * 
     * @return The CommandRegistry for this SoapClient.
     */
    public CommandRegistry getRegistry() {
        return registry;
    }
}
