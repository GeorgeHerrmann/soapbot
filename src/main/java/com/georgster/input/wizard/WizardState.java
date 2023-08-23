package com.georgster.input.wizard;

import discord4j.core.object.entity.Member;

/**
 * A bridge between the {@link InputWizard} and the {@link com.georgster.input.wizard.input.InputListener InputListener}.
 */
public class WizardState {
    private boolean hasEnded;
    private String message;
    private String[] options;
    private Member member;

    /**
     * Creates a new WizardState with the given message and options.
     * 
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    protected WizardState(String message, Member member, String... options) {
        this.hasEnded = false;
        this.message = message;
        this.options = options;
        this.member = member;
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

    public Member getRecentMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
