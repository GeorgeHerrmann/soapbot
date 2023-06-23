package com.georgster.gpt;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.ChatCompletionManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.wizard.IterableStringWizard;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command for creating chat completions with OpenAI's ChatGPT.
 */
public class GPTCommand implements ParseableCommand {

    private final ChatCompletionManager manager; //The chat completion manager.

    /**
     * Creates a GPTCommand from the given context.
     * 
     * @param context The context of the SoapClient for this command.
     */
    public GPTCommand(ClientContext context) {
        this.manager = context.getChatCompletionManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();

        String prompt = event.getCommandParser().get(0);
        logger.append("- Sending a chat completion request to OpenAI", LogDestination.NONAPI, LogDestination.API);
        List<String> responses = manager.createCompletionGetAll(prompt, event.getDiscordEvent().getAuthorAsMember());
        
        if (responses.size() == 1) {
            logger.append("- Only one response found, sending it in plain text", LogDestination.NONAPI);
            handler.sendPlainText(responses.get(0));
        } else if (responses.size() > 1) {
            logger.append("- Multiple responses found, starting an iterable wizard to view them", LogDestination.NONAPI);
            IterableStringWizard wizard = new IterableStringWizard(event, "Responses", responses);
            wizard.begin();
        }
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new CommandParser("V|R");
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("ask", "ai", "gpt", "asksoapbot", "askbot", "chatgpt");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
         return "Aliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n- !ask [PROMPT] - Give SOAP Bot a prompt and have it respond";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (!args.isEmpty()) {
            return PermissibleAction.GPTCOMMAND;
        }
        return PermissibleAction.DEFAULT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Ask SOAP Bot a question or give it a prompt.")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("prompt")
                        .description("A prompt to have SOAP Bot respond to")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldDefer() {
        return true;
    }
    
}
