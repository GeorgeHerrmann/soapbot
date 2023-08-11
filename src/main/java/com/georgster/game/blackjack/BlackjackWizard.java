package com.georgster.game.blackjack;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.blackjack.BlackJackGame.Move;
import com.georgster.util.commands.wizard.InputWizard;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

/**
 * A Wizard to handle the user-facing side of the {@link BlackJackGame}.
 * This wizard effectively drives the {@link BlackJackGame}.
 */
public class BlackjackWizard extends InputWizard {

    private BlackJackGame game;

    /**
     * Creates a BlackjackWizard for the provided game.
     * 
     * @param event The event that prompted the wizard's creation.
     * @param game The game this wizard will control.
     */
    public BlackjackWizard(CommandExecutionEvent event, BlackJackGame game) {
        super(event,
        InputListenerFactory.createButtonMessageListener(event, "Blackjack (" + game.getEntryAmount() + " coins)").builder().withXReaction(false).withTimeoutDuration(300000).requireMatch(true, true).build());
        this.game = game;
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("playerTurn");
    }

    /**
     * The window for the player's turn.
     */
    public void playerTurn() {
        if (game.isActive()) { // Ensures the game is running
            if (game.playerCanGo()) { // If the player can go, we run their turn.
                StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
                prompt.append("What would you like to do");

                String[] options = getAvailableMoveOptions();

                withResponse((response -> {
                    if (response.equalsIgnoreCase("double"))
                        getInputListener().setTitle("Blackjack (" + (game.getEntryAmount() * 2) + " coins)");

                    if (response.equalsIgnoreCase("hit") || response.equalsIgnoreCase("double"))
                        processCardDrawAces();
                    game.processPlayerMove(Move.valueOf(response.toUpperCase()));
                    nextWindow("playerTurn");
                }), false, prompt.toString(), options);
            } else { // Otherwise its the dealers turn
                dealerTurn();
            }
        } else { // Otherwise we display the result
            StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
            if (game.dealerWon()) {
                prompt.append("*Dealer wins, you lost your wager*");
            } else if (game.playerWon()) {
                prompt.append("*You won **" + game.getRewardAmount() + "** coins!*");
            } else {
                prompt.append("*Draw, your wager has been refunded*");
            }
            getInputListener().editCurrentMessageContent(prompt.toString());
            shutdown();
        }
    }

    /**
     * The window for a dealer's turn.
     */
    public void dealerTurn() {
        while (game.dealerCanGo()) { // Runs the dealer's turn until they can no longer go.
            game.processDealerTurn();
            getInputListener().editCurrentMessageContentDelay(game.getCardsAsString(), 500);
        }

        StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
        if (game.dealerWon()) {
            prompt.append("*Dealer wins, you lost your wager*");
        } else if (game.playerWon()) {
            prompt.append("*You won **" + game.getRewardAmount() + "** coins!*");
        } else {
            prompt.append("*Draw, your wager has been refunded*");
        }
        getInputListener().editCurrentMessageContent(prompt.toString());
        shutdown();
    }

    /**
     * Prompts the user to select a value for an Ace if the next card on the
     * drawing deck is an ace.
     */
    private void processCardDrawAces() {
        if (game.getGlobalDrawingDeck().peekTopCard().getValue().equalsIgnoreCase("A")) {
            if (game.getPlayerTotal() > 10) {
                game.setPlayerAcesValue(true);
            } else {
                StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
                prompt.append("You have drawn an ace, would you like this to be a 1 or an 11");
                withResponse((response -> {
                    if (response.equals("1")) {
                        game.setPlayerAcesValue(true);
                    } else if (response.equals("11")) {
                        game.setPlayerAcesValue(false);
                    }
                }) , false, prompt.toString(), "1", "11");
            }
        }
    }

    /**
     * Prompts the user to select a value for their ace unconditionally.
     */
    public void promptAceSelection() {
        StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
        prompt.append("You have drawn an ace, would you like this to be a 1 or an 11");
        withResponse((response -> {
            if (response.equals("1")) {
                game.setPlayerAcesValue(true);
            } else if (response.equals("11")) {
                game.setPlayerAcesValue(false);
            }
        }) , false, prompt.toString(), "1", "11");
    }

    /**
     * Returns the availble moves for the game as an array of strings.
     * 
     * @return The availble moves for the game as an array of strings.
     */
    private String[] getAvailableMoveOptions() {
        Move[] moves = game.getAvailablePlayerMoves();
        String[] options = new String[moves.length];

        for (int i = 0; i < moves.length; i++) {
            options[i] = moves[i].toString();
        }

        return options;
    }
}
