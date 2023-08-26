package com.georgster.util.commands.wizard.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.WizardState;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;

/**
 * Sends a message to the user in a {@code Message} containing {@link Button}s as the options.
 * Users can respond by either clicking on the corresponding button, or by sending a message
 * containing a valid option.
 * <p>
 * This listener will timeout after 30s of inactivity following
 * {@link #prompt(WizardState)} being called.
 * <p>
 * Every five {@link Button}s will be placed in their own {@link ActionRow},
 * up to a maximum of five rows. <b>Therefore, this listener must have anywhere
 * from 0 to 25 options</b>
 * <p>
 * If zero options are provided, the {@code Message} will still be sent, but will contain
 * no buttons.
 * <p>
 * By default, this listener follows {@link InputListener}'s lenient matching rules,
 * and has {@link InputListener#hasXReaction(boolean)} enabled.
 * 
 * @see InputListener#mustMatch(boolean, boolean)
 */
public class ButtonMessageListener extends InputListener {

    /**
     * Creates a new {@code ButtonMessageListener} with the given parameters.
     * 
     * @param event The event that prompted this listener's creation.
     * @param title The title to place onto messages.
     */
    public ButtonMessageListener(CommandExecutionEvent event, String title) {
        super(event, title, "end");
        hasXReaction(true);
        mustMatch(true, false);
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

        sendPromptMessage(prompt, getRowsFromButtons(buttons));
        

        // Create a listener that listens for the user to click a button
        createListener(dispatcher -> dispatcher.on(ButtonInteractionEvent.class)
            .filter(event -> event.getInteraction().getMember().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().get().getId().asString().equals(message.getId().asString()))
            .subscribe(event -> {
                setResponse(event.getCustomId().toLowerCase());
                handler.setActiveComponentInteraction(event);
            }));

        createListener(dispatcher -> dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getAuthor().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().getChannelId().equals(message.getChannelId()))
            .subscribe(event -> setResponse(event.getMessage().getContent())));
            
        return waitForResponse(inputState);
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
