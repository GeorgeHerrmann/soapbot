package com.georgster.wizard.input;

import discord4j.core.object.entity.Message;

import com.georgster.wizard.InputWizard;

/**
 * A {@link Message} that is being controlled by an {@link InputListener} for an {@link InputWizard}.
 */
public class WizardMessage {
    private Message message;

    /**
     * Creates a {@link WizardMessage} from the provided {@link Message}.
     * 
     * @param message The Message for this container.
     */
    public WizardMessage(Message message) {
        this.message = message;
    }

    /**
     * Sets the {@link Message} for this container.
     * 
     * @param message The new Message.
     */
    public void setMessage(Message message) {
        this.message = message;
    }
    
    /**
     * Returns the {@link Message} from this container.
     * 
     * @return This container's message.
     */
    public Message getMessage() {
        return message;
    }
}
