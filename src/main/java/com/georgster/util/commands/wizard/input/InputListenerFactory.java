package com.georgster.util.commands.wizard.input;

import com.georgster.control.util.CommandExecutionEvent;

import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;

/**
 * Factory class for creating {@link InputListener}s. All listeners can
 * be built with {@link InputListener#builder()}.
 */
public class InputListenerFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private InputListenerFactory() {
        throw new IllegalStateException("Factory class");
    }

    /**
     * Returns {@link MenuMessageListener} which will send a message to the user
     * and provide a menu of options to choose from. Responses can be recorded
     * via the select menu or a message containing the option.
     * 
     * @param event Command execution event that triggered the wizard.
     * @param title Title of the menu.
     * @return {@link MenuMessageListener} that will send a menu message and record responses.
     */
    public static InputListener createMenuMessageListener(CommandExecutionEvent event, String title) {
        return new MenuMessageListener(event, title);
    }

    /**
     * Returns a {@link ButtonMessageListener} Sends a message to the user in a {@code Message}
     * containing {@link Button}s as the options. Users can respond by either clicking on the 
     * corresponding button, or by sending a message containing a valid option.
     * 
     * @param event Command execution event that triggered the wizard.
     * @param title Title of the menu.
     * @return {@link ButtonMessageListener} that will send a message containing buttons and record responses.
     */
    public static InputListener createButtonMessageListener(CommandExecutionEvent event, String title) {
        return new ButtonMessageListener(event, title);
    }

    /**
     * Returns a {@link MessageListener} Sends a message to the user in a {@code Message} the options.
     * Users can respond by either clicking on the  corresponding button,
     * or by sending a message containing a valid option.
     * 
     * @param event Command execution event that triggered the wizard.
     * @param title Title of the menu.
     * @return {@link MessageListener} that will send a message and record responses.
     */
    public static InputListener createMessageListener(CommandExecutionEvent event, String title) {
        return new MessageListener(event, title);
    }

    /**
     * Creates a {@link ReactionListener} which will attach reaction emojis onto the current message
     * and record if any of them are clicked, returning the name of the emoji in the reaction.
     * 
     * @param event Command execution event that triggered the wizard.
     * @param title Title of the menu.
     * @return {@link ReactionListener} that will send a message and record responses.
     */
    public static InputListener createReactionListener(CommandExecutionEvent event, ReactionEmoji... emojis) {
        return new ReactionListener(event, "Test Title", emojis);
    }
}
