package com.georgster;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import java.util.Map;
import java.util.HashMap;

public class App
{
    private static final Map<String, Command> commands = new HashMap<>();

    static {
      commands.put("ping", event -> event.getMessage()
          .getChannel().block()
          .createMessage("Pong!").block());
  }
    public static void main( String[] args )
    {
        String token = "MTAzMjY4NjkwMjAyNjk3MzIyNQ.GgDqeW.j8vKUUyniZkacPA0bd2PaG7L83_DIAY48_XXuo";
        DiscordClient client = DiscordClient.create(token);

        client.login();

        /*Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
          // ReadyEvent example
          Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
              Mono.fromRunnable(() -> {
                final User self = event.getSelf();
                System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
              }))
              .then();
          // MessageCreateEvent example
          Mono<Void> handlePingCommand = gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();
            StringBuilder userMessage = new StringBuilder(message.getContent());
            StringBuilder fullMessage = new StringBuilder();
            for (int i = 0; i < userMessage.length(); i++) {
              if (userMessage.toString().contains("!ping")) {
                userMessage.delete(userMessage.indexOf("!ping"), userMessage.indexOf("!ping") + 5);
                fullMessage.append("pong!");
              }
            }
            
            if (fullMessage.length() > 1) {
              return message.getChannel().flatMap(channel -> channel.createMessage(fullMessage.toString()));
            }
            return Mono.empty();
          }).then();
        
          // combine them!
          return printOnLogin.and(handlePingCommand);
        }); */

        client.login().block();

    }
}
