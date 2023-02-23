package com.georgster.test;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Used to test on going features. This command will be considered active if the
 * {@code ACTIVE} field is set to {@code true}.
 */
public class TestCommand implements Command {
    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private static final boolean ACTIVE = true;

    /**
     * {@inheritDoc}
     */
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        if (!ACTIVE) return;

        MultiLogger<TestCommand> logger = new MultiLogger<>(manager, TestCommand.class);

        if (pipeline.getPermissionsManager().hasPermissionSendError(manager, logger, PermissibleAction.ADMIN, pipeline.getAuthorAsMember())) {
            logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);
        }

        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        if (ACTIVE) {
            return List.of("test");
        } else { //if inactive, the registry will not be able to find this command using the aliases
            return List.of();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Description test")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "The test command is used to test on going features.";
    }

}
