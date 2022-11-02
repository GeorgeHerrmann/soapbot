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
        int spot = rand.nextInt(1,17);
        System.out.println("Initial Location at 0, " + spot);

        StringBuilder sBoard = new StringBuilder();
        for (String[] x : board) {
            for (String y : x) {
                sBoard.append(y);
            }
            sBoard.append("\n");
        }

        Message message = ((MessageChannel) getChannel()).createMessage(sBoard.toString()).block();

        for (int i = 0; i < board.length - 1; i++) {
            sBoard.setCharAt(spot + (i * board[i].length), '0');
            spot = nextIndex(i + 1, spot);
            message.edit(spec -> {
                spec.setContent(sBoard.toString());
            }).block();
            //sBoard.replace(sBoard.toString().indexOf("0"), sBoard.toString().indexOf("0") + 1, " ");
        }
        ((MessageChannel) getChannel()).createMessage("done.").block();

    }

    
    private int nextIndex(int first, int second) {
        System.out.println("Examining: " + first + ", " + second);
        int nextIndex = second;
        if (board[first][second].equals("|")) {
            System.out.print("\tI am initially at a |: ");
            while (board[first][nextIndex].equals("|")) {
                nextIndex = rand.nextInt(nextIndex - 1, nextIndex + 2);
            }
            System.out.println("New Spot: " + first + ", " + nextIndex);
        }
            
        if (board[first][second - 1].equals("/") || board[first][second + 1].equals("/")) {
            System.out.print("\tI found a /:");
            nextIndex = second < 5 ? second + 2 : second - 2;
            System.out.println("I am now at: " + first + ", " + nextIndex);
        }
        if (board[first][nextIndex].equals(".")) {
            System.out.print("\tI found a . : ");
            while (board[first][nextIndex].equals(".")) {
                nextIndex = rand.nextInt(nextIndex - 1, nextIndex + 2);
            }
            System.out.println("I have placed myself at: " + first + ", " + nextIndex);
        
        }
        return nextIndex;
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
