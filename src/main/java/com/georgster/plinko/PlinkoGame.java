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
        {"\\"," ","."," ","."," ","."," ","."," ","."," ","."," ","/"," "," "},
        {" ","\\"," ","."," ","."," ","."," ","."," ","."," ","/"," "," "," "},
        {" ","/"," "," ","."," ","."," ","."," ","."," "," ","\\"," "," "," "},
        {"/"," ","."," ","."," ","."," ","."," ","."," ","."," ","\\"," "," "},
        {"\\"," "," ","."," ","."," ","."," ","."," ","."," "," ","/"," "," "},
        {" ","\\"," "," ","."," ","."," ","."," ","."," "," ","/"," "," "," "},
        {" ","/"," ","."," ","."," ","."," ","."," ","."," ","\\"," "," "," "},
        {"/"," ","."," ","."," ","."," ","."," ","."," ","."," ","\\"," "," "},
        {"\\"," "," ","."," ","."," ","."," ","."," ","."," "," ","/"," "," "},
        {"|"," "," "," "," "," "," "," "," "," "," "," "," "," "," "," ","|"},
        {"|","\\_","|","\\_","|","\\_","|","\\_","|","\\_","|"," "," "," "," "," "," "}
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
        int spot = rand.nextInt(2,15);

        StringBuilder sBoard = new StringBuilder();
        for (String[] x : board) {
            for (String y : x) {
                sBoard.append(y);
            }
            sBoard.append("\n");
        }
        sBoard.setCharAt(spot, '0');
        Message message = ((MessageChannel) getChannel()).createMessage(sBoard.toString()).block();
        sBoard.replace(sBoard.toString().indexOf("0"), sBoard.toString().indexOf("0") + 1, " ");

        for (int i = 0; i < board.length; i++) {
            spot = nextIndex(i, spot - (i - 1));
            sBoard.setCharAt(spot + (i * board[i].length), '0');
            if (i == board.length - 1) {
                System.out.println("Play reached the end");
                if (sBoard.charAt((spot + (i * board[i].length)) - 1) == '|') {
                    System.out.println("\tRemove to the left");
                    sBoard.deleteCharAt((spot + (i * board[i].length)) - 1);
                } else {
                    System.out.println("\tRemove to the right");
                    sBoard.deleteCharAt((spot + (i * board[i].length)) + 1);
                }
            }
            message.edit(spec -> {
                spec.setContent(sBoard.toString());
            }).block();
            if (i != board.length - 1) {
                sBoard.replace(sBoard.toString().indexOf("0"), sBoard.toString().indexOf("0") + 1, " ");
            }
        }
        ((MessageChannel) getChannel()).createMessage("done.").block();

    }

    
    private int nextIndex(int first, int second) {
        int nextIndex = second;
        if (first == board.length - 1) {
            while (board[first][nextIndex].equals("|")) {
                nextIndex = rand.nextInt(nextIndex - 1, nextIndex + 2);
            }
            nextIndex += (second / 2);
        } else {
            
        if (board[first][second + 1].equals("/")) {
            nextIndex = second - 2;
        }
        if (board[first][second - 1].equals("\\")) {
            nextIndex = second + 2;
        }
        if (board[first][nextIndex].equals(".")) {
            while (board[first][nextIndex].equals(".")) {
                nextIndex = rand.nextInt(nextIndex - 1, nextIndex + 2);
            }
        }
    }
        return nextIndex + first;
    }

    protected void showBoard() {
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
