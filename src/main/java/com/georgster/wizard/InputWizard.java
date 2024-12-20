package com.georgster.wizard;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler;
import com.georgster.util.handler.UserInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

/**
 * Abstract class for creating a wizard that prompts the user for input and records the response.
 * <p>In order to define different states for the wizard, states should be defined in methods which
 * point to eachother with {@link #nextWindow(String, Object...)}, stemming from {@link #begin()}.
 * Responses should be handled with {@link #withResponse(Consumer, Boolean, String, String...)} to define logic for how to handle a user's response.</p>
 * 
 * Implementations can use {@link #sendMessage(String, String)} to send self-expiring messages, or access {@link #handler} for more control
 * over Guild interactions. {@link #user} is the user of the InputWizard.
 * <p>
 * Example implementation:
 * <pre>
 * {@code
 * public void begin() {
 *    nextWindow("pickHeadsOrTails");
 * }
 * 
 * protected void pickHeadsOrTails() {
 *    String prompt = "Pick heads or tails";
 *    String[] options = {"heads", "tails"};
 *    withResponse((response -> {
 *      if (response.equalsIgnoreCase("heads")) {
 *          nextWindow("pickedHeads");
 *      } else {
 *          nextWindow("pickedTails");
 *      }
 *    }), false, prompt, options);
 * }
 * 
 * protected void pickedHeads() {
 *    String prompt = "You picked heads. Pick a number between 1 and 2";
 *    String[] options = {"1", "2"};
 *    withResponse((response -> {
 *        if (response.equals("1")) {
 *            nextWindow("picked1");
 *        } else {
 *            nextWindow("picked2");
 *        }
 *    }), true, prompt, options);
 * }
 * </pre>
 * 
 */
public abstract class InputWizard {
    private Deque<Method> activeFunctions; //Stack of methods that have been or are executing
    private Deque<Object[]> activeFunctionParams; //Stack of parameters for methods that have been or are executing

    protected final CommandExecutionEvent event;
    protected User user;
    private boolean isActive;
    private boolean awaitingResponse;
    protected InteractionHandler handler;
    private InputListener listener; // The default listener for this wizard
    private InputListener currentlyActiveListener; // The listener currently being used by the wizard
    protected final MultiLogger logger;

    private boolean wasShutdown; // A Shutdown wizard will not perform any additional end() activities

    /**
     * Initializes the InputWizard engine.
     * 
     * @param event The event that prompted the Wizard creation.
     * @param listener The {@code InputListener} which will handle the user interaction.
     */
    protected InputWizard(CommandExecutionEvent event, InputListener listener) {
        this.activeFunctions = new ArrayDeque<>();
        this.activeFunctionParams = new ArrayDeque<>();

        this.user = event.getDiscordEvent().getUser();
        this.handler = event.getGuildInteractionHandler();
        this.isActive = true;
        this.awaitingResponse = false;
        this.wasShutdown = false;
        this.listener = listener;
        this.currentlyActiveListener = listener;
        this.logger = event.getLogger();
        this.event = event;
    }

