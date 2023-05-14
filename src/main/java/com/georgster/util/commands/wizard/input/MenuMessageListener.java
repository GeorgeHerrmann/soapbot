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

public class MenuMessageListener implements UserInputListener {
    private static final int TIMEOUT_TIME = 300; // will wait 30s for a response (is in ms)
    private static final String END_STRING = "end";

    private final String endMessage;
    private final String title;
    private final EventDispatcher dispatcher;
    private final GuildInteractionHandler handler;
    private final Member user;
    private Message message;

    public MenuMessageListener(String endMessage, String title, EventDispatcher dispatcher, GuildInteractionHandler handler, Member user) {
        this.endMessage = endMessage;
        this.title = title;
        this.dispatcher = dispatcher;
        this.handler = handler;
        this.user = user;
        this.message = null;
    }

    public WizardState prompt(WizardState inputState) {
        String prompt = inputState.getMessage();
        String[] options = inputState.getOptions();
        //boolean ended = inputState.hasEnded();

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
            .filter(event -> List.of(options).contains(event.getMessage().getContent().toLowerCase()) || event.getMessage().getContent().toLowerCase().equals(endMessage))
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
                    return null;
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
        return output.toString();
    }
    
}
