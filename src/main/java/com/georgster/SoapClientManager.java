package com.georgster;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Creates a new SoapClientManager to manage all SoapClients for each Guild SOAP Bot is in
     * and begins the login process to Discord.
     * 
     * @param token The token used to log in to Discord.
     */
    protected SoapClientManager(String token) {
        clients = new HashMap<>();

        discordClient = DiscordClientBuilder.create(token).build().gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, //The intents the bot will work with
        Intent.GUILD_MESSAGES, Intent.GUILD_PRESENCES,
        Intent.GUILDS,
        Intent.GUILD_MESSAGE_TYPING,
        Intent.GUILD_VOICE_STATES))
        .login().block();
    }

    /**
     * Finalizes the login process to discord by blocking the current thread until the
     * {@code GatewayDiscordClient} disconnects. This method should be called last after this manager
     * has been set up. Until this method is called, SOAP Bot is not fully logged in.
     */
    protected void start() {
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
    protected void listenToEvents() {
        EventDispatcher dispatcher = discordClient.getEventDispatcher();
        /*
         * The manager's distributeClient method will handle this event.
         */
        dispatcher.on(GuildCreateEvent.class)
        .subscribe(this::distributeClient); //Executes onGuildCreate when a GuildCreateEvent is fired
        /* 
         * A MessageCreateEvent is fired each time a message is sent in a server and channel SOAP Bot has access to.
         * We will distribute the event to the associated client.
         */
        dispatcher.on(MessageCreateEvent.class)
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
        SoapClient newClient = new SoapClient(event.getGuild().getId());
        clients.put(event.getGuild().getId(), newClient);
        newClient.onGuildCreate(event);
    }
}
