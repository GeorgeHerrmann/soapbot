package com.georgster;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.voice.AudioProvider;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import com.georgster.api.ActionWriter;
import com.georgster.dm.MessageCommand;
import com.georgster.events.SoapEventHandler;
import com.georgster.events.reserve.EventCommand;
import com.georgster.events.reserve.ReserveCommand;
import com.georgster.events.reserve.ReserveEvent;
import com.georgster.plinko.PlinkoCommand;
import com.georgster.profile.UserProfile;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapHandler;
import com.georgster.profile.ProfileHandler;
import com.georgster.music.LavaPlayerAudioProvider;
import com.georgster.music.PlayMusicCommand;
import com.georgster.music.ShowQueueCommand;
import com.georgster.music.SkipMusicCommand;
import com.georgster.music.TrackScheduler;

/**
 * The main class for SoapBot.
 */
public class App {

    public static void main(String[] args) {
        ActionWriter.writeAction("Setting up SOAP Bot's audio interface");
        /* This sets up an audio configuration so SOAP Bot can join and "speak" in channels */
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        // This is an optimization strategy that Discord4J can utilize, (it was in the docs i dunno what it does to be honest)
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        AudioSourceManagers.registerRemoteSources(playerManager); //Allows the player to receive "remote" audio sources
        final AudioPlayer player = playerManager.createPlayer(); //How Discord receives audio data

        TrackScheduler scheduler = new TrackScheduler(player);

        AudioProvider provider = new LavaPlayerAudioProvider(player); //Implements LavaPlayer's audio provider in SOAP Bot

        /* Initial formation and login of the bot */
        ActionWriter.writeAction("Logging SOAP Bot into Discord");
        String token = "";
        try {
          token = Files.readString( Path.of(System.getProperty("user.dir"), "key.txt") );
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(0);
        }
        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES, Intent.GUILD_PRESENCES, Intent.GUILDS, Intent.GUILD_MESSAGE_TYPING, Intent.GUILD_VOICE_STATES)) //The intents the bot will work with
        .login().block();

        ActionWriter.writeAction("Defining SOAP Bot's commands map");
        final Map<String, Command> commands = new HashMap<>();
        /* Hard-defined commands that SOAP Bot has access to are stored in this HashMap */
        commands.put("ping", new PongCommand());
        commands.put("plinko", new PlinkoCommand());
        commands.put("help", new HelpCommand(commands));
        commands.put("soapbot", new SoapCommand());
        commands.put("play", new PlayMusicCommand(provider, playerManager, player, scheduler));
        commands.put("skip", new SkipMusicCommand(player, scheduler));
        commands.put("queue", new ShowQueueCommand(scheduler.getQueue()));
        commands.put("dm", new MessageCommand());
        commands.put("reserve", new ReserveCommand());
        commands.put("events", new EventCommand());


        /*
         * The GuildCreateEvent is fired each time SOAP Bot logs in to a server, either upon the program start or when it is added to a new server.
         * It is here that we use the Profile Handler to generate a profile for a guild and its members if one doesn't exist yet.
         */
        client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(event -> { //Subscribing to an event meant SOAP Bot is now "listening" for these events
          String guild = event.getGuild().getId().asString(); //The ID of the guild this event was fired from
          /*
           * If the guild this event was fired from has events scheduled, we will restart them.
           */
          if (ProfileHandler.areEvents(guild)) { //Checks to see if there are any events at all for this guild
            ActionWriter.writeAction("Restarting events for " + event.getGuild().getName()); //Note that the ActionWriter is what tells SOAP Api what is happening
            //We keep a list of all channels in this guild, where channelMatcher will get us Channel objects from their names
            List<GuildChannel> channels = event.getGuild().getChannels().buffer().blockFirst();
            for (ReserveEvent reserve : ProfileHandler.getEvents(guild)) { //For each event in the guild's events list
              SoapHandler.runDaemon(() -> 
                SoapEventHandler.scheduleEvent(reserve, (MessageChannel) SoapHandler.channelMatcher(reserve.getChannel(), channels), guild) //We schedule the event again
              );
            }
          }
          ActionWriter.writeAction("Logging in to server: " + event.getGuild().getName());
          List<Member> members = event.getGuild().getMembers().buffer().blockFirst(); //Stores all members of the guild this event was fired from in a List
          if (!ProfileHandler.serverProfileExists(guild)) { //If the guild this event was fired from does not have a profile scheme, or has an out of date profile scheme, we create one
            ActionWriter.writeAction("Creating a profile for " + event.getGuild().getName());
            ProfileHandler.createServerProfile(guild);
          }
          for (int i = 0; i < members.size(); i++) { //Same idea for Members
            String member = members.get(i).getId().asString();
            if (!ProfileHandler.userProfileExists(member, guild)) {
              ProfileHandler.createUserProfile(member, guild);
            }
            ActionWriter.writeAction("Updating user profile number " + i + " in " + event.getGuild().getName());
            ProfileHandler.updateUserProfile(new UserProfile(guild, member, members.get(i).getUsername())); //We will always update the user's profile to make sure it is up to date
          }
        });
        /* 
         * A MessageCreateEvent is fired each time a message is sent in a server and channel SOAP Bot has access to.
         * We will parse the message and execute the associated command if it is a valid command.
         */
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
          GuildManager manager = new GuildManager(event.getGuild().block());
          manager.setActiveChannel(event.getMessage().getChannel().block());
          final String content = event.getMessage().getContent();
          if (content.startsWith("!") && content.equals(content.toUpperCase())) { //Very necessary
            ActionWriter.writeAction("Getting yelled at");
            manager.sendText("Please stop yelling at me :(");
          }
          
          for (final Map.Entry<String, Command> entry : commands.entrySet()) { //We check the message against each command in the commands map
            if (content.toLowerCase().startsWith('!' + entry.getKey())) { //If it matches we create a new thread and execute the command
                ActionWriter.writeAction("Placing the " + entry.getKey() + " command on a new Daemon thread and executing it");
                SoapHandler.runDaemon(() -> entry.getValue().execute(event, manager)); //The execute(event) method of each Command is the entry point for logic for a command
                break;
            }
          }
        });

        client.onDisconnect().block(); //Disconnects the bot from the server upon program termination
        ActionWriter.writeAction("Logging off and shutting down");
    }

}
