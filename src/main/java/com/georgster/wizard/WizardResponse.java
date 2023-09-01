package com.georgster.wizard;

import com.georgster.util.DateTimed;

import discord4j.core.object.entity.User;

/**
 * A {@link DateTimed} {@link User} response from an InputListener to an InputWizard.
 */
public class WizardResponse extends DateTimed {
    private final User responder;
    private final String response;
    private final String notes;

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
}
