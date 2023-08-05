package com.georgster.game.blackjack;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.blackjack.BlackJackGame.Move;
import com.georgster.util.commands.wizard.InputWizard;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

public class BlackjackWizard extends InputWizard {

    private BlackJackGame game;
    
    public BlackjackWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Blackjack").builder().withXReaction(false).withTimeoutDuration(300000).build());
        this.game = new BlackJackGame(event);
    }

    public BlackjackWizard(CommandExecutionEvent event, BlackJackGame game) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Blackjack").builder().withXReaction(false).withTimeoutDuration(300000).build());
        this.game = game;
    }

    public void begin() {
        nextWindow("playerTurn");
    }

    public void playerTurn() {
        System.out.println("Player can go: " + game.playerCanGo());
        System.out.println("Game is active: " + game.isActive());
        if (game.isActive()) {
            if (game.playerCanGo()) {
                StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
                prompt.append("What would you like to do");

                String[] options = getAvailableMoveOptions();

                withResponse((response -> {
                    if (response.equalsIgnoreCase("hit")) processCardDrawAces();
                    game.processPlayerMove(Move.valueOf(response.toUpperCase()));
                    nextWindow("playerTurn");
                }), false, prompt.toString(), options);
            } else {
                System.out.println("Dealer turn");
                dealerTurn();
            }
        } else {
            StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
            if (game.dealerWon()) {
                prompt.append("Dealer wins");
            } else if (game.playerWon()) {
                prompt.append("You won!");
            } else {
                prompt.append("Draw");
            }
            getInputListener().editCurrentMessageContent(prompt.toString());
            shutdown();
        }
    }

    public void dealerTurn() {
        while (game.dealerCanGo()) {
            game.processDealerTurn();
            getInputListener().editCurrentMessageContentDelay(game.getCardsAsString(), 500);
        }

        StringBuilder prompt = new StringBuilder(game.getCardsAsString() + "\n");
        if (game.dealerWon()) {
            System.out.println(game.getDealerTotal() + " " + game.getPlayerTotal());
            prompt.append("Dealer wins");
        } else if (game.playerWon()) {
            prompt.append("You won!");
        } else {
            prompt.append("Draw");
        }
        getInputListener().editCurrentMessageContent(prompt.toString());
        shutdown();
    }

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

    private String[] getAvailableMoveOptions() {
        Move[] moves = game.getAvailablePlayerMoves();
        String[] options = new String[moves.length];

        for (int i = 0; i < moves.length; i++) {
            options[i] = moves[i].toString();
        }

        return options;
    }
}
