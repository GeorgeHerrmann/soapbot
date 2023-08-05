package com.georgster.game.blackjack;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.CardGame;
import com.georgster.game.card.CardDeck;
import com.georgster.game.card.PlayingCard;

public class BlackJackGame extends CardGame {

    private BlackjackWizard wizard;

    private int dealerTotal;
    private int playerTotal;

    private boolean playerCanGo;
    private boolean acesAreOne; // If false, aces are 11

    enum Move {
        HIT,
        STAND,
        DOUBLE,
        SPLIT
    }
    
    public BlackJackGame(CommandExecutionEvent event) {
        super(event, 2, true);
        addAutomatedPlayer(2, "Dealer");

        this.dealerTotal = 0;
        this.playerTotal = 0;
        this.playerCanGo = true;
        this.acesAreOne = true;

        this.wizard = new BlackjackWizard(event, this);
    }

    public Move[] getAvailablePlayerMoves() {
        if (!playerCanGo) return new Move[0];

        return new Move[] {Move.HIT, Move.STAND};
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

        if (getPlayerCards().containsValue("A")) {
            wizard.promptAceSelection();
        }

        getDealerDeck().getCardStack().forEach(this::addCardValueDealer);
        getPlayerCards().getCardStack().forEach(this::addCardValuePlayer);

        wizard.begin();
    }

    public void processPlayerMove(Move move) {
        if (move == Move.HIT) {
            CardDeck deck = getPlayerCards();
            deck.transferTopCardFrom(getGlobalDrawingDeck());
            PlayingCard newCard = deck.peekTopCard();
            newCard.show();
            addCardValuePlayer(newCard);
            if (playerBusted()) end();
        } else if (move == Move.STAND) {
            playerCanGo = false;
        }
    }

    public void processDealerTurn() {
        CardDeck deck = getDealerDeck();
        if (deck.size() == 2) deck.getCardList().forEach(PlayingCard::show);
        if (dealerCanGo()) {
            Move move = getDealerMove();
            if (move == Move.HIT) {
                deck.transferTopCardFrom(getGlobalDrawingDeck());
                PlayingCard newCard = deck.peekTopCard();
                newCard.show();
                addCardValueDealer(newCard);
                if (dealerBusted()) end();
                System.out.println("New dealer total: " + dealerTotal);
                if (dealerTotal >= 17) end();
            }
        }
    }

    /**
     * Sets whether drawn aces by the player will be counted as a one (true),
     * or an eleven (false).
     * 
     * @param ones True to count player-drawn aces as ones, false for elevens.
     */
    public void setPlayerAcesValue(boolean ones) {
        this.acesAreOne = ones;
    }

    private void addCardValuePlayer(PlayingCard card) {
        playerTotal += acesAreOne ? getCardValueAceOne(card) : getCardValueAceEleven(card);
    }

    private void addCardValueDealer(PlayingCard card) {
        if (dealerTotal <= 10) {
            dealerTotal += getCardValueAceEleven(card);
        } else {
            dealerTotal += getCardValueAceOne(card);
        }
    }

    public boolean playerCanGo() {
        return playerCanGo;
    }

    public boolean dealerCanGo() {
        return getDealerTotal() < 17 && isActive();
    }

    public boolean dealerBusted() {
        return dealerTotal > 21;
    }

    public boolean playerBusted() {
        return playerTotal > 21;
    }

    public int getDealerTotal() {
        int total = dealerTotal;
        for (PlayingCard card : getDealerDeck().getCardList()) {
            if (card.isFaceDown()) {
                if (total <= 10) {
                    total -= getCardValueAceEleven(card);
                } else {
                    total -= getCardValueAceOne(card);
                }
            }
        }
        return total < 0 ? 0 : total;
    }

    public int getPlayerTotal() {
        int total = playerTotal;
        for (PlayingCard card : getPlayerCards().getCardList()) {
            if (card.isFaceDown()) {
                if (!acesAreOne) {
                    total -= getCardValueAceEleven(card);
                } else {
                    total -= getCardValueAceOne(card);
                }
            }
        }
        return total < 0 ? 0 : total;
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

    public boolean playerWon() {
        return (!playerBusted() && dealerBusted()) || ((playerTotal > dealerTotal) && !playerBusted());
    }

    public boolean dealerWon() {
        return (playerBusted() && !dealerBusted()) || ((playerTotal < dealerTotal) && !dealerBusted());
    }

    public String getCardsAsString() {
        StringBuilder sb = new StringBuilder("Dealer cards:\n");
        getDealerDeck().getCardStack().forEach(card -> sb.append(card.isFaceDown() ? "F | " : card.getValue() + " | "));
        sb.append("(" + getDealerTotal() + ")\n");
        sb.append("Player cards:\n");
        getPlayerCards().getCardStack().forEach(card -> sb.append(card.isFaceDown() ? "F | " : card.getValue() + " | "));
        sb.append("(" + getPlayerTotal() + ")");

        return sb.toString();
    }
}