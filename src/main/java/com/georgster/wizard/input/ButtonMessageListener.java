package com.georgster.wizard.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.WizardState;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Attachment;

/**
 * Sends a message to the user in a {@code Message} containing {@link Button}s as the options.
 * Users can respond by either clicking on the corresponding button, or by sending a message
 * containing a valid option.
 * <p>
 * Buttons will be Primary buttons, unless the "back" option is present, in which case it will be
 * a Secondary button, or if the option starts with a "!", it will be a danger button (The first ! is removed and not returned as part of the user's response if selected).
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
     * <p>
     * If the user sends an attachment with their message, the attachment's URL will be used as the response.
     */
    public WizardState prompt(WizardState inputState) {
        String prompt = inputState.getMessage();
        String[] options = inputState.getOptions();

        Button[] buttons = new Button[options.length];

        for (int i = 0; i < options.length; i++) {
            if (options[i].equals("back")) {
                buttons[i] = Button.secondary(options[i], options[i]);
            } else if (options[i].startsWith("!"))  {
                options[i] = options[i].substring(1);
                inputState.getOptions()[i] = options[i];
                buttons[i] = Button.danger(options[i], options[i]);
            } else {
                buttons[i] = Button.primary(options[i], options[i]);
            }
        }

        inputState.getEmbed().ifPresentOrElse(spec ->
            sendPromptMessage(spec, getRowsFromButtons(buttons)),
        () -> 
            sendPromptMessage(prompt, getRowsFromButtons(buttons)));

        // Create a listener that listens for the user to click a button
        createListener(dispatcher -> dispatcher.on(ButtonInteractionEvent.class)
            .filter(event -> event.getInteraction().getUser().getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().get().getId().asString().equals(message.getMessage().getId().asString()))
            .subscribe(event -> {
                setResponse(event.getCustomId().toLowerCase(), event.getInteraction().getUser());
                handler.setActiveComponentInteraction(event);
            }));

        createListener(dispatcher -> dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getAuthor().orElse(user).getId().asString().equals(user.getId().asString()))
            .filter(event -> event.getMessage().getChannelId().equals(message.getMessage().getChannelId()))
            .subscribe(event -> {
                List<Attachment> attachments = event.getMessage().getAttachments();
                if (attachments.isEmpty()) {
                    setResponse(event.getMessage().getContent(), event.getMessage().getAuthor().orElse(user));
                } else {
                    setResponse(attachments.get(0).getUrl(), event.getMessage().getAuthor().orElse(user));
                }
                setResponseMessage(event.getMessage());
            }));
            
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
