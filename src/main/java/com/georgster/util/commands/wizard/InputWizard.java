package com.georgster.util.commands.wizard;

import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.wizard.input.UserInputListener;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;

public abstract class InputWizard {
    protected final Member user;
    private boolean isActive;
    private boolean awaitingResponse;
    protected final GuildInteractionHandler handler;
    private final UserInputListener listener;

    protected InputWizard(CommandExecutionEvent event, UserInputListener listener) {
        this.user = event.getDiscordEvent().getAuthorAsMember();
        this.handler = event.getGuildInteractionHandler();
        this.isActive = true;
        this.awaitingResponse = false;
        this.listener = listener;
    }

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

    public abstract void begin();

    public boolean isActive() {
        return isActive;
    }

    public void end() {
        isActive = false;
        listener.editCurrentMessageContent("Wizard ended.");
        
    }

    public boolean awaitingResponse() {
        return awaitingResponse;
    }

    public Member getUser() {
        return user;
    }

    public Channel getChannel() {
        return handler.getActiveChannel();
    }

    protected void withResponseFirst(Consumer<String> withResponse, String message, String... options) {
        String response = prompt(message, options);
        if (response == null) {
                isActive = false;
        } else {
            withResponse.accept(response);
        }
    }

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
            return WizardResponse.STRING;
        }
    }

}
