package com.georgster.wizard;

import java.util.Optional;

import com.georgster.util.DateTimed;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

/**
 * A {@link DateTimed} {@link User} response from an InputListener to an InputWizard.
 */
public class WizardResponse extends DateTimed {
    private final User responder;
    private final String response;
    private final String notes;
    private Optional<Message> message;

    /**
     * Creates a new WizardResponse with no notes for the provided User and their response.
     * 
     * @param responser The {@link User} who responded.
     * @param response The User's response.
     */
    protected WizardResponse(User responser, String response) {
        this.responder = responser;
        this.response = response;
        this.notes = "";
        this.message = Optional.empty();
    }

    /**
     * Creates a new WizardResponse with the provided notes for the provided User and their response.
     * 
     * @param responser The {@link User} who responded.
     * @param response The User's response.
     * @param notes The notes detailing the response from the InputListener.
     */
    protected WizardResponse(User responser, String response, String notes) {
        this.responder = responser;
        this.response = response;
        this.notes = notes;
        this.message = Optional.empty();
    }

    /**
     * Returns the {@link User} who responded to an InputListener's prompt.
     * 
     * @return The {@link User} who responded to an InputListener's prompt.
     */
    public User getResponder() {
        return responder;
    }

    /**
     * Returns the String-based response from the responder.
     * 
     * @return The response from the responder.
     */
    public String getResponse() {
        return response;
    }

    /**
     * Returns any notes provided by an InputListener about the responder's response.
     * <p>
     * If no notes were documented, an empty String is returned.
     * 
     * @return Any notes from an InputListener about a response, or an empty String if none were given.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Returns the {@link Message} that the user responded with, or an empty Optional if no message was provided.
     * <p>
     * Note this will only be present for an {@link com.georgster.wizard.input.InputListener InputListener}
     * which recorded a user's response via a unique {@link Message}.
     * 
     * @return The {@link Message} that the user responded with, or an empty Optional if no message was provided.
     */
    public Optional<Message> getMessageOptional() {
        return message;
    }

    /**
     * Sets the {@link Message} that the user responded with.
     * <p>
     * Note this will only be present for an {@link com.georgster.wizard.input.InputListener InputListener}
     * which recorded a user's response via a unique {@link Message}.
     * 
     * @param message The {@link Message} that the user responded with.
     */
    public void setMessage(Message message) {
        this.message = Optional.of(message);
    }

    /**
     * Returns the {@link Message} that the user responded with.
     * <p>
     * Note this will only be present for an {@link com.georgster.wizard.input.InputListener InputListener}
     * which recorded a user's response via a unique {@link Message}.
     * 
     * @return The {@link Message} that the user responded with.
     * @throws IllegalStateException if the message is not present
     */
    public Message getMessage() {
        return message.orElseThrow(() -> new IllegalStateException("Message is not present"));
    }
}
