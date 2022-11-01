package com.georgster.plinko;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.Message;
import java.util.Random;

public class PlinkoGame {

    private String guild;
    private Channel channel;
    private Random rand;
    private String[][] board = {
        {"|"," "," "," "," "," "," "," "," "," "," "," "," "," "," "," ","|"},
        {"\\"," ","."," ","."," ","."," ","."," ","."," ","."," ","/"},
        {" ","\\"," ","."," ","."," ","."," ","."," ","."," ","/"," "},
        {" ","/"," "," ","."," ","."," ","."," ","."," "," ","\\"," "},
        {"/"," ","."," ","."," ","."," ","."," ","."," ","."," ","\\"},
        {"\\"," "," ","."," ","."," ","."," ","."," ","."," "," ","/"},
        {" ","\\"," "," ","."," ","."," ","."," ","."," "," ","/"," "},
        {" ","/"," ","."," ","."," ","."," ","."," ","."," ","\\"," "},
        {"/"," ","."," ","."," ","."," ","."," ","."," ","."," ","\\"},
        {"\\"," "," ","."," ","."," ","."," ","."," ","."," "," ","/"},
        {"|"," "," "," "," "," "," "," "," "," "," "," "," "," "," "," ","|"},
        {"|","\\_","|","\\_","|","\\_","|","\\_","|","\\_","|"}
    };

    /**
     * Creates a {@code PlinkoGame}. A {@code PlinkoReward} is based on the guild ID.
     * 
     * @param event The MessageCreateEvent that prompted the creation of this {@code PlinkoGame}.
     */
    PlinkoGame(MessageCreateEvent event) {
        event.getGuildId().ifPresent(flake -> guild = flake.asString());
        channel = event.getMessage().getChannel().block();
        rand = new Random();
    }

    protected void play() {
        ((MessageChannel) getChannel()).createMessage("Placing initial chip:").block();
        int spot = 0;
        while (!board[0][spot].equals(" ")) {
            spot = rand.nextInt(1, 17);
        }
        board[0][spot] = "0";
        StringBuilder sBoard = new StringBuilder();
        for (String[] x : board) {
            for (String y : x) {
                sBoard.append(y);
            }
            sBoard.append("\n");
        }
        Message message = ((MessageChannel) getChannel()).createMessage(sBoard.toString()).block();
        //message.delete().block();

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
