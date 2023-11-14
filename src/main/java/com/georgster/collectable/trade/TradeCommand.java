package com.georgster.collectable.trade;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.DiscordEvent;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.handler.InteractionHandler;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.TradableWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A {@link ParseableCommand} that handles all trading related commands.
 */
public final class TradeCommand implements ParseableCommand {

    private final UserProfileManager manager;

    /**
     * Constructs a {@link TradeCommand} for the given {@link ClientContext}.
     * 
     * @param context the context to construct for
     */
    public TradeCommand(ClientContext context) {
        this.manager = context.getUserProfileManager();
    }
    
    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        InteractionHandler handler = event.getGuildInteractionHandler();
        DiscordEvent discordEvent = event.getDiscordEvent();

        List<User> users = discordEvent.getPresentUsers();

        if (users.isEmpty()) {
            handler.sendMessage("No users found, please include a user mention or tag to trade with", "Error", InteractionHandler.MessageFormatting.ERROR);
            logger.append("\n- No users found, sending help message", LogDestination.NONAPI);
        } else {
            logger.append("\n- Found User: " + users.get(0).getTag() + ", sending trade request", LogDestination.NONAPI);
            InputWizard wizard = new TradableWizard(event, manager.get(discordEvent.getUser().getId().asString()), manager.get(users.get(0).getId().asString()));
            wizard.begin();
        }
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new CommandParser("1R");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n - !trade @[USER]" +
        "\n\t- Examples: !trade @georgster#0" +
        "\n*Note: You should either mention the user, or include their direct Username#Discriminator in the command, or use the slash command*";
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("trade", "tr");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.TRADECOMMAND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Trade with another user")
                .addOption(ApplicationCommandOptionData.builder()
                    .name("user")
                    .description("The user to trade with")
                    .type(ApplicationCommandOption.Type.USER.getValue())
                    .required(true)
                    .build())
                .build();
    }

}
