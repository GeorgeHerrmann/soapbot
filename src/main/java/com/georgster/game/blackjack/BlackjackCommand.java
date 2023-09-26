package com.georgster.game.blackjack;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.economy.CoinBank;
import com.georgster.game.DiscordGame;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A {@link ParseableCommand} to interact with the BlackJack {@link DiscordGame}.
 */
public class BlackjackCommand implements ParseableCommand {

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();
        
        long wagerAmount = Long.parseLong(event.getParsedArguments().get(0));

        DiscordGame game = new BlackJackGame(event, wagerAmount);

        CoinBank bank = game.getOwnerProfile().getBank();

        if (wagerAmount < 0) {
            handler.sendMessage("You must wager at least *0 coins* in Blackjack", "Blackjack", MessageFormatting.ERROR);
            logger.append("- Wager of " + wagerAmount + " less than minimum of 0 coins\n" + LogDestination.NONAPI);
        } else {
            if (bank.hasBalance(wagerAmount)) {
                try {
                    game.startGame();
                } catch (IllegalStateException e) {
                    event.getGuildInteractionHandler().sendMessage(e.getMessage(), "Blackjack", MessageFormatting.ERROR);
                }
            } else {
                handler.sendMessage("You only have " + bank.getBalance() + " coins, wager of " + wagerAmount + " not placed.", "Blackjack", MessageFormatting.ERROR);
                logger.append("- " + game.getOwner().getTag() + " has " + bank.getBalance() + " coins. Failed to place wager of " + wagerAmount + "\n", LogDestination.NONAPI);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("blackjack", "bj");
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new ParseBuilder("1R").withRules("N").build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Play a game of Blackjack")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("wager")
                        .description("The amount to wager on the game")
                        .type(ApplicationCommandOption.Type.NUMBER.getValue())
                        .required(true)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.isEmpty()) {
            return PermissibleAction.DEFAULT;
        } else {
            return PermissibleAction.BLACKJACKGAME;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- '!blackjack [WAGER]' to play a game of Blackjack with the provided wager";
    }
}
