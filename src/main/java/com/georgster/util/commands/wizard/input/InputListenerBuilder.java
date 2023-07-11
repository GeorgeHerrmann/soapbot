package com.georgster.util.commands.wizard.input;

import com.georgster.util.commands.wizard.WizardState;

/**
 * Builder for {@link InputListener}s.
 */
public class InputListenerBuilder {
    private InputListener listener; // Listener being built

    /**
     * Begins building the provided {@link InputListener}.
     * 
     * @param listener The listener to build.
     */
    public InputListenerBuilder(InputListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the condition of the match condition and how strict it is.
     * <p>
     * If mustMatch is off, any input will be accepted. Otherwise, if strict mode is on,
     * only inputs matching the input options will be accepted. If strict mode is off,
     * This Listener will only accept inputs matching one of the given options when calling {@link #prompt(WizardState)},
     * except where the user essentially has no true choices, such as in the following cases:
     * <ul>
     * <li>There are no options</li>
     * <li>There is only one option</li>
     * <li>There are two options, but one is the "back" option</li>
     * </ul>
     * 
     * @param setting Whether mustMatch is on or not.
     * @param strict True if mustMatch should be strict, false if lenient
     */
    public InputListenerBuilder requireMatch(boolean setting, boolean strict) {
        listener.mustMatch(setting, strict);
        return this;
    }

    /**
     * Sets whether this listener should include the ❌ reaction for the user
     * to be able to stop the listener and end the {@link WizardState}.
     * 
     * @param setting True if the ❌ should be included, false otherwise.
     */
    public InputListenerBuilder withXReaction(boolean setting) {
        listener.hasXReaction(setting);
        return this;
    }

    /**
     * Sets the duration before this listener times out (in ms).
     * 
     * @param ms The duration in ms before this listener times out.
     */
    public InputListenerBuilder withTimeoutDuration(int timeout) {
        listener.setTimeout(timeout);
        return this;
    }

    /**
     * Builds the {@link InputListener}.
     * 
     * @return The resulting {@link InputListener}.
     */
    public InputListener build() {
        return listener;
    }
}
