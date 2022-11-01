package com.georgster.plinko;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;

public class PlinkoGame {

    private String guild;
    private Channel channel;
    private String[][] board = {
        {"|"," "," "," "," "," "," "," "," "," "," "," ","|"},
        {"\\"," ","."," ","."," ","."," ","."," ","."," ","/"},
        {" ","\\"," ","."," ","."," ","."," ","."," ","/"," "},
        {" ","/"," "," ","."," ","."," ","."," "," ","\\"," "},
        {"/"," ","."," ","."," ","."," ","."," ","."," ","\\"},
        {"\\"," "," ","."," ","."," ","."," ","."," "," ","/"},
        {" ","\\"," "," ","."," ","."," ","."," "," ","/"," "},
        {" ","/"," ","."," ","."," ","."," ","."," ","\\"," "},
        {"/"," ","."," ","."," ","."," ","."," ","."," ","\\"},
        {"\\"," "," ","."," ","."," ","."," ","."," ","/"},
        {"|"," "," "," "," "," "," "," "," "," "," "," ","|"},
        {"|","\\_","|","\\_","|","","|","\\_","|","\\_","|","\\_","|"}
    };

    /**
     * Creates a {@code PlinkoGame}. A {@code PlinkoReward} is based on the guild ID.
     * 
     * @param event The MessageCreateEvent that prompted the creation of this {@code PlinkoGame}.
     */
    PlinkoGame(MessageCreateEvent event) {
        event.getGuildId().ifPresent(flake -> guild = flake.asString());
        channel = event.getMessage().getChannel().block();
    }

    protected void play() {
        StringBuilder sBoard = new StringBuilder();
        for (String[] x : board) {
            for (String y : x) {
                sBoard.append(y);
            }
            sBoard.append("\n");
        }
        ((MessageChannel) getChannel()).createMessage(sBoard.toString()).block();
    }

    /**
     * Returns the guild ID where this {@code PlinkoGame} is active.
     * Only classes within the plinko package should be able to access this.
     * 
     * @return A string containing the ID of the guild where this {@code PlinkoGame} is active.
     */
    protected String getGuild() {
        return guild;
    }

    /**
     * Returns the channel where this {@code PlinkoGame} is active.
     * Only classes within the plinko package should be able to access this.
     * 
     * @return The channel where this {@code PlinkoGame} is active.
     */
    protected Channel getChannel() {
        return channel;
    }

}
