package com.georgster.coinfactory;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.coinfactory.wizard.CoinFactoryWizard;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.manager.UserSettingsManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.permissions.PermissibleAction;
import com.georgster.settings.UserSettings;
import com.georgster.util.DiscordEvent;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.discordjson.json.ApplicationCommandRequest;

import com.georgster.coinfactory.model.CoinFactory;

/**
 * A {@link Command} for the {@link CoinFactory}.
 */
public final class CoinFactoryCommand implements ParseableCommand {

    private final UserProfileManager manager;
    private final UserSettingsManager settingsManager;
    
    /**
     * Constructs a new CoinFactoryCommand with the given {@link ClientContext}.
     * 
     * @param context The {@link ClientContext} of the originating SOAPClient.
     */
    public CoinFactoryCommand(ClientContext context) {
        this.manager = context.getUserProfileManager();
        this.settingsManager = context.getUserSettingsManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        SubcommandSystem sb = event.createSubcommandSystem();
        DiscordEvent discordEvent = event.getDiscordEvent();
        UserSettings settings = settingsManager.get(discordEvent.getAuthorAsMember().getId().asString());
        CoinFactory factory = manager.get(discordEvent.getAuthorAsMember().getId().asString()).getFactory();

        sb.on(() -> new CoinFactoryWizard(event).begin()); // !factory

        sb.on(p -> {
            handler.sendMessage(factory.getDetailEmbed(manager, settings));
        }, "stats", "info", "i"); // !factory stats
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !factory - Opens the Coin Factory wizard.";
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("factory", "coinfactory", "cf", "coinf");
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new CommandParser("VO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.COINFACTORYCOMMAND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Interact with your CoinFactory")
                .build();
    }

}
