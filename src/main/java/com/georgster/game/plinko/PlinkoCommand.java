package com.georgster.game.plinko;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.SubcommandSystem;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the actions following the execution of a "!plinko" command.
 */
public class PlinkoCommand implements ParseableCommand {

    private static final String PATTERN = "1|R 1|O";

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        final MultiLogger logger = event.getLogger();
        final SubcommandSystem subcommands = event.createSubcommandSystem();

        PlinkoGame game = new PlinkoGame(event); //Creates a PlinkoGame

        subcommands.on(p -> {
            try {
                game.startGame();
            } catch (IllegalStateException e) {
                event.getGuildInteractionHandler().sendText(e.getMessage(), "Plinko");
            }
        }, "play");

        subcommands.on(p -> {
            logger.append("- Showing a blank Plinko Board", LogDestination.NONAPI, LogDestination.API);
            game.showBoard();
        }, "board");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.contains("play")) {
            return PermissibleAction.PLINKOGAME;
        } else {
            return PermissibleAction.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new CommandParser(PATTERN);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("plinko");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Play a game of Plinko!")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("command")
                        .description("Play a game of Plinko!")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("play")
                                .value("play")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("board")
                                .value("board")
                                .build())
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- '!plinko play' to simulate a game of plinko" +
        "\n- '!plinko board' to show an empty plinko board";
    }

}
