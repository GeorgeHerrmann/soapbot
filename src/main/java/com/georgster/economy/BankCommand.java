package com.georgster.economy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.georgster.Command;
import com.georgster.ParseableCommand;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.profile.UserProfile;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.IterableStringWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A {@link Command} for interacting with a user's CoinBank.
 */
public class BankCommand implements ParseableCommand {

    private final UserProfileManager manager;
    
    /**
     * Creates a new BankCommand from the provided context.
     * 
     * @param context The context for this command's SOAPClient.
     */
    public BankCommand(ClientContext context) {
        this.manager = context.getUserProfileManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        SubcommandSystem subcommands = event.createSubcommandSystem();

        subcommands.on(p -> {
            List<UserProfile> profiles = manager.getAll();
            StringBuilder sb = new StringBuilder();

            // Sort profiles based on balance in descending order
            Collections.sort(profiles, Comparator.comparingLong(profile -> -profile.getBank().getBalance()));
            event.getLogger().append("- Sorting " + handler.getGuild().getName() + "'s profiles by CoinBank balance\n", LogDestination.NONAPI);
            event.getLogger().append("Displaying a guild's balance leaderboard" + LogDestination.API);

            // Build the formatted string
            for (int i = 0; i < profiles.size(); i++) {
                UserProfile profile = profiles.get(i);
                sb.append(String.format("**%d) %s** : *%d*%n", i + 1, profile.getUsername(), profile.getBank().getBalance()));
            }
            List<String> leaderboard = SoapUtility.splitAtEvery(sb.toString(), 5);
            InputWizard wizard = new IterableStringWizard(event, handler.getGuild().getName() + "'s Coin Leaderboard", leaderboard);
            wizard.begin();
        }, "leaderboard", "lb");

        subcommands.on(() -> {
            Member member = event.getDiscordEvent().getAuthorAsMember();
            UserProfile profile = manager.get(member.getId().asString());
            event.getGuildInteractionHandler().sendText("You have **" + profile.getBank().getBalance() + "** coins", member.getUsername() + "'s bank");
            event.getLogger().append("- Displaying a user's coin balance", LogDestination.NONAPI, LogDestination.API);
        });
    }

    public CommandParser getCommandParser() {
        return new ParseBuilder("1O").withIdentifiers("leaderboard", "lb").build();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("bank", "coins");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- '!bank bal' to view your coin balance" +
        "\n- '!bank leaderboard' to view this server's coin leaderboard" +
        "\n*This feature is currently in beta*";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Show your Coin Bank information")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("option")
                        .description("View balance or leaderboard")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                            .name("leaderboard")
                            .value("leaderboard")
                            .build())
                        .required(false)
                        .build())
                .build();
    }
}
