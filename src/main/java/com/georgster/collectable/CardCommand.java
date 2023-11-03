package com.georgster.collectable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.MultiLogger;
import com.georgster.logs.LogDestination;
import com.georgster.permissions.PermissibleAction;
import com.georgster.profile.UserProfile;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler;
import com.georgster.wizard.AlternateWizard;
import com.georgster.wizard.CollectableViewWizard;
import com.georgster.wizard.CollectableWizard;
import com.georgster.wizard.CollectectedMarketWizard;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.IterableStringWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A {@link ParseableCommand} that handles all trading card related commands.
 */
public final class CardCommand implements ParseableCommand {

    private final CollectableManager collectableManager;

    /**
     * Constructs a {@link CardCommand} for the given {@link ClientContext}.
     * 
     * @param context the context to construct for
     */
    public CardCommand(ClientContext context) {
        this.collectableManager = context.getCollectableManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();
        SubcommandSystem sb = event.createSubcommandSystem();

        sb.on(p -> {
            logger.append("- Beginning the Create Collectable Wizard\n", LogDestination.NONAPI);
            new CollectableWizard(event).begin();
        }, "create");

        sb.on(p -> {
            if (collectableManager.isEmpty()) {
                handler.sendMessage("There are no trading cards to view", "Error", InteractionHandler.MessageFormatting.ERROR);
                logger.append("\n- No trading cards found, sending help message", LogDestination.NONAPI);
            } else {
                logger.append("\n- Beginning the View Collectable Wizard\n", LogDestination.NONAPI);
                InputWizard wizard1 = new CollectableViewWizard(event, true);
                InputWizard wizard2 = new CollectableViewWizard(event, false);
                AlternateWizard wizard = new AlternateWizard(event, wizard1, wizard2, false);
                wizard.begin();
            }
        }, "view");

        sb.on(() -> {
            if (collectableManager.isEmpty()) {
                handler.sendMessage("There are no trading cards to view", "Error", InteractionHandler.MessageFormatting.ERROR);
                logger.append("\n- No trading cards found, sending help message", LogDestination.NONAPI);
            } else {
                logger.append("\n- Beginning the View Collectable Wizard\n", LogDestination.NONAPI);
                InputWizard wizard1 = new CollectableViewWizard(event, true);
                InputWizard wizard2 = new CollectableViewWizard(event, false);
                AlternateWizard wizard = new AlternateWizard(event, wizard1, wizard2, false);
                wizard.begin();
            }
        });

        sb.on(p -> {
            logger.append("\n- Beginning the Collected Market Wizard\n", LogDestination.NONAPI);
            InputWizard wizard = new CollectectedMarketWizard(event);
            wizard.begin();
        }, "market", "marketplace", "shop", "store");

        sb.on(p -> {
            logger.append(" - Showing a user's own cards\n", LogDestination.NONAPI);
            UserProfile profile = event.getUserProfileManager().get(event.getDiscordEvent().getAuthorAsMember().getId().asString());
            new CollectableViewWizard(event, false).begin("viewMemberCards", profile, 0);
        }, "mine", "my", "self");

        sb.on(p -> {
            if (collectableManager.isEmpty()) {
                handler.sendMessage("There are no trading cards to view", "Error", InteractionHandler.MessageFormatting.ERROR);
                logger.append("\n- No trading cards found, sending help message", LogDestination.NONAPI);
            } else {
                List<Collectable> cards = collectableManager.getAll();
                StringBuilder lb = new StringBuilder();

                // Sort cards by cost in descending order
                Collections.sort(cards, Comparator.comparingLong(card -> -card.getCost()));
                event.getLogger().append("- Sorting " + handler.getGuild().getName() + "'s cards by cost\n", LogDestination.NONAPI);
                event.getLogger().append("Displaying a guild's card leaderboard" + LogDestination.API);

                // Build the formatted string
                for (int i = 0; i < cards.size(); i++) {
                    Collectable card = cards.get(i);
                    lb.append(String.format("**%d) %s** : *%d*%n", i + 1, card.getName(), card.getCost()));
                }
                List<String> leaderboard = SoapUtility.splitAtEvery(lb.toString(), 5);

                InputWizard wizard1 = new IterableStringWizard(event, handler.getGuild().getName() + "'s Card Leaderboard", leaderboard);
                InputWizard wizard2 = new CollectableViewWizard(event, "viewAllRankedCollecteds", 0);
                InputWizard wizard = new AlternateWizard(event, wizard1, wizard2, true);
                wizard.begin();
            }
        }, "lb", "leaderboard");

        sb.onIndexLast(arg -> {
            logger.append("\n - Attempting to find a user with the name " + arg + "\n", LogDestination.NONAPI);
            UserProfile profile = event.getUserProfileManager().get(event.getDiscordEvent().getPresentUsers().get(0).getId().asString());
            logger.append(" - Showing " + profile.getUsername() + "'s cards\n", LogDestination.NONAPI);
            new CollectableViewWizard(event, false).begin("viewMemberCards", profile, 0);
        }, 0);

        sb.onIndexLast(id -> {
            if (!collectableManager.exists(id)) {
                logger.append("\n - No trading card with the name " + id + ", attempting lookup by ID", LogDestination.NONAPI);
                Collected c = collectableManager.getCollectedById(id);
                if (c == null) {
                    logger.append("\n - No trading card with the ID " + id + ", sending help message", LogDestination.NONAPI);
                    handler.sendMessage("A trading card with that ID or name does not exist inside of " + event.getGuildInteractionHandler().getGuild().getName(), "Card not found");
                } else {
                    logger.append("\n - Found trading card with the ID " + id + ", beginning view wizard", LogDestination.NONAPI);
                    InputWizard wizard = new CollectableViewWizard(event, false);
                    wizard.begin("viewCollected", c);
                }
            } else {
                logger.append("\n - Found trading card with the name " + id + ", beginning view wizard", LogDestination.NONAPI);
                Collectable collectable = collectableManager.get(id);
                InputWizard wizard = new CollectableViewWizard(event, false);
                wizard.begin("viewCollectable", collectable);
            }
        }, 0);

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
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n - '!cards' or '!cards view' to view and/or manage trading cards" +
        "\n - '!cards create' to create a new trading card" +
        "\n - '!cards market to open the card marketplace" +
        "\n - '!cards [ID] to view an individual trading card" +
        "\n - '!cards [NAME]' to view a trading card by name" +
        "\n - '!cards mine' to view your own trading cards" +
        "\n - '!cards leaderboard' or '!cards lb' to view the leaderboard of trading cards" +
        "\n - '!cards @[USER]' to view another user's trading cards" +
        "\n - '!trade @[USER]' to trade with another user" +
        "\n - Visit https://tinyurl.com/soapbotcards for more information";
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("cards", "card");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.CARDCOMMAND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Interact with trading cards")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("id, name or 'mine'")
                        .description("Either a trading card ID, name to lookup, or 'mine' for personal cards")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("A Member's card collection to view")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("option")
                        .description("Subcommands for handling trading cards")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("create")
                                .value("create")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("view")
                                .value("view")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("market")
                                .value("market")
                                .build())
                        .build())
                .build();
    }

}