    /**
     * Immediately switches to a new window in this {@code InputWizard}, given
     * the name of a method in the wizard and the parameters required to run it.
     * 
     * @param methodName The name of the method which will run the next window in this {@code InputWizard}.
     * @param parameters The parameters for said method.
     * @throws IllegalArgumentException If a method with the provided name and paramaters was not found.
     */
    protected void nextWindow(String methodName, Object... parameters) throws IllegalArgumentException {
        try {
            activeFunctions.push(getMethod(methodName, parameters));
            activeFunctionParams.push(parameters);

            logger.append("- Switching to window: " + methodName + "\n", LogDestination.NONAPI);

            invokeCurrentMethod();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Returns to the previous window of this {@code InputWizard}, given one exists.
     */
    protected void goBack() {
        logger.append("- Returning to the previous window" + "\n", LogDestination.NONAPI);

        activeFunctions.pop();
        activeFunctionParams.pop();
        invokeCurrentMethod();
    }

    /**
     * Runs the current method for as long as this {@code InputWizard} is active.
     */
    private void invokeCurrentMethod() {
        while (isActive()) {
            try {
                activeFunctions.peek().invoke(this, activeFunctionParams.peek());
            } catch (Exception e) {
                e.printStackTrace();
                logger.append("- An error occurred while running the wizard: " + e.getMessage() + "\n", LogDestination.NONAPI);
                shutdown();
            }
        }
    }

    /**
     * Returns the Method that is in this {@code InputWizard} if one exists.
     * 
     * @param methodName The name of the method.
     * @param parameters The parameters of the method.
     * @return The Method object.
     * @throws NoSuchMethodException If no method exist, or the method is no visible.
     */
    private Method getMethod(String methodName, Object... parameters) throws NoSuchMethodException {
        Class<?>[] paramClasses = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramClasses[i] = parameters[i].getClass();
        }

        try {
            return this.getClass().getDeclaredMethod(methodName, paramClasses);
        } catch (NoSuchMethodException e) {
           throw new NoSuchMethodException("Method: " + methodName + " does not exist inside of InputWizard: " + this.getClass().getSimpleName() + ", or the method is not protected.");
        }
    }

    /**
     * Prompts the user using this Wizard's default {@link InputListener} 
     * with a message and options and returns the response, or null if the wizard was ended.
     * 
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private String promptQuick(String message, String... options) {
        WizardState state = new WizardState(message, user, options);
        awaitingResponse = true;

        state = listener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }

        return state.getMessage();
    }

    /**
     * Prompts the user using this Wizard's default {@link InputListener} 
     * with a message and options and returns the response, or null if the wizard was ended.
     * 
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private WizardResponse prompt(String message, String... options) {
        WizardState state = new WizardState(message, user, options);
        awaitingResponse = true;

        state = listener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }

        WizardResponse userResponse = new WizardResponse(state.getRecentUser(), state.getMessage(), state.getNotes());
        state.getMessageOptional().ifPresent(userResponse::setMessage);

        return userResponse;
    }

    /**
     * Prompts the user using the input {@link InputListener} 
     * with a message and options and returns the response, or null if the wizard was ended,
     * returning only the response String.
     * 
     * @param newListener The {@link InputListener} to prompt the user with.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private String promptQuick(InputListener newListener, String message, String... options) {
        WizardState state = new WizardState(message, user, options);
        awaitingResponse = true;

        state = newListener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }

        return state.getMessage();
    }

    /**
     * Prompts the user using the input {@link InputListener}
     * with a message and options and returns the response, or null if the wizard was ended,
     * returning the full {@link WizardResponse}.
     * 
     * @param newListener The {@link InputListener} to prompt the user with.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private WizardResponse prompt(InputListener newListener, String message, String... options) {
        WizardState state = new WizardState(message, user, options);
        awaitingResponse = true;

        state = newListener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }

        WizardResponse userResponse = new WizardResponse(state.getRecentUser(), state.getMessage(), state.getNotes());
        state.getMessageOptional().ifPresent(userResponse::setMessage);

        return userResponse;
    }

    /**
     * Prompts the user using the input {@link InputListener} 
     * with an EmbedCreateSpec and options and returns the response, or null if the wizard was ended,
     * returning only the response String.
     * 
     * @param newListener The {@link InputListener} to prompt the user with.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private String promptQuick(InputListener newListener, EmbedCreateSpec spec, String... options) {
        WizardState state = new WizardState(spec, user, options);
        awaitingResponse = true;

        state = newListener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }

        return state.getMessage();
    }

    /**
     * Prompts the user using the input {@link InputListener}
     * with an EmbedCreateSpec and options and returns the response, or null if the wizard was ended,
     * returning the full {@link WizardResponse}.
     * 
     * @param newListener The {@link InputListener} to prompt the user with.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private WizardResponse prompt(InputListener newListener, EmbedCreateSpec spec, String... options) {
        WizardState state = new WizardState(spec, user, options);
        awaitingResponse = true;

        state = newListener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }
        WizardResponse userResponse = new WizardResponse(state.getRecentUser(), state.getMessage(), state.getNotes());
        state.getMessageOptional().ifPresent(userResponse::setMessage);

        return userResponse;
    }

    /**
     * Prompts the user using the default {@link InputListener} 
     * with an EmbedCreateSpec and options and returns the response, or null if the wizard was ended,
     * returning only the response String.
     * 
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private String promptQuick(EmbedCreateSpec spec, String... options) {
        WizardState state = new WizardState(spec, user, options);
        awaitingResponse = true;

        state = listener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }

        return state.getMessage();
    }

    /**
     * Prompts the user using the default {@link InputListener}
     * with an EmbedCreateSpec and options and returns the response, or null if the wizard was ended,
     * returning the full {@link WizardResponse}.
     * 
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private WizardResponse prompt(EmbedCreateSpec spec, String... options) {
        WizardState state = new WizardState(spec, user, options);
        awaitingResponse = true;

        state = listener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();
        user = state.getRecentUser();

        if (state.hasEnded()) {
            return null;
        }
        
        WizardResponse userResponse = new WizardResponse(state.getRecentUser(), state.getMessage(), state.getNotes());
        state.getMessageOptional().ifPresent(userResponse::setMessage);

        return userResponse;
    }

    /**
     * Begins the wizard from the starting window.
     */
    public abstract void begin();

    /**
     * Begins the wizard starting from the provided method window name and its paramaters (if any).
     * Using this method instead of {@link #begin()} requires knowledge of the method names of the
     * InputWizard being used and does NOT begin from the wizard's configurated initial window.
     * <p>
     * This method will shut down the wizard once no more windows are running, but <b>WILL NOT</b> use
     * {@link #end()} to perform additional shutdown activities (like displaying the 'wizard ended' text).
     * 
     * @param startingMethod The method name to begin the wizard from.
     * @param parameters The paramaters required (if any) to run said method.
     * @throws IllegalArgumentException If a method with the provided name and paramaters was not found.
     */
    public void beginSilent(String startingMethod, Object... parameters) throws IllegalArgumentException {
        nextWindow(startingMethod, parameters);
        shutdown();
    }

    /**
     * Begins the wizard starting from the provided method window name and its paramaters (if any).
     * Using this method instead of {@link #begin()} requires knowledge of the method names of the
     * InputWizard being used and does NOT begin from the wizard's configurated initial window.
     * <p>
     * This method will shut down the wizard once no more windows are running, but <b>WILL</b> use
     * {@link #end()} to perform additional shutdown activities (like displaying the 'wizard ended' text).
     * 
     * @param startingMethod The method name to begin the wizard from.
     * @param parameters The paramaters required (if any) to run said method.
     * @throws IllegalArgumentException If a method with the provided name and paramaters was not found.
     */
    public void begin(String startingMethod, Object... parameters) throws IllegalArgumentException {
        nextWindow(startingMethod, parameters);
        end();
    }

    /**
     * Returns whether the wizard is active.
     * 
     * @return Whether the wizard is active.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Ends the wizard.
     */
    public void end() {
        if (!wasShutdown) {
            isActive = false;
            listener.editCurrentMessageContent("Wizard ended.");
            awaitingResponse = false;
        }
    }

    /**
     * Cancels the current {@link InputListener} and deletes the current message.
     * <p>
     * This effectively completely removes the existence of an {@link InputWizard} in a Discord {@link MessageChannel}.
     */
    public void delete() {
        awaitingResponse = false;
        listener.cancel();
        listener.deleteCurrentMessage();
    }

    /**
     * Unconditionally restarts this wizard and begins from the first window.
     */
    public void restart() {
        wasShutdown = false;
        isActive = true;
        begin();
    }

    /**
     * Turns off the wizard without performing additional {@link #end()} activities.
     */
    public void shutdown() {
        wasShutdown = true;
        awaitingResponse = false;
        isActive = false;
    }

    /**
     * Returns whether the wizard is awaiting a response.
     * 
     * @return Whether the wizard is awaiting a response.
     */
    public boolean awaitingResponse() {
        return awaitingResponse;
    }

    /**
     * Returns the user of the wizard.
     * 
     * @return The user of the wizard.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the channel of the wizard.
     * 
     * @return The channel of the wizard.
     */
    public MessageChannel getChannel() {
        return handler.getActiveMessageChannel();
    }

    /**
     * Returns this wizard's default {@link InputListener}.
     * 
     * @return this wizard's default {@link InputListener}.
     */
    public InputListener getInputListener() {
        return listener;
    }

    /**
     * Returns the currently active {@link InputListener}.
     * 
     * @return The currently active {@link InputListener}.
     */
    public InputListener getActiveListener() {
        return currentlyActiveListener;
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses this Wizard's default {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withResponse(Consumer<String> withResponse, boolean backOption, String message, String... options) {
        this.currentlyActiveListener = listener;
        if (backOption) {
            withResponseBack(withResponse, message, options);
        } else {
            withResponse(withResponse, message, options);
        }
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses this Wizard's default {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withResponse(Consumer<String> withResponse, boolean backOption, EmbedCreateSpec spec, String... options) {
        this.currentlyActiveListener = listener;
        if (backOption) {
            withResponseBack(withResponse, spec, options);
        } else {
            withResponse(withResponse, spec, options);
        }
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses this Wizard's default {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withFullResponse(Consumer<WizardResponse> withResponse, boolean backOption, String message, String... options) {
        this.currentlyActiveListener = listener;
        if (backOption) {
            withFullResponseBack(withResponse, message, options);
        } else {
            withFullResponse(withResponse, message, options);
        }
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses this Wizard's default {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withFullResponse(Consumer<WizardResponse> withResponse, boolean backOption, EmbedCreateSpec spec, String... options) {
        this.currentlyActiveListener = listener;
        if (backOption) {
            withFullResponseBack(withResponse, spec, options);
        } else {
            withFullResponse(withResponse, spec, options);
        }
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses the provided {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param newListener The {@link InputListener} to prompt the user and get their response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withResponse(Consumer<String> withResponse, boolean backOption, InputListener newListener, String message, String... options) {
        this.currentlyActiveListener = newListener;
        if (backOption) {
            withResponseBack(withResponse, newListener, message, options);
        } else {
            withResponse(withResponse, newListener, message, options);
        }
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses the provided {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param newListener The {@link InputListener} to prompt the user and get their response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withResponse(Consumer<String> withResponse, boolean backOption, InputListener newListener, EmbedCreateSpec spec, String... options) {
        this.currentlyActiveListener = newListener;
        if (backOption) {
            withResponseBack(withResponse, newListener, spec, options);
        } else {
            withResponse(withResponse, newListener, spec, options);
        }
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses the provided {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param newListener The {@link InputListener} to prompt the user and get their response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withFullResponse(Consumer<WizardResponse> withResponse, boolean backOption, InputListener newListener, String message, String... options) {
        this.currentlyActiveListener = newListener;
        if (backOption) {
            withFullResponseBack(withResponse, newListener, message, options);
        } else {
            withFullResponse(withResponse, newListener, message, options);
        }
    }

    /**
     * A handler for the response from a method (or "window") of the wizard, prompting them with the given options.
     * <p>
     * This method uses the provided {@link UserInputListener} to gather the user's response and, therefore,
     * the way the user is prompted and shown their options is determined by the listener. Refer to the configured
     * {@link UserInputListener} for documentation, or view the {@link InputListenerFactory}.
     * <p>
     * {@code withResponse} defines the logic for what this wizard should do once a valid response is given
     * by the user. Implementing wizards can assume that the logic will only be run when a valid response is given,
     * excluding the back option or a user ending the wizard (as those are automatically handled).
     * <p>
     * If {@code backOption} is true, the window will include full back option functionality, irregardless
     * if there are any previous windows to return to.
     * 
     * @param withResponse Handler for the response.
     * @param backOption True if this window should include back option functionality, false otherwise.
     * @param newListener The {@link InputListener} to prompt the user and get their response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     * @see InputListenerFactory
     */
    protected void withFullResponse(Consumer<WizardResponse> withResponse, boolean backOption, InputListener newListener, EmbedCreateSpec spec, String... options) {
        this.currentlyActiveListener = newListener;
        if (backOption) {
            withFullResponseBack(withResponse, newListener, spec, options);
        } else {
            withFullResponse(withResponse, newListener, spec, options);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponse(Consumer<String> withResponse, String message, String... options) {
        String response = promptQuick(message, options);
        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponse(Consumer<String> withResponse, EmbedCreateSpec spec, String... options) {
        String response = promptQuick(spec, options);
        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponse(Consumer<WizardResponse> withResponse, String message, String... options) {
        WizardResponse response = prompt(message, options);
        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponse(Consumer<WizardResponse> withResponse, EmbedCreateSpec spec, String... options) {
        WizardResponse response = prompt(spec, options);
        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponseBack(Consumer<String> withResponse, String message, String... options) {
        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        String response = promptQuick(message, optionsWithBack);
        if (response == null) {
            isActive = false;
        } else if (response.equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponseBack(Consumer<String> withResponse, EmbedCreateSpec spec, String... options) {
        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        String response = promptQuick(spec, optionsWithBack);
        if (response == null) {
            isActive = false;
        } else if (response.equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponseBack(Consumer<WizardResponse> withResponse, String message, String... options) {
        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        WizardResponse response = prompt(message, optionsWithBack);
        if (response == null) {
            isActive = false;
        } else if (response.getResponse().equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using this Wizard's default {@link UserInputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponseBack(Consumer<WizardResponse> withResponse, EmbedCreateSpec spec, String... options) {
        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        WizardResponse response = prompt(spec, optionsWithBack);
        if (response == null) {
            isActive = false;
        } else if (response.getResponse().equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponse(Consumer<String> withResponse, InputListener newListener, String message, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        String response = promptQuick(newListener, message, options);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }

    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponse(Consumer<String> withResponse, InputListener newListener, EmbedCreateSpec spec, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        String response = promptQuick(newListener, spec, options);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }

    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponse(Consumer<WizardResponse> withResponse, InputListener newListener, String message, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        WizardResponse response = prompt(newListener, message, options);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponse(Consumer<WizardResponse> withResponse, InputListener newListener, EmbedCreateSpec spec, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        WizardResponse response = prompt(newListener, spec, options);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponseBack(Consumer<String> withResponse, InputListener newListener, String message, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        String response = promptQuick(newListener, message, optionsWithBack);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else if (response.equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param newListener The {@link InputListener} to prompt the user with.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponseBack(Consumer<String> withResponse, InputListener newListener, EmbedCreateSpec spec, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        String response = promptQuick(newListener, spec, optionsWithBack);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else if (response.equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponseBack(Consumer<WizardResponse> withResponse, InputListener newListener, String message, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        WizardResponse response = prompt(newListener, message, optionsWithBack);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else if (response.getResponse().equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param newListener The {@link InputListener} to prompt the user with.
     * @param spec The {@link EmbedCreateSpec} to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withFullResponseBack(Consumer<WizardResponse> withResponse, InputListener newListener, EmbedCreateSpec spec, String... options) {
        if (activeFunctions.size() > 1 || newListener.getCurrentMessage() == null) {
            loadListener(newListener);
        }

        String[] optionsWithBack;
        if (activeFunctions.size() > 1) {
            optionsWithBack = new String[options.length + 1];
            System.arraycopy(options, 0, optionsWithBack, 0, options.length);
            optionsWithBack[options.length] = "back";
        } else {
            optionsWithBack = options;
        }
        WizardResponse response = prompt(newListener, spec, optionsWithBack);

        loadDefaultListener(newListener);

        if (response == null) {
            isActive = false;
        } else if (response.getResponse().equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * Sends a message in this wizard's active text channel that will self-delete in five seconds.
     * <p>
     * Invokes {@link GuildInteractionHandler#sendMessage(String, String)} for message sending.
     * 
     * @param text The body of the message.
     * @param title The title of the message.
     */
    protected void sendMessage(String text, String title) {
        ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> {
            Message message = handler.sendMessage(text, title);
            try {
                Thread.sleep(5000);
                message.delete().block();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    /**
     * Sends a message in this wizard's active text channel that will self-delete in five seconds.
     * <p>
     * Invokes {@link GuildInteractionHandler#sendMessage(String, String, String)} for message sending.
     * 
     * @param text The body of the message.
     * @param title The title of the message.
     * @param imageUrl The URL of the image to include in the message.
     */
    protected void sendMessage(String text, String title, String imageUrl) {
        ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> {
            Message message = handler.sendMessage(text, title, imageUrl);
            try {
                Thread.sleep(5000);
                message.delete().block();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    /**
     * Loads the properties from this listener's default {@link InputListener}
     * into the provided {@code newListener}.
     * 
     * @param newListener The new {@link InputListener} to load properties to.
     */
    private void loadListener(InputListener newListener) {
        newListener.setCurrentMessage(listener.getCurrentMessage());
        newListener.setUser(listener.getUser());
        newListener.setTitle(listener.getTitle());
        newListener.setInteractingMember(listener.getInteractingUser());
        newListener.setInteractionHandler(listener.getInteractionHandler());
    }

    /**
     * Loads the properties from the provided {@code newListener} into
     * this listener's default {@link InputListener}.
     * 
     * @param newListener The new {@link InputListener} to load properties from.
     */
    protected void loadDefaultListener(InputListener newListener) {
        listener.setCurrentMessage(newListener.getCurrentMessage());
        listener.setUser(newListener.getUser());
        listener.setTitle(newListener.getTitle());
        listener.setInteractingMember(newListener.getInteractingUser());
        listener.setInteractionHandler(newListener.getInteractionHandler());
    }

    /**
     * Sets the default {@link InputListener} for this wizard.
     * 
     * @param newListener The new {@link InputListener} to set as the default.
     */
    public void setDefaultListener(InputListener newListener) {
        if (listener.getCurrentMessage() != null) {
            loadListener(newListener);
        } else {
            loadDefaultListener(newListener);
        }
        this.listener = newListener;
    }

    /**
     * Cancels the currently active {@link InputListener} for this Wizard.
     */
    public void cancelCurrentListener() {
        if (currentlyActiveListener != null) {
            currentlyActiveListener.cancel();
        }
    }

    /**
     * Switches this Wizard from the default Guild wizard to a {@link User} wizard
     * for the current {@link User} of this Wizard.
     */
    public void switchToUserWizard() {
        InteractionHandler newHandler = new UserInteractionHandler(this.user);
        this.handler = newHandler;
        this.listener.setInteractionHandler(newHandler);
    }

    /**
     * Switches this Wizard from the default Guild wizard to a {@link User} wizard
     * for the provided {@link User}.
     * 
     * @param user The {@link User} to switch to.
     */
    public void swtichToUserWizard(User user) {
        InteractionHandler newHandler = new UserInteractionHandler(user);
        this.user = user;
        this.listener.setUser(user);
        this.currentlyActiveListener.setUser(user);
        this.handler = newHandler;
        this.listener.setInteractionHandler(newHandler);
    }

    /**
     * Returns the Guild this Wizard was started in.
     * 
     * @return The Guild this Wizard was started in.
     */
    public Guild getGuild() {
        return event.getGuildInteractionHandler().getGuild();
    }

    /**
     * Returns if an {@link InputListener} has been started for this Wizard.
     * 
     * @return True if an {@link InputListener} has been started for this Wizard, false otherwise.
     */
    public boolean hasStarted() {
        return listener.getCurrentMessage() != null || currentlyActiveListener.getCurrentMessage() != null;
    }
}
