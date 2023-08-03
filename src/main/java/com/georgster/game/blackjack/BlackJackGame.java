package com.georgster.game.blackjack;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.CardGame;
import com.georgster.game.card.CardDeck;
import com.georgster.game.card.PlayingCard;

public class BlackJackGame extends CardGame {

    private int dealerTotal;
    private int playerTotal;

    private boolean playerCanGo;

    enum Move {
        HIT,
        STAND,
        DOUBLE,
        SPLIT
    }
    
    public BlackJackGame(CommandExecutionEvent event) {
        super(event, 2);
        addAutomatedPlayer(2, "Dealer");

        this.dealerTotal = 0;
        this.playerTotal = 0;
        this.playerCanGo = true;
    }

    public CardDeck getDealerDeck() {
        return getPlayerDeck("Dealer");
    }

    public CardDeck getPlayerCards() {
        return getPlayerDeck(getOwner().getId().asString());
    }

    public void play() {
        getDealerDeck().getCard(0).show();
        getPlayerDeck(getOwner().getId().asString()).getCardList().forEach(PlayingCard::show);
    }

    public void processPlayerMove(Move move) {
        if (move == Move.HIT) {
            CardDeck deck = getPlayerCards();
            deck.transferTopCardFrom(getGlobalDrawingDeck());
            PlayingCard newCard = deck.peekTopCard();
            newCard.show();
        } else if (move == Move.STAND) {
            playerCanGo = false;
        }
    }

    public void processDealerTurn() {
        CardDeck deck = getDealerDeck();
        if (deck.size() == 2) deck.getCardList().forEach(PlayingCard::show);
        if (!dealerBusted()) {
            Move move = getDealerMove();
        }
    }

    public boolean playerCanGo() {
        return playerCanGo;
    }

    public boolean dealerBusted() {
        return dealerTotal > 21;
    }

    public boolean playerBusted() {
        return playerTotal > 21;
    }

    public int getDealerTotal() {
        return dealerTotal;
    }

    public int getPlayerTotal() {
        return playerTotal;
    }

    private Move getDealerMove() {
        if (dealerTotal < 17) {
            return Move.HIT;
        } else {
            return Move.STAND;
        }
    }

    public static int getCardValueAceOne(PlayingCard card) {
        String value = card.getValue();
        if (value.equalsIgnoreCase("J") || value.equalsIgnoreCase("Q") || value.equalsIgnoreCase("K")) {
            return 10;
        } else if (value.equalsIgnoreCase("A")) {
            return 1;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static int getCardValueAceEleven(PlayingCard card) {
        String value = card.getValue();
        if (value.equalsIgnoreCase("J") || value.equalsIgnoreCase("Q") || value.equalsIgnoreCase("K")) {
            return 10;
        } else if (value.equalsIgnoreCase("A")) {
            return 11;
        } else {
            return Integer.parseInt(value);
        }
    }
}
