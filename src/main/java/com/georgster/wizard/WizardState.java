package com.georgster.wizard;

import discord4j.core.object.entity.User;

/**
 * A bridge between the {@link InputWizard} and the {@link com.georgster.wizard.input.InputListener InputListener}.
 */
public class WizardState {
    private boolean hasEnded;
    private String message;
    private String notes;
    private String[] options;
    private User user;

    /**
     * Creates a new WizardState with the given message and options.
     * 
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    protected WizardState(String message, User user, String... options) {
        this.hasEnded = false;
        this.message = message;
        this.options = options;
        this.user = user;
        this.notes = "";
    }

    /**
     * Returns whether the wizard has ended.
     * 
     * @return Whether the wizard has ended.
     */
    public boolean hasEnded() {
        return hasEnded;
    }

    /**
     * Returns the message to be sent to the user.
     * 
     * @return Message to be sent to the user.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the option at the given index.
     * 
     * @param index Index of the option to return.
     * @return Option at the given index.
     */
    public String getOption(int index) {
        return options[index];
    }

    /**
     * Sets the response of the user.
     * 
     * @param message Message to be sent to the user.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Overwrites and sets the notes about the state of an {@link InputWizard}
     * or a response from an {@link com.georgster.wizard.input.InputListener InputListener}.
     * 
     * @param notes The new notes.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Adds a new line, followed by the {@code note} about the state of an {@link InputWizard}
     * or a response from an {@link com.georgster.wizard.input.InputListener InputListener} to the current notes.
     * 
     * @param notes The new note.
     */
    public void addNote(String note) {
        this.notes += "\n" + note;
    }

    /**
     * Returns any notes about the state of an {@link InputWizard}
     * or a response from an {@link com.georgster.wizard.input.InputListener InputListener}
     * 
     * @return This state's notes.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Returns the options to be sent to the user.
     * 
     * @return Options to be sent to the user.
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * Ends the wizard.
     */
    public void end() {
        hasEnded = true;
    }

    /**
     * Returns the {@link User} who was prompted or responded in a listener or wizard.
     * 
     * @return The {@link User} who was prompted or responded in a listener or wizard.
     */
    public User getRecentUser() {
        return user;
    }

    /**
     * Sets the {@link User} who was prompted or responded in a listener or wizard.
     * 
     * @param user The {@link User} who was prompted or responded in a listener or wizard.
     */
    public void setUser(User user) {
        this.user = user;
    }
}
