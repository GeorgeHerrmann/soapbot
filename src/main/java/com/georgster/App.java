package com.georgster;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Map;
import java.util.HashMap;

/**
 * The main class for SoapBot.
 */
public class App {

    public static void main(String[] args) {
        final String token = "MTAzMjY4NjkwMjAyNjk3MzIyNQ.GgDqeW.j8vKUUyniZkacPA0bd2PaG7L83_DIAY48_XXuo";
        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

        /* Note that client could technically be null here, however we can safely assume that will not be the case since our token should always be valid */
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
          final String content = event.getMessage().getContent();
          final Map<String, Command> commands = new HashMap<>();
          Command pong = new PongCommand();
          commands.put("ping", pong);

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
