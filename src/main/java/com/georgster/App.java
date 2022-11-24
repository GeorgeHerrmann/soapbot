package com.georgster;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;

import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import com.georgster.plinko.PlinkoCommand;
import com.georgster.profile.UserProfile;
import com.georgster.profile.ProfileHandler;

/**
 * The main class for SoapBot.
 */
public class App {

    public static void main(String[] args) {
        /* Initial formation and login of the bot */
        String token = "";
        try {
          token = Files.readString( Path.of(System.getProperty("user.dir"), "key.txt") );
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(0);
        }
        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES, Intent.GUILD_PRESENCES, Intent.GUILDS, Intent.GUILD_MESSAGE_TYPING)) //The intents the bot will work with
        .login().block();


        /*
         * The GuildCreateEvent is fired each time SOAP Bot logs in to a server, either upon the program start or when it is added to a new server.
         * It is here that we use the Profile Handler to generate a profile for a guild and its members if one doesn't exist yet.
         */
        client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(event -> { //Subscribing to an event meant SOAP Bot is now "listening" for these events
          List<Member> members = event.getGuild().getMembers().buffer().blockFirst(); //Stores all members of the guild this event was fired from in a List
          String guild = event.getGuild().getId().asString();
          if (!ProfileHandler.serverProfileExists(guild)) {
            ProfileHandler.createServerProfile(guild);
          }
          for (int i = 0; i < members.size(); i++) {
            String member = members.get(i).getId().asString();
            if (!ProfileHandler.userProfileExists(member, guild)) {
              ProfileHandler.createUserProfile(member, guild);
            }
            ProfileHandler.updateUserProfile(new UserProfile(guild, member, members.get(i).getUsername()));
          }
        });

        /* 
         * A MessageCreateEvent is fired each time a message is sent in a server and channel SOAP Bot has access to.
         * Note that client could technically be null here, however we can safely assume that will not be the case since our token should always be valid.
         */
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
          final String content = event.getMessage().getContent();
          if (content.startsWith("!") && content.equals(content.toUpperCase())) {
            event.getMessage().getChannel().block().createMessage("Please stop yelling at me :(").block();
          }
          final Map<String, Command> commands = new HashMap<>();
          /* Hard-defined commands that SOAP Bot has access to are stored in this HashMap */
          commands.put("ping", new PongCommand());
          commands.put("plinko", new PlinkoCommand());
          commands.put("help", new HelpCommand(commands));
          commands.put("soapbot", new SoapCommand());

          for (final Map.Entry<String, Command> entry : commands.entrySet()) {
            if (content.toLowerCase().startsWith('!' + entry.getKey())) {
                entry.getValue().execute(event); //The execute(event) method of each Command is the entry point for logic for a command
                break;
            }
          }
        });

        client.onDisconnect().block(); //Disconnects the bot from the server upon program termination
    }
}
