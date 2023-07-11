package com.georgster.util.commands.wizard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.wizard.input.InputListener;
import com.georgster.util.commands.wizard.input.InputListenerFactory;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

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

    protected final Member user;
    private boolean isActive;
    private boolean awaitingResponse;
    protected final GuildInteractionHandler handler;
    private final InputListener listener;
    protected final MultiLogger logger;

    /**
     * Initializes the InputWizard engine.
     * 
     * @param event The event that prompted the Wizard creation.
     * @param listener The {@code InputListener} which will handle the user interaction.
     */
    protected InputWizard(CommandExecutionEvent event, InputListener listener) {
        this.activeFunctions = new ArrayDeque<>();
        this.activeFunctionParams = new ArrayDeque<>();

        this.user = event.getDiscordEvent().getAuthorAsMember();
        this.handler = event.getGuildInteractionHandler();
        this.isActive = true;
        this.awaitingResponse = false;
        this.listener = listener;
        this.logger = event.getLogger();
    }

    /**
     * Immediately switches to a new window in this {@code InputWizard}, given
     * the name of a method in the wizard and the parameters required to run it.
     * 
     * @param methodName The name of the method which will run the next window in this {@code InputWizard}.
     * @param parameters The parameters for said method.
     */
    protected void nextWindow(String methodName, Object... parameters) {
        try {
            activeFunctions.push(getMethod(methodName, parameters));
            activeFunctionParams.push(parameters);

            logger.append("- Switching to window: " + methodName + "\n", LogDestination.NONAPI);

            invokeCurrentMethod();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
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
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
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
    private String prompt(String message, String... options) {
        WizardState state = new WizardState(message, options);
        awaitingResponse = true;

        state = listener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();

        if (state.hasEnded()) {
            return null;
        }

        return state.getMessage();
    }

    /**
     * Prompts the user using the input {@link InputListener} 
     * with a message and options and returns the response, or null if the wizard was ended.
     * 
     * @param newListener The {@link InputListener} to prompt the user with.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard was ended.
     */
    private String prompt(InputListener newListener, String message, String... options) {
        WizardState state = new WizardState(message, options);
        awaitingResponse = true;

        state = newListener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();

        if (state.hasEnded()) {
            return null;
        }

        return state.getMessage();
    }

    /**
     * Begins the wizard.
     */
    public abstract void begin();

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
        isActive = false;
        listener.editCurrentMessageContent("Wizard ended.");
        
    }

    /**
     * Restarts this wizard and begins from the first window.
     */
    public void restart() {
        isActive = true;
        begin();
    }

    /**
     * Turns off the wizard without performing additional {@link #end()} activities.
     */
    public void shutdown() {
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
    public Member getUser() {
        return user;
    }

    /**
     * Returns the channel of the wizard.
     * 
     * @return The channel of the wizard.
     */
    public Channel getChannel() {
        return handler.getActiveChannel();
    }

    public InputListener getInputListener() {
        return listener;
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
        if (backOption) {
            withResponseBack(withResponse, message, options);
        } else {
            withResponse(withResponse, message, options);
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
        if (backOption) {
            withResponseBack(withResponse, newListener, message, options);
        } else {
            withResponse(withResponse, newListener, message, options);
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
        String response = prompt(message, options);
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
        String[] optionsWithBack = new String[options.length + 1];
        System.arraycopy(options, 0, optionsWithBack, 0, options.length);
        optionsWithBack[options.length] = "back";
        String response = prompt(message, optionsWithBack);
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
     * using the given {@link InputListener}.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    private void withResponse(Consumer<String> withResponse, InputListener newListener, String message, String... options) {
        String response = prompt(newListener, message, options);
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
        String[] optionsWithBack = new String[options.length + 1];
        System.arraycopy(options, 0, optionsWithBack, 0, options.length);
        optionsWithBack[options.length] = "back";
        String response = prompt(newListener, message, optionsWithBack);
        if (response == null) {
            isActive = false;
        } else if (response.equalsIgnoreCase("back")) {
            goBack();
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * Sends a message in this wizard's active text channel that will self-delete in five seconds.
     * <p>
     * Invokes {@link GuildInteractionHandler#sendText(String, String)} for message sending.
     * 
     * @param text The body of the message.
     * @param title The title of the message.
     */
    protected void sendMessage(String text, String title) {
        ThreadPoolFactory.scheduleGeneralTask(handler.getId(), () -> {
            Message message = handler.sendText(text, title);
            try {
                Thread.sleep(5000);
                message.delete().block();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }
}
