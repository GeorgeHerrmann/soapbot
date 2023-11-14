package com.georgster.wizard.input;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.WizardState;

import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.User;

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
     * @return This Builder
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
     * @return This Builder
     */
    public InputListenerBuilder withXReaction(boolean setting) {
        listener.hasXReaction(setting);
        return this;
    }

    /**
     * If true, all {@link User Users} will be allowed to record responses to this listener.
     * If false, only the primary interacting User (owner) of this Listener can respond.
     * <p>
     * If no member was set with {@link #setInteractingMember(User)}, the primary interacting
     * member is always the author of the {@link CommandExecutionEvent} that created this listener.
     * 
     * @param setting True if all users can respond, false if only the primary interacting member can.
     * @return This Builder
     */
    public InputListenerBuilder allowAllResponses(boolean setting) {
        listener.allowAllUsers(setting);
        return this;
    }

    /**
     * Sets whether this listener should send prompt messages on
     * {@link #sendPromptMessage(String, LayoutComponent...)}. If disabled, this listener
     * will NOT send any prompt message on {@link #prompt(WizardState)}, but will still listen to
     * and record responses in accordance to all other listener settings. This is always enabled
     * by default and is <b>not</b> reccomended to be disabled for most listeners.
     * 
     * @param setting True if prompt messages should be sent, false otherwise.
     * @return This Builder
     */
    public InputListenerBuilder withPromptMessages(boolean setting) {
        listener.sendPromptMessage(setting);
        return this;
    }

    /**
     * If true, all Discord API call tasks will be placed on a {@code General Task Thread Pool}, if false, all tasks will be on one thread.
     * 
     * @param setting If true, api call tasks will be placed on a separate thread, if false, all tasks will be on one thread.
     * @return This Builder
     */
    public InputListenerBuilder withApiCallsOnSeparateThread(boolean setting) {
        listener.apiCallsOnSeparateThread(setting);
        return this;
    }

    /**
     * Sets the duration before this listener times out (in ms).
     * 
     * @param ms The duration in ms before this listener times out.
     * @return This Builder
     */
    public InputListenerBuilder withTimeoutDuration(int ms) {
        listener.setTimeout(ms);
        return this;
    }

    /**
     * Disables auto formatting of the responses to lowercase.
     * 
     * @return This Builder
     */
    public InputListenerBuilder disableAutoFormatting() {
        listener.autoFormat(false);
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
