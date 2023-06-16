package com.georgster.util.commands.wizard.input;

import java.util.List;

import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.wizard.WizardState;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

/**
 * Sends a message to the user in a {@code SelectMenu} and records their response via either
 * selecting or typing the option. This listener will timeout after 30s of inactivity following
 * {@code prompt()} being called, and can be ended early by the user by reacting with the
 * {@code ❌} emoji.
 * <p>
 * <b>Note:</b> If there are zero or one options when calling {@link #prompt(WizardState)},
 * this listener will accept any message-based input, otherwise only an input matching one of the options will be accepted.
 */
public class MenuMessageListener implements UserInputListener {
    private static final int TIMEOUT_TIME = 300; // will wait 30s for a response (is in ms)

    private final String endMessage;
    private final String title;
    private final EventDispatcher dispatcher;
    private final GuildInteractionHandler handler;
    private final Member user;
    private Message message;

    /**
     * Creates a new {@code MenuMessageListener} with the given parameters.
     * 
     * @param endMessage Message to send when the listener ends.
     * @param title Title of the menu.
     * @param dispatcher Dispatcher to listen for events on.
     * @param handler Handler to send messages with.
     * @param user User to send messages to.
     */
    public MenuMessageListener(String endMessage, String title, EventDispatcher dispatcher, GuildInteractionHandler handler, Member user) {
        this.endMessage = endMessage;
        this.title = title;
        this.dispatcher = dispatcher;
        this.handler = handler;
        this.user = user;
        this.message = null;
    }

    /**
     * {@inheritDoc}
     */
    public WizardState prompt(WizardState inputState) {
        String prompt = inputState.getMessage();
        String[] options = inputState.getOptions();

        boolean mustMatch = (options.length > 2 || !((List.of(options).contains("back") && options.length == 2) || options.length == 1));

        SelectMenu.Option[] menuOptions = new SelectMenu.Option[options.length];

        for (int i = 0; i < options.length; i++) {
            menuOptions[i] = SelectMenu.Option.of(options[i], options[i]);
        }

        SelectMenu menu = SelectMenu.of(title, menuOptions);

        prompt += "\nYour options are: " + String.join(", ", options);
        if (message == null) {
            message = handler.sendText(prompt, title, ActionRow.of(menu));
            message.addReaction(ReactionEmoji.unicode("❌")).block();
        } else {
            try {
                message = handler.editMessageContent(message, prompt, title, ActionRow.of(menu));
            } catch (Exception e) {
                System.out.println("here");
            }
        }
        
        StringBuilder output = new StringBuilder();

        // Create a listener that listens for the user's next message
        Disposable canceller = dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getAuthor().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().getChannelId().equals(message.getChannelId()))
            .filter(event -> (List.of(options).contains(event.getMessage().getContent().toLowerCase()) || !mustMatch) || event.getMessage().getContent().toLowerCase().equals(endMessage))
            .subscribe(event -> {
                if (event.getMessage().getContent().equals(endMessage)) {
                    inputState.end();
                } else {
                    output.append(event.getMessage().getContent());
                }
            });

        // Create a listener that listens for the user to end the wizard by reacting
        Disposable canceller2 = dispatcher.on(ReactionAddEvent.class)
            .filter(event -> event.getMember().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getEmoji().equals(ReactionEmoji.unicode("❌")))
            .subscribe(event -> inputState.end());

            // Create a listener that listens for the user to select an option
        Disposable canceller3 = dispatcher.on(SelectMenuInteractionEvent.class)
            .filter(event -> event.getInteraction().getMember().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().get().getId().asString().equals(message.getId().asString()))
            .subscribe(event -> {
                output.append(event.getValues().get(0));
                handler.setActiveSelectMenuInteraction(event);
            });

        int timeout = 0;
        while (output.isEmpty()) { // Wait for the user to send a message
            try {
                if (inputState.hasEnded() || timeout > TIMEOUT_TIME) {
                    canceller.dispose();
                    canceller2.dispose();
                    canceller3.dispose();
                    inputState.end();
                    return inputState;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            timeout ++;
        }
        canceller.dispose(); // Dispose of the listeners
        canceller2.dispose();
        canceller3.dispose();

        inputState.setMessage(output.toString());
        return inputState;
    }

    /**
     * {@inheritDoc}
     */
    public void editCurrentMessageContent(String newContent) {
        message = handler.editMessageContent(message, newContent, title);
    }
    
}
