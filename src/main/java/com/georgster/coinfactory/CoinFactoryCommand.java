package com.georgster.coinfactory;

import java.util.List;

import com.georgster.Command;
import com.georgster.ParseableCommand;
import com.georgster.coinfactory.wizard.CoinFactoryWizard;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.manager.UserSettingsManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.settings.UserSettings;
import com.georgster.util.DiscordEvent;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
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
        MultiLogger logger = event.getLogger();

        sb.on(p -> {
            handler.sendMessage(factory.getDetailEmbed(manager, settings));
            logger.append("- Displaying a user's CoinFactory stats", LogDestination.NONAPI, LogDestination.API);
        }, "stats", "mine", "s"); // !factory stats

        sb.on(p -> {
            StringBuilder output = new StringBuilder("**OVERVIEW**\n");
            output.append("- The CoinFactory is an idle upgrade farm game that allows you to passively gain coins.\n");
            output.append("- You can view upgrade tracks which have specific factory upgrades, each with an associated level, cost and effect.\n" +
                            "- Based on your current factory upgrades, your CoinFactory will produce a certain amount of coins each process cycle.\n" +
                            "- You can invest coins into your factory from your CoinBank in order to be able to purchase more upgrades, and withdraw produced coins from your factory into your CoinBank.\n");
            output.append("\n**PROCESS CYCLE AND UPGRADES**\n");
            output.append("- Each process cycle, your coin factory will produce coins based on its upgrades. Upgrades can influence various factors which affect how many coins will be produced.\n" +
                            "- The Coin Factory processes coins with the following algorithm:\n" +
                            "- All upgrades which affect **STARTING PRODUCTION** are processed first, increasing the starting number of coins the factory will work with.\n" +
                            "- Then, the factory will examine **BASE** and **WORKING** values. Any upgrade which influences **BASE** production is additive and does not represent the actual amount of produced coins in a cycle." +
                            " Instead, **WORKING** production upgrades, which are multiplicative, are based off the current **BASE** production value, and the resulting value is added to the current **WORKING** production, which is the actual amount of produced coins in a cycle.\n" +
                            "\t- ***For example:*** An upgrade which increases **STARTING** production by +20, **BASE** production by +20, and **WORKING** production by 1.15x will be processed as follows:\n" +
                            "\t- **STARTING** *production will be increased by +20, then any other upgrades that influence* **STARTING** *production will be processed.*\n" +
                            "\t- *Then, the* **BASE** *production will be increased by +20 and then the* **WORKING** *production will be increased by 40 x (0.15) = 6*\n");
            
            output.append("\n**PRESTIGES**\n");
            output.append("- You can prestige your factory for greater coin production, but the cost of your upgrades will also increase.\n" +
                            "- In order to prestige your factory, you must own ALL available upgrades and have the equivilant of the refund value of ALL upgrades invested in your Factory.\n" +
                            "- Upon prestiging, your owned upgrades will reset, the cost of the prestige will be deducted from your invested coins and your prestige level will increase.\n" +
                            "- The color of your Coin Factory *(the background color of the UI)* will change depending on your prestige level.\n");
            
            output.append("\n*Use* **!factory** *to interact with your Coin Factory, or use* **!help factory** *to view the available subcommands.*");

            handler.sendMessage(output.toString(), "The Coin Factory");
            logger.append("- Diplaying Coin Factory information", LogDestination.NONAPI, LogDestination.API);
        }, "info", "i", "help");

        sb.on(() -> {
            CoinFactoryWizard wizard = new CoinFactoryWizard(event);
            wizard.begin();
            logger.append("- Opening the Coin Factory Wizard", LogDestination.NONAPI, LogDestination.API);
        }); // !factory
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !factory - Interact with your CoinFactory" +
        "\n- !factory stats - Displays the current stats of your Coin Factory." +
        "\n- !factory info - Displays information about how the Coin Factory works";
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
        return new ParseBuilder("VO").withIdentifiers("stats", "mine", "s", "info", "i", "help").build();
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
                .addOption(ApplicationCommandOptionData.builder()
                        .name("option")
                        .description("View Factory stats or info")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("stats")
                            .value("stats")
                            .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("info")
                            .value("info")
                            .build())
                        .required(false)
                        .build())
                .build();
    }

}
