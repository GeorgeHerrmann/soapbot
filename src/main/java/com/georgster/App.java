package com.georgster;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import com.georgster.plinko.PlinkoCommand;
import com.georgster.profile.ProfileHandler;

/**
 * The main class for SoapBot.
 */
public class App {

    public static void main(String[] args) {
        String token = "";
        try {
          token = Files.readString( Path.of(System.getProperty("user.dir") + "\\key.txt") );
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(0);
        }
        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

        client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(event -> {
          String guild = event.getGuild().getId().asString();
          if (!ProfileHandler.serverProfileExists(guild)) {
            ProfileHandler.createServerProfile(guild);
          }
        });

        /* Note that client could technically be null here, however we can safely assume that will not be the case since our token should always be valid */
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
          final String content = event.getMessage().getContent();
          final Map<String, Command> commands = new HashMap<>();
          Command pong = new PongCommand();
          Command plinko = new PlinkoCommand();
          Command soap = new SoapCommand();
          Command help = new HelpCommand(commands);
          commands.put("ping", pong);
          commands.put("plinko", plinko);
          commands.put("help", help);
          commands.put("soapbot", soap);

          for (final Map.Entry<String, Command> entry : commands.entrySet()) {
            if (content.startsWith('!' + entry.getKey())) {
                entry.getValue().execute(event);
                break;
            }
          }
        });

        client.onDisconnect().block();
    }
}
