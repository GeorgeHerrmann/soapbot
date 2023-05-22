package com.georgster.util.commands.wizard.input;

import com.georgster.util.commands.wizard.WizardState;

/**
 * Interface for classes that listen for user input within an {@link com.georgster.util.commands.wizard.InputWizard}.
 */
public interface UserInputListener {
    /**
     * Prompts the user with the given message and options.
     * The InputState will contain the message and options to be sent to the user.
     * OutputState should call {@link WizardState#setMessage()} and {@link WizardState#end()} to return the response
     * and end the wizard respectively.
     * 
     * @param inputState InputState containing the message and options to be sent to the user.
     * @return OutputState containing the response from the user.
     */
    public WizardState prompt(WizardState inputState);

    /**
     * Edits the current message content to the given string.
     * 
     * @param newContent New content for the current message.
     */
    public void editCurrentMessageContent(String newContent);
}
