package com.georgster.coinfactory;

import java.util.List;

import com.georgster.Command;
import com.georgster.coinfactory.wizard.CoinFactoryWizard;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.permissions.PermissibleAction;

import discord4j.discordjson.json.ApplicationCommandRequest;

import com.georgster.coinfactory.model.CoinFactory;

/**
 * A {@link Command} for the {@link CoinFactory}.
 */
public final class CoinFactoryCommand implements Command {
    
    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        CoinFactoryWizard wizard = new CoinFactoryWizard(event);
        wizard.begin();
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
