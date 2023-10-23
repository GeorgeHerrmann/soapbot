package com.georgster.wizard.input;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.WizardState;

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

/**
 * Sends a message to the user in a {@code SelectMenu} and records their response via either
 * selecting or typing the option. This listener will timeout after 30s of inactivity following
 * {@link #prompt(WizardState)} being called.
 * <p>
 * Because this Listener uses Discord's SelectMenu functionality, at least one option must be provided
 * when calling {@link #prompt(WizardState)} (including the back option). If no options are provided,
 * {@link #prompt(WizardState)} will fail.
 * <p>
 * By default, this listener follows {@link InputListener}'s lenient matching rules,
 * and has {@link InputListener#hasXReaction(boolean)} enabled.
 * 
 * @see InputListener#mustMatch(boolean, boolean)
 */
public class MenuMessageListener extends InputListener {

    /**
     * Creates a new {@code MenuMessageListener} with the given parameters.
     * 
     * @param event The event that prompted this listener's creation.
     * @param title The title to place onto messages.
     */
    public MenuMessageListener(CommandExecutionEvent event, String title) {
        super(event, title, "end");
        mustMatch(true, false);
        hasXReaction(true);
    }

    /**
     * {@inheritDoc}
     */
    public WizardState prompt(WizardState inputState) {
        StringBuilder prompt = new StringBuilder(inputState.getMessage());
        String[] options = inputState.getOptions();

        SelectMenu.Option[] menuOptions = new SelectMenu.Option[options.length];

        for (int i = 0; i < options.length; i++) {
            menuOptions[i] = SelectMenu.Option.of(options[i], options[i]);
        }

        SelectMenu menu = SelectMenu.of(title, menuOptions);

        prompt.append("\nYour options are: " + String.join(", ", options));

        inputState.getEmbed().ifPresentOrElse(spec ->
            sendPromptMessage(spec, ActionRow.of(menu)),
        () ->
            sendPromptMessage(prompt.toString(), ActionRow.of(menu)));

        // Create a listener that listens for the user's next message
        createListener(dispatcher -> dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getChannelId().equals(message.getMessage().getChannelId()))
            .subscribe(event -> setResponse(event.getMessage().getContent(), event.getMessage().getAuthor().orElse(user))));

        // Create a listener that listens for the user to select an option
        createListener(dispatcher -> dispatcher.on(SelectMenuInteractionEvent.class)
            .filter(event -> event.getMessage().get().getId().asString().equals(message.getMessage().getId().asString()))
            .subscribe(event -> {
                setResponse(event.getValues().get(0), event.getInteraction().getUser());
                handler.setActiveComponentInteraction(event);
            }));

        return waitForResponse(inputState);
    }
    
}
