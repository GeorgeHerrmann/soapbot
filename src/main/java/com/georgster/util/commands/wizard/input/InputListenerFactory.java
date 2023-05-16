package com.georgster.util.commands.wizard.input;

import com.georgster.control.util.CommandExecutionEvent;

public class InputListenerFactory {

    private InputListenerFactory() {
        throw new IllegalStateException("Factory class");
    }

    public static UserInputListener createMenuMessageListener(CommandExecutionEvent event, String title) {
        return new MenuMessageListener("end", title, event.getEventDispatcher(), event.getGuildInteractionHandler(), event.getDiscordEvent().getAuthorAsMember());
    }
}
