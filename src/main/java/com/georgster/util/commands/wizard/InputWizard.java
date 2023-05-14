package com.georgster.util.commands.wizard;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.GuildInteractionHandler;

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

    protected InputWizard(String title, CommandExecutionEvent event) {
        this.title = title;
        this.user = event.getDiscordEvent().getAuthorAsMember();
        this.dispatcher = event.getEventDispatcher();
        this.handler = event.getGuildInteractionHandler();
        this.isActive = false;
        this.awaitingResponse = false;
    }

    public String prompt(String message, String... options) {
        // TODO: implement
        return "";
    }

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

}
