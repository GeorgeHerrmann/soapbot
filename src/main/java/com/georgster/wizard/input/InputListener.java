package com.georgster.wizard.input;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.WizardState;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

/**
 * Framework for creating an {@link InputListener} which prompts a user and listens
 * for their response. The {@link InputListener} is designed to communicate with a
 * {@link com.georgster.wizard.InputWizard} via the {@link WizardState}.
 * <p>
 * Implementations must define {@link #prompt(WizardState)} which is how outside
 * wizard's indicate a need to prompt a user.
 * <p>
 * {@link #prompt(WizardState)} should create {@link Disposable} listeners via {@link #createListener(ListenerFactory)},
 * keeping in mind verification for user, channel and message IDs is not covered automatically,
 * send the prompt message with {@link #sendPromptMessage(String, LayoutComponent...)}, and handle 
 * creating the output {@link WizardState} after listening for a response with {@link #waitForResponse(WizardState)}.
 * 
 * @see com.georgster.wizard.InputWizard
 */
public abstract class InputListener {

    private int timeoutTime = 300; // will wait 30s for a response (is in ms)

    // Configuration properties
    protected final String endString; // String to type to cancel the listener
    protected String title; // The title to attach to messages
    private final EventDispatcher dispatcher; // Dispatcher sending events
    protected final InteractionHandler handler; // Handler to interact with the Guild
    protected User user; // The initial user of the listener
    private final List<Disposable> listeners; // The Disposable listeners

    // Settings for this listener
    private boolean addXReaction;
    private boolean mustMatchLenient; // If false, mustMatchStrict will always also be false
    private boolean mustMatchStrict; // If true, mustMatchLenient will always also be true
    private boolean sendPromptMessage; // If false (generally not reccomended), sendPromptMessage(String) does nothing
    private boolean allowAllUsers; // If false, only user will be able to respond.
    private boolean apiCallOnSeparateThread; // If true, all API calls will be placed on a seperate general task thread
    private boolean autoFormat; // If true, all responses will be lowercase

