package com.georgster;

import discord4j.core.event.domain.message.MessageCreateEvent;

interface Command {
    void execute(MessageCreateEvent event);
}
