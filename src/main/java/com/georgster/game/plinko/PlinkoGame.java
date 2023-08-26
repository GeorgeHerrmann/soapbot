package com.georgster.game.plinko;

import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.presence.Status.Platform;
import discord4j.core.object.entity.Message;

import java.time.Duration;
import java.util.Random;

import com.georgster.api.ActionWriter;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.DiscordGame;
import com.georgster.profile.UserProfile;
import com.georgster.util.GuildInteractionHandler;

/**
 * A basic game of Plinko simulated in a {@code TextChannel}.
 */
public class PlinkoGame extends DiscordGame {

    private Channel channel; //The channel where the game existed
    private Random rand; //Keeps track of the random elements of the game
    private final GuildInteractionHandler handler;
    private final Platform platform;
    private final UserProfileManager profileManager;
    private final UserProfile profile;
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
     * Creates a {@code PlinkoGame} in the {@link Channel} in the execution event.
     * 
     * @param event The event that prompted the creation of this {@code PlinkoGame}.
     */
    protected PlinkoGame(CommandExecutionEvent event) {
        super(event);
        channel = event.getDiscordEvent().getChannel();
        rand = new Random();
        this.platform = event.getDiscordEvent().getPlatform();
        this.handler = event.getGuildInteractionHandler();
        this.profileManager = event.getUserProfileManager();
        this.profile = profileManager.get(event.getDiscordEvent().getAuthorAsMember().getId().asString());
    }

    /**
     * {@inheritDoc}
     */
    protected void play() {
        ActionWriter.writeAction("Beginning setup of a PlinkoGame");
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
        Message message = null;
        if (platform == Platform.MOBILE) {
            String mobileOutput = "Playing a game of plinko...\n" + 0 + "/" + board.length + " Rows completed\n";
            mobileOutput += "*The game is not shown because " + profile.getUsername() + " is using a mobile device*";
            message = handler.sendPlainText(mobileOutput); //Creates the initial message of the board state
        } else {
            message = handler.sendPlainText(sBoard.toString()); //Creates the initial message of the board state
        }
        /* The handling of the creation of the plinko game is different than updating it, therefore we do these updates outside the loop */
        sBoard.replace(sBoard.toString().indexOf("0"), sBoard.toString().indexOf("0") + 1, " ");

        /*
         * Instead of updating the location of the plinko chip inside the 2d array each iteration and converting that to a string every time (O(n^2))
         * we can get the index of where that location in the 2d array is inside of sBoard, then "place" the chip at that location and make an API
         * call to update the message. It determines this index by doing the following:
         *  - Asking nextIndex where the next location of the chip should be based on where it is now.
         *  - nextIndex returns the index of the x and y axis added together.
         *  - Determining where the row (or y axis) is inside of sBoard by:
         *      - multiplying the total number of rows by which row it is currently at
         *      - then adding the x (or column) value to that.
         *  - It edits that message after a delay of 500ms (to prevent overloading the api with edit calls), then reverts where the chip was back to the char it had before
         */
        for (int i = 0; i < board.length; i++) {
            spot = nextIndex(i, spot - (i - 1));
            sBoard.setCharAt(spot + (i * board[i].length), '0'); //Note we don't multiply by board[i].length - 1 because we must include the \n character inside sBoard

            if (platform == Platform.MOBILE) {
                String mobileOutput = "Playing a game of plinko...\n" + (i + 1) + "/" + board.length + " Rows completed\n";
                mobileOutput += "*The game is not shown because " + profile.getUsername() + " is using a mobile device*";
                message.edit().withContentOrNull(mobileOutput).delayElement(Duration.ofMillis(500)).block();
            } else {
                message.edit().withContentOrNull(sBoard.toString()).delayElement(Duration.ofMillis(500)).block();
            }

            ActionWriter.writeAction("Updating the Plinko Board in a plinko game and sending the request to Discord's API");
            if (i != board.length - 1) { //We want to keep the chip in the final spot once the game is over
                sBoard.replace(sBoard.toString().indexOf("0"), sBoard.toString().indexOf("0") + 1, " "); //Easy way to find where the chip is in sBoard
            } else {
                ActionWriter.writeAction("Determining the reward for a Plinko Game");
            }
        }
        long reward = calculateReward(spot - 11);
        handler.sendText("Your reward is " + reward + " coins", "Plinko");
        profile.getBank().deposit(reward);
        profileManager.update(profile);
    }
    
    /**
     * Determines where the next index of the Plinko chip should be inside this {@code PlinkoGame}.
     * 
     * @param first the row or y-axis value of where the chip currently is
     * @param second the column or x-axis value of where the chip currently is
     * @return The index of where the chip should be in the next iteration of the game.
     */
    private int nextIndex(int first, int second) {
        ActionWriter.writeAction("Calculating the next location of the Plinko Chip in a Plinko Game");
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

    /**
     * Calculates and returns the proper reward amount based on the final x-index of the Plinko Chip.
     * 
     * @param finalIndex The final x-index of the Plinko Chip.
     * @return The reward amount.
     * @throws IllegalArgumentException If the final index is invalid.
     */
    private long calculateReward(int finalIndex) throws IllegalArgumentException {
        if (finalIndex == 1 || finalIndex == 9) {
            return 5;
        } else if (finalIndex == 3 || finalIndex == 7) {
            return 20;
        } else if (finalIndex == 5) {
            return 50;
        }
        throw new IllegalArgumentException("Incorrect final index: " + finalIndex + ", current final indexes are 1, 3, 5, 7 & 9");
    }

    /**
     * Shows a blank version of the Plinko Board.
     */
    protected void showBoard() {
        StringBuilder sBoard = new StringBuilder();
        for (String[] x : board) {
            for (String y : x) {
                sBoard.append(y);
            }
            sBoard.append("\n");
        }

        handler.sendPlainText(sBoard.toString());
    }

    /**
     * Returns the channel where this {@code PlinkoGame} is active.
     * 
     * @return The channel where this {@code PlinkoGame} is active.
     */
    protected Channel getChannel() {
        return channel;
    }

}
