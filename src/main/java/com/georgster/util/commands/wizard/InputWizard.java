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
import com.georgster.util.commands.wizard.input.UserInputListener;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**
 * Abstract class for creating a wizard that prompts the user for input and records the response.
 * <p>In order to define different states for the wizard, states should be defined in methods which
 * point to eachother with {@link #nextWindow(String, Object...)}, stemming from {@link #begin()}.
 * Responses should be handled with {@link #withResponseBack(Consumer, String, String...)} for states with back functionality,
 * and {@code withResponse()} for all other states.</p>
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
 *    }), prompt, options);
 * }
 * 
 * protected void pickedHeads() {
 *    String prompt = "You picked heads. Pick a number between 1 and 2";
 *    String[] options = {"1", "2"};
 *    withResponseBack((response -> {
 *        if (response.equals("1")) {
 *            nextWindow("picked1");
 *        } else {
 *            nextWindow("picked2");
 *        }
 *    }), prompt, options);
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
    private final UserInputListener listener;
    protected final MultiLogger logger;

    /**
     * Initializes the InputWizard engine.
     * 
     * @param event The event that prompted the Wizard creation.
     * @param listener The {@code UserInputListener} which will handle the user interaction.
     */
    protected InputWizard(CommandExecutionEvent event, UserInputListener listener) {
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
     * Prompts the user with a message and options and returns the response,
     * or null if the wizard has ended.
     * 
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @return Response from the user, or null if the wizard has ended.
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

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    protected void withResponse(Consumer<String> withResponse, String message, String... options) {
        String response = prompt(message, options);
        if (response == null) {
            isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard, prompting them with the given options
     * and including back functionality.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    protected void withResponseBack(Consumer<String> withResponse, String message, String... options) {
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
