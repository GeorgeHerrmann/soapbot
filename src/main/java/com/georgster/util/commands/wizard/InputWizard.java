package com.georgster.util.commands.wizard;

import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.wizard.input.UserInputListener;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;

/**
 * Abstract class for creating a wizard that prompts the user for input and records the response.
 * <p>In order to define different states for the wizard, states should be defined in methods which
 * point to eachother, stemming from {@link #begin()}. Responses should be handled with {@code withResponseFirst()}
 * for the first state, and {@code withResponse()} for all other states. Example implementation:</p>
 * <pre>
 * {@code
 * public void begin() {
 *    String prompt = "Pick heads or tails";
 *    String[] options = {"heads", "tails"};
 *    withResponseFirst((response -> {
 *      if (response.equalsIgnoreCase("heads")) {
 *          pickedHeads();
 *      } else {
 *          pickedTails();
 *      }
 *    }), prompt, options);
 * }
 * 
 * public void pickedHeads() {
 *   while (isActive()) { // For states which be repeated
 *      String prompt = "You picked heads. Pick a number between 1 and 2";
 *      String[] options = {"1", "2"};
 *      withResponse((response -> {
 *          if (response.equals("1")) {
 *              picked1();
 *          } else {
 *              picked2();
 *          }
 *      }), prompt, options);
 *      if (output == WizardResponse.BACK) { // For states which you can go back to the previous state
 *          return;
 *      }
 *   }
 * }
 * </pre>
 * 
 */
public abstract class InputWizard {
    protected final Member user;
    private boolean isActive;
    private boolean awaitingResponse;
    protected final GuildInteractionHandler handler;
    private final UserInputListener listener;

    /**
     * Creates a new InputWizard with the given event and listener.
     * 
     * @param event Event to create the wizard from.
     * @param listener Listener to handle input.
     */
    protected InputWizard(CommandExecutionEvent event, UserInputListener listener) {
        this.user = event.getDiscordEvent().getAuthorAsMember();
        this.handler = event.getGuildInteractionHandler();
        this.isActive = true;
        this.awaitingResponse = false;
        this.listener = listener;
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
    public abstract void begin(); // The first state should be defined here

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
     * A handler for the response from the first state of the wizard.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     */
    protected void withResponseFirst(Consumer<String> withResponse, String message, String... options) {
        String response = prompt(message, options);
        if (response == null) {
                isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

    /**
     * A handler for the response from a state of the wizard.
     * 
     * @param withResponse Handler for the response.
     * @param message Message to prompt the user with.
     * @param options Options to provide the user.
     * @return A {@link WizardResponse} indicating the result of the response.
     */
    protected WizardResponse withResponse(Consumer<String> withResponse, String message, String... options) {
        String[] optionsWithBack = new String[options.length + 1];
        System.arraycopy(options, 0, optionsWithBack, 0, options.length);
        optionsWithBack[options.length] = "back";

        String response = prompt(message, optionsWithBack);
        if (response == null) {
            isActive = false;
            return WizardResponse.ENDED;
        } else if (response.equalsIgnoreCase("back")) {
            return WizardResponse.BACK;
        } else {
            withResponse.accept(response);
            return WizardResponse.NEXT;
        }
    }

}
