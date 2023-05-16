package com.georgster.util.commands.wizard;

import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.wizard.input.UserInputListener;

import discord4j.core.event.EventDispatcher;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;

public abstract class InputWizard {
    private String title;
    private final Member user;
    private final EventDispatcher dispatcher;
    private boolean isActive;
    private boolean awaitingResponse;
    private final GuildInteractionHandler handler;
    private final UserInputListener listener;

    protected InputWizard(String title, CommandExecutionEvent event, UserInputListener listener) {
        this.title = title;
        this.user = event.getDiscordEvent().getAuthorAsMember();
        this.dispatcher = event.getEventDispatcher();
        this.handler = event.getGuildInteractionHandler();
        this.isActive = false;
        this.awaitingResponse = false;
        this.listener = listener;
    }

    private String prompt(String message, String... options) {
        WizardState state = new WizardState(message, options);
        awaitingResponse = true;
        state = listener.prompt(state);
        awaitingResponse = false;
        isActive = !state.hasEnded();

        return state.getMessage();
    }

    public abstract void begin();

    public boolean isActive() {
        return isActive;
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
        handler.sendText("Wizard ended", title);
    }

    protected boolean withResponse(Consumer<String> withResponse, String message, String... options) {
        String response = prompt(message, options);
        if (response == null) {
            isActive = false;
            return false;
        } else if (response.equalsIgnoreCase("back")) {
            return true;
        } else {
            withResponse.accept(response);
        }
        return false;
    }

}
