package com.georgster.util.commands.wizard.input;

import com.georgster.control.util.CommandExecutionEvent;

/**
 * Factory class for creating input listeners.
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
     * via the select menu or a message containing the option. Can be exited
     * with a message containing "end" or reacting with the "end" emoji.
     * 
     * @param event Command execution event that triggered the wizard.
     * @param title Title of the menu.
     * @return {@link MenuMessageListener} that will send a menu message and record responses.
     */
    public static UserInputListener createMenuMessageListener(CommandExecutionEvent event, String title) {
        return new MenuMessageListener("end", title, event.getEventDispatcher(), event.getGuildInteractionHandler(), event.getDiscordEvent().getAuthorAsMember());
    }
}
