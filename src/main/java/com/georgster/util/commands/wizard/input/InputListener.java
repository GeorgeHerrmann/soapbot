package com.georgster.util.commands.wizard.input;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.wizard.WizardState;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

/**
 * Framework for creating an {@link InputListener} which prompts a user and listens
 * for their response. The {@link InputListener} is designed to communicate with a
 * {@link com.georgster.util.commands.wizard.InputWizard} via the {@link WizardState}.
 * <p>
 * Implementations must define {@link #prompt(WizardState)} which is how outside
 * wizard's indicate a need to prompt a user.
 * <p>
 * {@link #prompt(WizardState)} should create {@link Disposable} listeners via {@link #createListener(ListenerFactory)},
 * keeping in mind verification for user, channel and message IDs is not covered automatically,
 * send the prompt message with {@link #sendPromptMessage(String, LayoutComponent...)}, and handle 
 * creating the output {@link WizardState} after listening for a response with {@link #waitForResponse(WizardState)}.
 * 
 * @see com.georgster.util.commands.wizard.InputWizard
 */
public abstract class InputListener {

    private int timeoutTime = 300; // will wait 30s for a response (is in ms)

    protected final String endString;
    protected final String title;
    private final EventDispatcher dispatcher;
    protected final GuildInteractionHandler handler;
    protected final Member user;
    private final List<Disposable> listeners;

    private boolean addXReaction;
    private boolean mustMatchLenient;
    private boolean mustMatchStrict;

    protected Message message;
    private WizardState recentState;
    private StringBuilder responseContainer;

    /**
     * Creates an InputListener from the provided event, title, and String to end the listener.
     * 
     * @param event The event that prompted this listener's creation.
     * @param title The title placed on sent messages.
     * @param endString The String a user can send to end this listener.
     */
    protected InputListener(CommandExecutionEvent event, String title, String endString) {
        this.endString = endString;
        this.title = title;
        this.dispatcher = event.getEventDispatcher();
        this.handler = event.getGuildInteractionHandler();
        this.user = event.getDiscordEvent().getAuthorAsMember();
        this.listeners = new ArrayList<>();
        this.message = null;
        this.addXReaction = true;
        this.mustMatchLenient = true;
        this.mustMatchStrict = false;
        this.timeoutTime = 300;
        this.responseContainer = new StringBuilder();
    }

    /**
     * Adds a new {@link Disposable} listener to this listener.
     * <p>
     * Listeners should verify the correct {@link Member}, {@link Channel}, and {@link Message} is present, if applicable.
     * <p>
     * If the Event that created the listener is a {@link discord4j.core.event.domain.interaction.ComponentInteractionEvent},
     * {@link GuildInteractionHandler#setActiveComponentInteraction(discord4j.core.event.domain.interaction.ComponentInteractionEvent)} should
     * be called with the dispatched event.
     * <p>
     * If a response is given, it can be set into this listener with {@link #setResponse(String)}.
     * 
     * @param listener The {@link Disposable} listener which will listen for some Discord Event.
     */
    protected void createListener(ListenerFactory listener) {
        listeners.add(listener.createListener(dispatcher));
    }

