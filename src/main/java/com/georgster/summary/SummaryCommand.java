package com.georgster.summary;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.georgster.Command;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command for generating a summary of recent messages in a Discord text channel.
 * Retrieves the last 50 messages (excluding bots and system messages) and uses
 * OpenAI to create a concise one-paragraph recap under 75 words.
 */
public class SummaryCommand implements Command {

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
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();

        logger.append("- Retrieving last 50 messages from channel\n", LogDestination.NONAPI, LogDestination.API);

        try {
            // 1. Get the channel where command was invoked
            Channel channelObj = event.getDiscordEvent().getChannel();
            
            if (!(channelObj instanceof MessageChannel)) {
                handler.sendMessage("Unable to access this channel.", MessageFormatting.ERROR);
                logger.append("- Channel is not a MessageChannel", LogDestination.NONAPI);
                return;
            }
            
            MessageChannel channel = (MessageChannel) channelObj;
            String channelName = channel.getMention();

            // 2. Retrieve last 50 messages
            List<Message> messages = channel.getMessagesBefore(Snowflake.of(Instant.now()))
                .take(50)
                .collectList()
                .block();

            if (messages == null || messages.isEmpty()) {
                handler.sendMessage("No messages to summarize in this channel.", MessageFormatting.INFO);
                logger.append("- No messages found in channel", LogDestination.NONAPI);
                return;
            }

            // 3. Filter to user messages only (exclude bots and system messages)
            List<String> userMessageContents = messages.stream()
                .filter(msg -> msg.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(msg -> msg.getType() == Message.Type.DEFAULT || 
                              msg.getType() == Message.Type.REPLY)
                .map(Message::getContent)
                .collect(Collectors.toList());

            if (userMessageContents.isEmpty()) {
                handler.sendMessage("No messages to summarize in this channel.", MessageFormatting.INFO);
                logger.append("- All messages filtered out (bots/system messages)", LogDestination.NONAPI);
                return;
            }

            // 4. Generate AI summary
            logger.append("- Sending " + userMessageContents.size() + " messages to OpenAI for summarization\n", 
                         LogDestination.NONAPI, LogDestination.API);
            
            String summary = manager.createSummaryCompletion(userMessageContents, channelName);

            // 5. Post summary to channel
            handler.sendPlainMessage(summary);
            logger.append("- Successfully generated and posted summary", LogDestination.NONAPI);

        } catch (RuntimeException e) {
            handler.sendMessage("Sorry, I couldn't process this in time. Please try again.", MessageFormatting.ERROR);
            logger.append("- Summary generation failed: " + e.getMessage(), LogDestination.NONAPI);
            e.printStackTrace();
        }
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
               "\n- !summary - Generate a summary of the last 100 messages in this channel";
    }

    /**
     * {@inheritDoc}
     * 
     * @return PermissibleAction.SUMMARYCOMMAND
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.SUMMARYCOMMAND;
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
                .description("Generate a summary of the last 100 messages in this channel")
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