    /*
     * InputListeners work on a message to message basis.
     * These properties store the most recent data from the last prompt.
     * If there was no previous message, these will either be empty objects or null.
     */
    protected WizardMessage message; // This listener's most recent message (to edit)
    private WizardState recentState; // The most recent state of the communicating InputWizard
    private StringBuilder responseContainer; // The users most recent response

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
        this.user = event.getDiscordEvent().getUser();
        this.listeners = new ArrayList<>();
        this.message = null;
        this.addXReaction = true;
        this.mustMatchLenient = true;
        this.mustMatchStrict = false;
        this.sendPromptMessage = true;
        this.allowAllUsers = false;
        this.apiCallOnSeparateThread = false;
        this.autoFormat = true;
        this.timeoutTime = 300;
        this.responseContainer = new StringBuilder();
    }

    /**
     * Adds a new {@link Disposable} temporary listener to this InputListener.
     * <p>
     * Listeners should verify the correct {@link discord4j.core.object.entity.channel.Channel Channel}
     * and {@link Message} are present, if applicable, along with any other properties.
     * <p>
     * If the Event that created the listener is a
     * {@link discord4j.core.event.domain.interaction.ComponentInteractionEvent ComponentInteractionEvent},
     * such as a {@link discord4j.core.event.domain.interaction.SelectMenuInteractionEvent SelectMenuInteractionEvent} or a
     * {@link discord4j.core.event.domain.interaction.ButtonInteractionEvent ButtonInteractionEvent},
     * {@link GuildInteractionHandler#setActiveComponentInteraction(discord4j.core.event.domain.interaction.ComponentInteractionEvent) handler.setActiveComponentInteraction(ComponentInteractionEvent)}
     * should be called with the dispatched event to update the GuildInteractionHandler's event interactions.
     * <p>
     * If a response is given, it can be set into this listener with {@link #setResponse(String)}.
     * <p>
     * If a disposable listener should end this InputListener, {@link WizardState#end()} should be called on the input WizardState
     * <p>
     * For example, a listener which records the user's response in a message they send could be created like so:
     * <pre>
     * createListener(dispatcher -> dispatcher.on(MessageCreateEvent.class)
     *      .filter(event -> event.getMessage().getAuthor().get().getId().asString().equals(user.getId().asString()))
     *      .filter(event -> event.getMessage().getChannelId().equals(message.getChannelId()))
     *      .subscribe(event -> setResponse(event.getMessage().getContent())));
     * </pre>
     * 
     * @param listener The ListenerFactory which will create a {@link Disposable} listener to listen for some Discord Event.
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
        if (!sendPromptMessage) return;

        if (message == null || message.getMessage() == null) {
            message = new WizardMessage(components.length == 0 ? handler.sendMessage(prompt, title) : handler.sendMessage(prompt, title, components));
        } else {
            try {
                if (apiCallOnSeparateThread) {
                    ThreadPoolFactory.scheduleGeneralTask(handler.getId(), () -> message.setMessage(components.length == 0 ? handler.editMessage(message.getMessage(), prompt, title) : handler.editMessage(message.getMessage(), prompt, title, components)));
                } else {
                    message.setMessage(components.length == 0 ? handler.editMessage(message.getMessage(), prompt, title) : handler.editMessage(message.getMessage(), prompt, title, components));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (addXReaction) {
            addXEmojiListener();
        }
    }

    /**
     * Attaches the ❌ emoji as a reaction to this listener's active {@link Message},
     * and adds a listener that ends the most recent {@link WizardState} when reacted by this listener's user.
     */
    private void addXEmojiListener() {
        if (recentState == null) {
            this.message.getMessage().addReaction(ReactionEmoji.unicode("❌")).block();
        }
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
            .filter(event -> event.getMessage().getChannelId().equals(message.getMessage().getChannelId()))
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
     * Sets whether this listener should send prompt messages on
     * {@link #sendPromptMessage(String, LayoutComponent...)}. If disabled, this listener
     * will NOT send any prompt message on {@link #prompt(WizardState)}, but will still listen to
     * and record responses in accordance to all other listener settings. This is always enabled
     * by default and is <b>not</b> reccomended to be disabled for most listeners.
     * 
     * @param setting True if prompt messages should be sent, false otherwise.
     */
    public void sendPromptMessage(boolean setting) {
        this.sendPromptMessage = setting;
    }

    /**
     * If true, all {@link User Users} will be allowed to record responses to this listener.
     * If false, only the primary interacting User (owner) of this Listener can respond.
     * <p>
     * If no member was set with {@link #setInteractingMember(User)}, the primary interacting
     * member is always the author of the {@link CommandExecutionEvent} that created this listener.
     * 
     * @param setting True if all users can respond, false if only the primary interacting member can.
     */
    public void allowAllUsers(boolean setting) {
        this.allowAllUsers = setting;
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
     * If true, all Discord API call tasks will be placed on a {@code General Task Thread Pool}, if false, all tasks will be on one thread.
     * 
     * @param setting If true, api call tasks will be placed on a separate thread, if false, all tasks will be on one thread.
     */
    public void apiCallsOnSeparateThread(boolean setting) {
        this.apiCallOnSeparateThread = setting;
    }

    /**
     * If true, all responses will be automatically formatted to lowercase.
     * 
     * @param setting If true, all responses will be automatically formatted to lowercase.
     */
    public void autoFormat(boolean setting) {
        this.autoFormat = setting;
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
        message.setMessage(handler.editMessage(message.getMessage(), newContent, title));
    }

    /**
     * Edits the current active Message's content to the provided String, then delays the
     * calling thread by {@code millis} milliseconds. This is generally used in a pattern
     * where rapid message edits are desired without overloading with Discord API calls.
     * <p>
     * Uses {@link GuildInteractionHandler#editMessageContent(Message, String, String)} for formatting.
     * 
     * @param newContent The new Message {@code content}.
     * @param millis
     */
    public void editCurrentMessageContentDelay(String newContent, long millis) {
        message.setMessage(handler.editMessage(message.getMessage(), newContent, title));
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    /**
     * Sets the title of the current {@link Message} for this Wizard.
     * 
     * @param newTitle The new title for the Message.
     */
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Deletes this listener's current presenting message. Note that
     * this does <b>NOT</b> cancel the listener, and if a prompt is currently active
     * the listener will still listen for and record a response if one is possible.
     */
    public void deleteCurrentMessage() {
        message.getMessage().delete().block();
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
    protected void setResponse(String response, User responder, String... notes) {
        if (!allowAllUsers && !responder.equals(user)) {
            return;
        }
        List<String> options = new ArrayList<>(List.of(recentState.getOptions()));
        
        for (int i = 0; i < options.size(); i++) {
            options.set(i, options.get(i).toLowerCase());
        }

        String fullResponse = response;
        response = response.toLowerCase();

        if (!mustMatchLenient && !mustMatchStrict) {
            this.recentState.setNotes(String.join("\n", notes));
            if (autoFormat) {
                this.responseContainer.append(response);
            } else {
                this.responseContainer.append(fullResponse);
            }
            this.recentState.setUser(responder);
            return;
        }
        if (options.contains(response) || ((mustMatchLenient && (options.size() > 2 || (options.contains("back") && options.size() == 2) || options.size() == 1)) && !mustMatchStrict)) {
            this.recentState.setNotes(String.join("\n", notes));
            if (autoFormat) {
                this.responseContainer.append(response);
            } else {
                this.responseContainer.append(fullResponse);
            }
            this.recentState.setUser(responder);
        }
    }

    /**
     * Adds a note to this Listener's most recent {@link WizardState}.
     */
    protected void addNote(String note) {
        this.recentState.addNote(note);
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
        responseContainer = new StringBuilder();

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

        return recentState;
    }

    /**
     * Sets the current message this listener will work with to present {@link #prompt(WizardState)}simple.
     * 
     * @param message The new message.
     */
    public void setCurrentMessage(Message message) {
        if (this.message != null) {
            this.message.setMessage(message);
        } else {
            this.message = new WizardMessage(message);
        }
    }

    /**
     * Sets the primary interacting {@link User} for this listener.
     * If {@link #allowAllUsers(boolean)} is false, only the interacting {@link User} will
     * be able to respond. This {@link User} is considered the Owner of this listener.
     * 
     * @param user The new primary interacting {@link User} for this listener.
     */
    public void setInteractingMember(User user) {
        this.user = user;
        if (recentState != null) {
            recentState.setUser(user);
        }
    }

    /**
     * Cancels the current {@link #prompt(WizardState)} and ends the {@code WizardState}.
     * If there is no active prompt for this listener, this method does nothing.
     */
    public void cancel() {
        if (recentState != null) {
            recentState.end();
        }
    }

    /**
     * Returns a builder to customize this {@link InputListener}.
     * 
     * @return a builder to customize this {@link InputListener}.
     */
    public InputListenerBuilder builder() {
        return new InputListenerBuilder(this);
    }

    /**
     * Returns the current interacting {@link User} for this listener.
     * 
     * @return The current interacting {@link User} for this listener.
     */
    public User getInteractingUser() {
        return user;
    }

    /**
     * Returns the Title of this listener.
     * 
     * @return The Title of this listener.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the current {@link Message} of this listener.
     * 
     * @return The current {@link Message} of this listener.
     */
    public Message getCurrentMessage() {
        if (message != null) {
            return message.getMessage();
        } else {
            return null;
        }
    }
}
