package com.georgster.wizard.input;

import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.WizardState;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;

/**
 * Sends a message containing the provided options to the user in a {@code Message}.
 * Users can respond by sending a message.
 * <p>
 * This listener will timeout after 30s of inactivity following
 * {@link #prompt(WizardState)} being called.
 * <p>
 * By default, this listener follows {@link InputListener}'s lenient matching rules,
 * and has {@link InputListener#hasXReaction(boolean)} enabled.
 */
public class MessageListener extends InputListener {

    /**
     * Creates a new {@code MessageListener} with the given parameters.
     * 
     * @param event The event that prompted this listener's creation.
     * @param title The title to place onto messages.
     */
    public MessageListener(CommandExecutionEvent event, String title) {
        super(event, title, "end");
        hasXReaction(true);
        mustMatch(true, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the user sends an attachment with their message, the attachment's URL will be used as the response.
     */
    public WizardState prompt(WizardState inputState) {
        StringBuilder prompt = new StringBuilder(inputState.getMessage());
        String[] options = inputState.getOptions();

        if (options.length > 0) {
            prompt.append("\nYour options are: " + String.join(", ", options));
        }
        inputState.getEmbed().ifPresentOrElse(this::sendPromptMessage,
        () -> sendPromptMessage(prompt.toString()));

        createListener(dispatcher -> dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getChannelId().equals(message.getMessage().getChannelId()))
            .subscribe(event -> {
                List<Attachment> attachments = event.getMessage().getAttachments();
                if (attachments.isEmpty()) {
                    setResponse(event.getMessage().getContent(), event.getMessage().getAuthor().orElse(user));
                } else {
                    setResponse(attachments.get(0).getUrl(), event.getMessage().getAuthor().orElse(user));
                }
                setResponseMessage(event.getMessage());
            }));
            
        return waitForResponse(inputState);
    }
}
