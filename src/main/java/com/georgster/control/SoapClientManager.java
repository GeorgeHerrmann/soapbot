package com.georgster.control;

import java.util.HashMap;
import java.util.Map;

import com.georgster.control.util.ClientPipeline;
import com.georgster.logs.MultiLogger;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;

/**
 * Manages all {@code SoapClient}s for each {@code Guild} SOAP Bot is in.
 * Each {@code Guild} gets a SoapClient which this manager will
 * distribute events to. This manager also houses the {@code GatewayDiscordClient},
 * maintaining the active connection to Discord's API.
 */
public final class SoapClientManager {
    /*
     * Using a Map means each Guild will have, at most, one SoapClient, 
     * which is easily accessible by its unique Snowflake (containing the Guild's ID). 
     */
    private final Map<Snowflake, SoapClient> clients;
    private final GatewayDiscordClient discordClient; //Maintains the connection to Discord
    private final EventDispatcher dispatcher;

    /**
     * Creates a new SoapClientManager to manage all SoapClients for each Guild SOAP Bot is in
     * and begins the login process to Discord.
     * 
     * @param token The token used to log in to Discord.
     */
    public SoapClientManager(String token) {
        clients = new HashMap<>();

        discordClient = DiscordClientBuilder.create(token).build().gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, //The intents the bot will work with
        Intent.GUILD_MESSAGES, Intent.GUILD_PRESENCES,
        Intent.GUILDS, Intent.GUILD_MESSAGE_TYPING,
        Intent.GUILD_VOICE_STATES, Intent.GUILD_MESSAGE_REACTIONS))
        .login().block();

        this.dispatcher = discordClient.getEventDispatcher();
    }

    /**
     * Finalizes the login process to discord by blocking the current thread until the
     * {@code GatewayDiscordClient} disconnects. This method should be called last after this manager
     * has been set up. Until this method is called, SOAP Bot is not fully logged in.
     */
    public void start() {
        MultiLogger.wipeFileLogs(); //Temporary
        discordClient.onDisconnect().block();
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
        dispatcher.on(GuildCreateEvent.class)
        .subscribe(this::distributeClient); //Executes onGuildCreate when a GuildCreateEvent is fired

        dispatcher.on(MessageCreateEvent.class)
        .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> message.getMessage().getContent().startsWith("!"))
        .subscribe(event -> clients.get(event.getGuildId().get()).onMessageCreate(event)); //Executes onMessageCreate when a MessageCreateEvent is fired
    }

    /**
     * Creates a new {@code SoapClient} for the associated {@code Guild} in the
     * {@code GuildCreateEvent} and distributes the event to the new client.
     * 
     * The {@code GuildCreateEvent} is fired each time SOAP Bot "logs in" to a {@code Guild},
     * either upon program start, when it is added to a new {@code Guild}, or upon reconnection
     * if the {@code GatewayDiscordClient}'s connection was dropped.
     * 
     * @param event The GuildCreateEvent that was fired.
     */
    private void distributeClient(GuildCreateEvent event) {
        Snowflake flake = event.getGuild().getId();
        clients.computeIfAbsent(flake, client -> new SoapClient(new ClientPipeline(dispatcher, event.getGuild(), discordClient.getRestClient()))); //Creates a new SoapClient if one does not already exist for the Guild
        clients.get(flake).onGuildCreate(event); //Distributes the event to the client
    }
}
