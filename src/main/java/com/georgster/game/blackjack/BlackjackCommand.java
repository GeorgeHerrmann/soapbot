package com.georgster.game.blackjack;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.economy.CoinBank;
import com.georgster.game.DiscordGame;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class BlackjackCommand implements ParseableCommand {

    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();
        
        long wagerAmount = Long.parseLong(event.getCommandParser().get(0));

        DiscordGame game = new BlackJackGame(event, wagerAmount);

        CoinBank bank = game.getOwnerProfile().getBank();

        if (wagerAmount < 50) {
            handler.sendText("You must wager at least *50 coins* in Blackjack", "Blackjack");
            logger.append("- Wager of " + wagerAmount + " less than minimum of 50 coins\n" + LogDestination.NONAPI);
        } else {
            if (bank.hasBalance(wagerAmount)) {
                try {
                    game.startGame();
                } catch (IllegalStateException e) {
                    event.getGuildInteractionHandler().sendText(e.getMessage(), "Plinko");
                }
            } else {
                handler.sendText("You only have " + bank.getBalance() + " coins, wager of " + wagerAmount + " not placed.", "Blackjack");
                logger.append("- " + game.getOwner().getTag() + " has " + bank.getBalance() + " coins. Failed to place wager of " + wagerAmount + "\n", LogDestination.NONAPI);
            }
        }
    }

    public List<String> getAliases() {
        return List.of("blackjack", "bj");
    }

    public CommandParser getCommandParser() {
        return new CommandParser("1|R");
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