    /**
     * Sends a message containing their prompt as the content and the optional
     * LayoutComponents attached.
     * <p>
     * If options are present, they should be included in the prompt String.
     * 
     * @param prompt The prompt to present the user.
     * @param components The components to attach (optional).
     */
    protected void sendPromptMessage(String prompt, LayoutComponent... components) {
        if (message == null) {
            message = components.length == 0 ? handler.sendText(prompt, title) : handler.sendText(prompt, title, components);
            if (addXReaction) {
                addXEmojiListener();
            }
        } else {
            try {
                message = components.length == 0 ? handler.editMessageContent(message, prompt, title) : handler.editMessageContent(message, prompt, title, components);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Attaches the ❌ emoji as a reaction to this listener's active {@link Message},
     * and adds a listener that ends the most recent {@link WizardState} when reacted by this listener's user.
     */
    private void addXEmojiListener() {
        this.message.addReaction(ReactionEmoji.unicode("❌")).block();
        // Create a listener that listens for the user to end the wizard by reacting
        createListener(eventDispatcher -> eventDispatcher.on(ReactionAddEvent.class)
            .filter(event -> event.getMember().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getEmoji().equals(ReactionEmoji.unicode("❌")))
            .subscribe(event -> recentState.end()));
    }

    /**
     * Creates a listener that ends the most recent {@link WizardState} when this listener's user types the {@link #endString}.
     */
    private void addEndMessageListener() {
        createListener(eventDispatcher -> dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getAuthor().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().getChannelId().equals(message.getChannelId()))
            .filter(event -> event.getMessage().getContent().toLowerCase().equals(endString))
            .subscribe(event -> recentState.end()));
    }

    /**
     * Sets whether this listener should include the ❌ reaction for the user
     * to be able to stop the listener and end the {@link WizardState}.
     * 
     * @param setting True if the ❌ should be included, false otherwise.
     */
    public void hasXReaction(boolean setting) {
        this.addXReaction = setting;
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
     * @param strict True if mustMatch should be strict, false if lenient.ww
     */
    public void mustMatch(boolean setting, boolean strict) {
        if (strict) {
            this.mustMatchLenient = setting;
            this.mustMatchStrict = setting;
        } else {
            this.mustMatchLenient = setting;
            this.mustMatchStrict = false;
        }
    }

    /**
     * Sets the duration before this listener times out (in ms).
     * 
     * @param ms The duration in ms before this listener times out.
     */
    public void setTimeout(int ms) {
        this.timeoutTime = ms;
    }

    /**
     * Edits the current active Message's content to the provided String.
     * 
     * @param newContent The string to replace the current Message's content with.
     */
    public void editCurrentMessageContent(String newContent) {
        message = handler.editMessageContent(message, newContent, title);
    }

    /**
     * Prompts the user given the state of an {@link InputWizard} and
     * returns the new state. The user's response will be present in
     * {@link WizardState#getMessage()}, unless {@link WizardState#hasEnded()} is true,
     * in which case it will be null.
     * 
     * @param inputState The state of an {@link InputWizard}.
     * @return The output state after the prompt.
     */
    public abstract WizardState prompt(WizardState inputState);

    /**
     * Sets the response of this listener, in accordance with its matching rules.
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
     * @param response
     */
    protected void setResponse(String response) {
        List<String> options = List.of(recentState.getOptions());
        response = response.toLowerCase();
        if (!mustMatchLenient && !mustMatchStrict) {
            this.responseContainer.append(response);
            return;
        }
        if (options.contains(response) || ((mustMatchLenient && (options.size() > 2 || (options.contains("back") && options.size() == 2) || options.size() == 1)) || mustMatchStrict)) {
            this.responseContainer.append(response);
        }
    }

    /**
     * Waits for a listener to send a valid response via {@link #setResponse(String)}, for the user to end the listener,
     * or for it to timeout, and processes the output {@link WizardState} accordingly.
     * 
     * @param inputState The input state from an InputWizard.
     * @return The output state of the InputWizard.
     */
    protected WizardState waitForResponse(WizardState inputState) {
        this.recentState = inputState;

        addEndMessageListener();
        int timeout = 0;
        while (responseContainer.isEmpty()) { // Wait for the user to send a message
            try {
                if (recentState.hasEnded() || timeout > timeoutTime) {
                    listeners.forEach(Disposable::dispose);
                    listeners.clear();
                    recentState.end();
                    return recentState;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            timeout ++;
        }
        listeners.forEach(Disposable::dispose);
        listeners.clear();
        recentState.setMessage(responseContainer.toString());

        responseContainer = new StringBuilder();

        return recentState;
    }

    /**
     * Returns a builder to customize this {@link InputListener}.
     * 
     * @return a builder to customize this {@link InputListener}.
     */
    public InputListenerBuilder builder() {
        return new InputListenerBuilder(this);
    }
}
