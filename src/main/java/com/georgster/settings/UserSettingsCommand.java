package com.georgster.settings;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.wizard.UserSettingsWizard;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A {@link Command} for interacting with a user's {@link UserSettings}.
 */
public final class UserSettingsCommand implements Command {
    
    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        event.getLogger().append("- Opening The User Settings Wizard\n", LogDestination.NONAPI, LogDestination.API);
        new UserSettingsWizard(event).begin();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("settings", "set");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- '!settings' to manage your personal SOAP Bot settings.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Manage your personal SOAP Bot settings")
                .build();
    }

}
