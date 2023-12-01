package com.georgster.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.georgster.control.manager.GlobalSoapManager;
import com.georgster.control.manager.SoapManager;
import com.georgster.control.manager.UserSettingsManager;
import com.georgster.control.util.ClientContext;

import com.georgster.logs.MultiLogger;
import com.georgster.settings.UserSettings;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;

/**
 * Manages all {@link SoapClient SoapClients} for each {@code Guild} SOAP Bot is in.
 * Each {@code Guild} gets a SoapClient which this manager will
 * distribute events to. This manager also houses the {@link GatewayDiscordClient},
 * maintaining the active connection to Discord's API.
 */
public final class SoapClientManager {
    private static Set<GlobalSoapManager<?>> managers = new HashSet<>();
    private final Map<Snowflake, SoapClient> clients; // Maps each Guild's Snowflake ID to its client
    private final GatewayDiscordClient discordClient; //Maintains the connection to Discord
    private final EventDispatcher dispatcher;
    private boolean testMode;

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
        Intent.GUILD_VOICE_STATES, Intent.GUILD_MESSAGE_REACTIONS, Intent.DIRECT_MESSAGES, Intent.DIRECT_MESSAGE_REACTIONS))
        .login().block();

        this.dispatcher = discordClient.getEventDispatcher();
        this.testMode = false;

        loadManagers();
    }

    private static void loadManagers() {
        if (managers.isEmpty()) {
            managers.add(new UserSettingsManager());
        }
        managers.forEach(SoapManager::load);
    }

    /**
     * Finalizes the login process to discord by blocking the current thread until the
     * {@link GatewayDiscordClient} disconnects. This method should be called last after this manager
     * has been set up. Until this method is called, SOAP Bot is not fully logged in.
     */
    public void start() {
        MultiLogger.wipeFileLogs(); //Temporary
        discordClient.onDisconnect().block();
    }

    /**
     * Begins listening to all Discord's events that SOAP Bot needs to handle
     * with permanent listeners.
     * <p>
     * These events are currently:
     * <ul>
     * <li>{@link GuildCreateEvent}</li>
     * <li>{@link MessageCreateEvent}</li>
     * <li>{@link ChatInputInteractionEvent}</li>
     * <li>{@link MemberJoinEvent}</li>
     * <li>{@link RoleUpdateEvent}</li>
     * <li>{@link RoleCreateEvent}</li>
     * </ul>
     */
    public void listenToEvents() {
        dispatcher.on(GuildCreateEvent.class)
        .subscribe(event -> {
            event.getGuild().getMembers().collectList().block().forEach(member -> {
                UserSettingsManager manager = getUserSettingsManager();
                if (!manager.exists(member.getId().asString())) {
                    manager.add(new UserSettings(member.getId().asString()));
                }
            });
            this.distributeClient(event);
        }); //Executes onGuildCreate when a GuildCreateEvent is fired

        dispatcher.on(MessageCreateEvent.class)
        .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> isValidForTestMode(message.getMessage().getContent()))
        .subscribe(event -> clients.get(event.getGuild().block().getId()).onMessageCreate(event)); //Executes onMessageCreate when a MessageCreateEvent is fired

        dispatcher.on(ChatInputInteractionEvent.class)
        .filter(message -> !testMode) //If test mode is enabled, commands must be sent with the !! notation, slash commands are not supported
        .subscribe(event -> clients.get(event.getInteraction().getGuildId().get()).onChatInputInteraction(event));

        dispatcher.on(MemberJoinEvent.class)
        .filter(event -> !event.getMember().isBot())
        .subscribe(event -> {
            UserSettingsManager manager = getUserSettingsManager();
            if (!manager.exists(event.getMember().getId().asString())) {
                manager.add(new UserSettings(event.getMember().getId().asString()));
            }
            clients.get(event.getGuildId()).onMemberJoin(event);
        });

        dispatcher.on(RoleUpdateEvent.class)
        .subscribe(event -> clients.get(event.getCurrent().getGuild().block().getId()).onRoleUpdate(event));

        dispatcher.on(RoleCreateEvent.class)
        .subscribe(event -> clients.get(event.getGuildId()).onRoleCreate(event));
    }

    /**
     * Returns if the given message is valid for the current test mode setting.
     * 
     * @param message The message to check.
     * @return If the message is valid for the current test mode setting.
     */
    private boolean isValidForTestMode(String message) {
        if (testMode) {
            return message.startsWith("!!") || message.startsWith("//");
        } else {
            return (message.startsWith("!") && !message.startsWith("!!")) || (message.startsWith("/") && !message.startsWith("//"));
        }
    }

    /**
     * Creates a new {@link SoapClient} for the associated {@code Guild} in the
     * {@link GuildCreateEvent} and distributes the event to the new client.
     * <p>
     * The {@link GuildCreateEvent} is fired each time SOAP Bot "logs in" to a {@code Guild},
     * either upon program start, when it is added to a new {@code Guild}, or upon reconnection
     * if the {@link GatewayDiscordClient}'s connection was dropped.
     * 
     * @param event The {@link GuildCreateEvent} that was fired.
     */
    private void distributeClient(GuildCreateEvent event) {
        Snowflake flake = event.getGuild().getId();
        clients.computeIfAbsent(flake, client -> {
            ClientContext context = new ClientContext(dispatcher, event.getGuild(), discordClient.getRestClient());
            context.addManagers(managers.toArray(new GlobalSoapManager<?>[managers.size()]));
            return new SoapClient(context);
        }); //Creates a new SoapClient if one does not already exist for the Guild
        clients.get(flake).onGuildCreate(event); //Distributes the event to the client
    }

    /**
     * Enables test mode for all SoapClients. If test mode is enabled, all SoapClients will
     * only respond to commands via a {@link MessageCreateEvent} if they begin with an additional {@code !}.
     */
    public void enableTestMode() {
        this.testMode = true;
    }

    /**
     * Returns the {@link UserSettingsManager}.
     * 
     * @return the {@link UserSettingsManager}.
     */
    public UserSettingsManager getUserSettingsManager() {
        return (UserSettingsManager) managers.stream().filter(UserSettingsManager.class::isInstance).findFirst().orElse(null);
    }
}
