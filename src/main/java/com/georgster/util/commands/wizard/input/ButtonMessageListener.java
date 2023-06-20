package com.georgster.util.commands.wizard.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.wizard.WizardState;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

/**
 * Sends a message to the user in a {@code Message} containing {@link Button}s as the options.
 * Users can respond by either clicking on the corresponding button, or by sending a message
 * containing a valid option.
 * <p>
 * This listener will timeout after 30s of inactivity following
 * {@link #prompt(WizardState)} being called, and can be ended early by the user by reacting with the
 * {@code ❌} emoji, or a user typing the "end" String.
 * <p>
 * Every five {@link Button}s will be placed in their own {@link ActionRow},
 * up to a maximum of five rows. <b>Therefore, this listener must have anywhere
 * from 0 to 25 options</b>
 * <p>
 * If zero options are provided, the {@code Message} will still be sent, but will contain
 * no buttons.
 * <p>
 * This Listener will only accept inputs matching one of the given options when calling {@link #prompt(WizardState)},
 * except where the user essentially has no true choices, such as in the following cases:
 * <ul>
 * <li>There are no options</li>
 * <li>There is only one option</li>
 * <li>There are two options, but one is the "back" option</li>
 * </ul>
 */
public class ButtonMessageListener implements UserInputListener {
    
    private static final int TIMEOUT_TIME = 300; // will wait 30s for a response (is in ms)

    private final String end;
    private final String title;
    private final EventDispatcher dispatcher;
    private final GuildInteractionHandler handler;
    private final Member user;
    private Message message;

    /**
     * Creates a new {@code ButtonInputListener} with the given parameters.
     * 
     * @param endMessage Message to send when the listener ends.
     * @param title Title of the menu.
     * @param dispatcher Dispatcher to listen for events on.
     * @param handler Handler to send messages with.
     * @param user User to send messages to.
     */
    public ButtonMessageListener(String end, String title, EventDispatcher dispatcher, GuildInteractionHandler handler, Member user) {
        this.end = end;
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

        Button[] buttons = new Button[options.length];

        for (int i = 0; i < options.length; i++) {
            if (options[i].equals("back")) {
                buttons[i] = Button.secondary(options[i], options[i]);
            } else {
                buttons[i] = Button.primary(options[i], options[i]);
            }
        }

        boolean mustMatch = (options.length > 2 || !((List.of(options).contains("back") && options.length == 2) || options.length == 1));

        if (message == null) {
            message = handler.sendText(prompt, title, getRowsFromButtons(buttons));
            message.addReaction(ReactionEmoji.unicode("❌")).block();
        } else {
            try {
                message = handler.editMessageContent(message, prompt, title, getRowsFromButtons(buttons));
            } catch (Exception e) {
                System.out.println("here");
            }
        }
        
        StringBuilder output = new StringBuilder();

        // Create a listener that listens for the user to end the wizard by reacting
        Disposable canceller = dispatcher.on(ReactionAddEvent.class)
            .filter(event -> event.getMember().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getEmoji().equals(ReactionEmoji.unicode("❌")))
            .subscribe(event -> inputState.end());

        // Create a listener that listens for the user to click a button
        Disposable canceller2 = dispatcher.on(ButtonInteractionEvent.class)
            .filter(event -> event.getInteraction().getMember().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().get().getId().asString().equals(message.getId().asString()))
            .subscribe(event -> {
                output.append(event.getCustomId());
                handler.setActiveComponentInteraction(event);
            });

        Disposable canceller3 = dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getAuthor().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().getChannelId().equals(message.getChannelId()))
            .filter(event -> (List.of(options).contains(event.getMessage().getContent().toLowerCase()) || !mustMatch) || event.getMessage().getContent().toLowerCase().equals(end))
            .subscribe(event -> {
                if (event.getMessage().getContent().equals(end)) {
                    inputState.end();
                } else {
                    output.append(event.getMessage().getContent());
                }
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

    /**
     * Returns an array of ActionRows with a maximum of five buttons per row, up to a
     * maximum of five rows.
     * 
     * @param buttons The buttons to layout in the ActionRow.
     * @return An array of ActionRows with the buttons laid out.
     */
    private ActionRow[] getRowsFromButtons(Button... buttons) {
        List<ActionRow> rows = new ArrayList<>();

        int copyRangeBegin = 0;
        for (int i = 0; i < buttons.length; i++) {
            if ((i != 0 && ((i + 1) % 5 == 0 || i == buttons.length - 1)) || (i == 0 && buttons.length == 1)) {
                rows.add(ActionRow.of(Arrays.copyOfRange(buttons, copyRangeBegin * 5, i + 1)));
                copyRangeBegin++;
            }
        }

        return rows.toArray(new ActionRow[copyRangeBegin]);
    }

}
