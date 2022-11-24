package com.georgster.plinko;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.Message;

import java.time.Duration;
import java.util.Random;

/**
 * A PlinkoGame represents a game of plinko that is created on a "!plinko play:" command.
 * Each PlinkoGame is associated with the following:
 *  - The guild ID where the game existed.
 *  - The Channel the game was played in.
 *  - A PlinkoReward based on where the chip landed.
 */
public class PlinkoGame {

    private String guild; //The ID of the guild the game was played in
    private Channel channel; //The channel where the game existed
    private Random rand; //Keeps track of the random elements of the game
    /*
     * A 2D array representation of the Plinko board. PlinkoGame
     * uses this array to quickly determine the next location of the chip,
     * then translates this board to a string for the Discord API call.
     */
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
        {"I","_","I","_","I","_","I","_","I","_","I"," "," "," "," "," "," "}
    };

    /**
     * Creates a {@code PlinkoGame} where the {@code MessageCreateEvent} was fired.
     * 
     * @param event The {@code MessageCreateEvent} that prompted the creation of this {@code PlinkoGame}.
     */
    PlinkoGame(MessageCreateEvent event) {
        event.getGuildId().ifPresent(flake -> guild = flake.asString()); //Translates the guild ID from a Snowflake to a string
        channel = event.getMessage().getChannel().block();
        rand = new Random();
    }

    /**
     * Simulates the {@code PlinkoGame} inside the {@code Channel} it was started.
     */
    protected void play() {
        int spot = rand.nextInt(2,14); //Initially, spot randomly generates the first location for the Plinko chip

        /* sBoard will be used to hold the string representation of the plinko game, which will be used for the API call */
        StringBuilder sBoard = new StringBuilder();
        for (String[] x : board) { //Initial conversion of the board to a string
            for (String y : x) {
                sBoard.append(y);
            }
            sBoard.append("\n");
        }
        sBoard.setCharAt(spot, '0'); //Places the chip
        Message message = ((MessageChannel) channel).createMessage(sBoard.toString()).block(); //Creates the initial message of the board state
        /* The handling of the creation of the plinko game is different than updating it, therefore we do these updates outside the loop */
        sBoard.replace(sBoard.toString().indexOf("0"), sBoard.toString().indexOf("0") + 1, " ");

        /*
         * Instead of updating the location of the plinko chip inside the 2d array each iteration and converting that to a string every time (O(n^2))
         * we can get the index of where that location in the 2d array is inside of sBoard, then "place" the chip at that location and make an API
         * call to update the message. It determines this index by doing the following:
         *  - Asking nextIndex where the next location of the chip should be based on where it is now.
         *  - nextIndex returns the index of the x and y axis added together.
         *  - Determining where the row (or y axis) is inside of sBoard by:
         *      -  multiplying the total number of rows by which row it is currently at
         *      - then adding the x (or column) value to that.
         *  - It edits that message after a delay of 500ms (to prevent overloading the api with edit calls), then reverts where the chip was back to the char it had before
         */
        for (int i = 0; i < board.length; i++) {
            spot = nextIndex(i, spot - (i - 1));
            sBoard.setCharAt(spot + (i * board[i].length), '0'); //Note we don't multiply by board[i].length - 1 because we must include the \n character inside sBoard
            message.edit().withContentOrNull(sBoard.toString()).delayElement(Duration.ofMillis(500)).block();
            if (i != board.length - 1) { //We want to keep the chip in the final spot once the game is over
                sBoard.replace(sBoard.toString().indexOf("0"), sBoard.toString().indexOf("0") + 1, " "); //Easy way to find where the chip is in sBoard
            } else {
                getReward(spot);
            }
        }

    }
    
    /**
     * Determines where the next index of the Plinko chip should be inside this {@code PlinkoGame}.
     * 
     * @param first the row or y-axis value of where the chip currently is
     * @param second the column or x-axis value of where the chip currently is
     * @return The index of where the chip should be in the next iteration of the game.
     */
    private int nextIndex(int first, int second) {
        if (second > board.length - 3) second -= 3; //On the off chance the chip goes out of bounds, this pulls it back within the scope of the board
        int nextIndex = second; //nextIndex is what will ultimately hold the next location of the chip throughout this method
        if (first == board.length - 1) { //This means we're at the end of the game and must determine which "hole" the chip should go into
            while (!board[first][nextIndex].equals("_")) { //It randomly moves it around until it falls into one of the slots (a "_" character)
                nextIndex = rand.nextInt(nextIndex - 1, nextIndex + 2);
            }
        }
        
        /* If we hit a "/" it's like hitting an "edge" of a real board, so we bounce it back a few indecies */
        if (board[first][second + 1].equals("/") || board[first][second].equals("/")) {
            nextIndex = second - 2;
        }
        /* Same thing on the other side of the board */
        if (board[first][second - 1].equals("\\") || board[first][second].equals("\\")) {
            nextIndex = second + 2;
        }
        /* If we hit a "." we must randomly move it around until it is clear of any other dots */
        if (board[first][nextIndex].equals(".")) {
            while (board[first][nextIndex].equals(".")) {
                nextIndex = rand.nextInt(nextIndex - 1, nextIndex + 2);
            }
        }
        /* We add the number of rows because we must take into account the number of new line characters that will be in the string representation of the board */
        return nextIndex + first;
    }

    protected int getReward(int spot) {
        return 0;
    }

    /* Shows a blank version of the Plinko Board. */
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
