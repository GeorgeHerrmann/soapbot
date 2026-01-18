package com.georgster.summary;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command for generating a summary of recent messages in a Discord text channel.
 * Retrieves the last 50 messages (excluding bots and system messages) and uses
 * OpenAI to create a concise one-paragraph recap under 75 words.
 */
public class SummaryCommand implements ParseableCommand {

    private final UserProfileManager manager;

    /**
     * Creates a SummaryCommand from the given context.
     * 
     * @param context The context of the SoapClient for this command.
     */
    public SummaryCommand(ClientContext context) {
        this.manager = context.getUserProfileManager();
    }

    /**
     * {@inheritDoc}
     * 
     * Retrieves last 50 messages from the channel, filters to user messages only,
     * generates AI summary, and posts result to channel.
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        // Implementation will be added in Phase 3
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @return "V" - No arguments required (though we implement ParseableCommand for consistency)
     */
    @Override
    public CommandParser getCommandParser() {
        return new CommandParser("V");
    }

    /**
     * {@inheritDoc}
     * 
     * @return List of aliases: ["summary", "summarize", "recap"]
     */
    @Override
    public List<String> getAliases() {
        return List.of("summary", "summarize", "recap");
    }

    /**
     * {@inheritDoc}
     * 
     * @return Help text showing command usage
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
               "\n- !summary - Generate a summary of the last 50 messages in this channel";
    }

    /**
     * {@inheritDoc}
     * 
     * @return PermissibleAction.SUMMARYCOMMAND
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (!args.isEmpty()) {
            return PermissibleAction.SUMMARYCOMMAND;
        }
        return PermissibleAction.DEFAULT;
    }

    /**
     * {@inheritDoc}
     * 
     * @return ApplicationCommandRequest for /summary slash command
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Generate a summary of the last 50 messages in this channel")
                .build();
    }

    /**
     * {@inheritDoc}
     * 
     * @return true (command may take >3 seconds, requires deferral)
     */
    @Override
    public boolean shouldDefer() {
        return true;
    }
}
