package com.georgster.game.blackjack;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.CardGame;
import com.georgster.game.card.CardDeck;
import com.georgster.game.card.PlayingCard;

/**
 * A {@link CardGame} representing logic for a standard game of blackjack.
 * <p>
 * Uses a {@link BlackjackWizard} for front-end user input.
 * <p>
 * <b>Rules:</b>
 * <ul>
 * <li>Dealer must draw until 17</li>
 * <li>Can double for scores of 11 or under</li>
 * <li>Aces will be adjusted <i>(score of 11 -> 1)</i> if busting can be prevented</li>
 * </ul>
 */
public class BlackJackGame extends CardGame {

    private BlackjackWizard wizard;

    private int dealerTotal;
    private int playerTotal;

    private boolean playerCanGo;
    private boolean acesAreOne; // If false, aces are 11
    private boolean aceCanBeAdjusted;
    private boolean dealerAceCanBeAdjusted;

    /**
     * An Enumeration representing the moves of a game of Blackjack.
     */
    enum Move {
        HIT,
        STAND,
        DOUBLE,
        SPLIT
    }
    
    /**
     * Creates a new BlackJack game with the provided entry amount as the wager.
     * 
     * @param event The event that prompted the game's creation.
     * @param entryAmount The player's wager.
     */
    public BlackJackGame(CommandExecutionEvent event, long entryAmount) {
        super(event, 2, true, entryAmount);
        addAutomatedPlayer(2, "Dealer");

        this.dealerTotal = 0;
        this.playerTotal = 0;
        this.playerCanGo = true;
        this.acesAreOne = true;
        this.aceCanBeAdjusted = false;
        this.dealerAceCanBeAdjusted = false;

        this.wizard = new BlackjackWizard(event, this);
    }

    /**
     * Returns the available moves for the player
     * in the current game state.
     * 
     * @return The current available player moves.
     */
    public Move[] getAvailablePlayerMoves() {
        if (!playerCanGo) return new Move[0];

        if (getPlayerTotal() <= 11 && getOwnerProfile().getBank().hasBalance(getEntryAmount())) {
            return new Move[] {Move.HIT, Move.STAND, Move.DOUBLE};
        }

        return new Move[] {Move.HIT, Move.STAND};
    }

    /**
     * Returns the dealer's deck of cards.
     * 
     * @return The dealer's deck of cards.
     */
    public CardDeck getDealerDeck() {
        return getPlayerDeck("Dealer");
    }

    /**
     * Returns the player's deck of cards.
     * 
     * @return The player's deck of cards.
     */
    public CardDeck getPlayerCards() {
        return getPlayerDeck(getOwner().getId().asString());
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Processes the provided move as the player's move
     * and updates the game's state accordingly.
     * 
     * @param move The player's move.
     */
    public void processPlayerMove(Move move) {
        if (move == Move.HIT) {
            CardDeck deck = getPlayerCards();
            deck.transferTopCardFrom(getGlobalDrawingDeck());
            PlayingCard newCard = deck.peekTopCard();
            newCard.show();
            addCardValuePlayer(newCard);
            if (playerBusted()) end();
            if (getPlayerTotal() == 21 && getPlayerCards().size() == 2 && getDealerTotal() < 21)
                end();
        } else if (move == Move.DOUBLE) {
            withdrawlEntryAmount(); // Withdrawls the old entry fee again (essentially doubling it)
            setEntryAmount(getEntryAmount() * 2); // Updates the entry fee to double the old one

            CardDeck deck = getPlayerCards();
            deck.transferTopCardFrom(getGlobalDrawingDeck());
            PlayingCard newCard = deck.peekTopCard();
            newCard.show();
            addCardValuePlayer(newCard);
            if (playerBusted()) end();
            playerCanGo = false; // Player only gets one more card on double
        } else if (move == Move.STAND) {
            playerCanGo = false;
        }

        updateGameReward();
    }

    /**
     * Processes one turn of the dealer based on the current game state.
     */
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
                if (dealerTotal >= 17) end();
            }
        }

        updateGameReward();
    }

    /**
     * Updates the {@link DiscordGame} reward based on the current game state.
     */
    private void updateGameReward() {
        if (playerWon()) {
            if (getPlayerTotal() == 21 && getPlayerCards().size() == 2) {
                setRewardAmount((long) (getEntryAmount() + (getEntryAmount() * 1.5)));
            } else {
                setRewardAmount(getEntryAmount() * 2);
            }
            if (getPlayerTotal() == 21 && getEntryAmount() == 0) {
                setRewardAmount(5);
            }
        } else if (dealerWon()) {
            setRewardAmount(0);
        } else {
            setRewardAmount(getEntryAmount());
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

    /**
     * Adds the value of the card to the player's total,
     * making any adjustments necessary.
     * 
     * @param card The card to add to the player's total.
     */
    private void addCardValuePlayer(PlayingCard card) {
        if ((!card.getValue().equalsIgnoreCase("A"))
            && (getPlayerCards().getSubDeck(1, getPlayerCards().size()).containsValue("A") && ((playerTotal + getCardValueAceOne(card) - 10) <= 21) && aceCanBeAdjusted)) {
            playerTotal -= 10;
            aceCanBeAdjusted = false;
        }

        if (getPlayerTotal() > 10) {
            playerTotal += getCardValueAceOne(card);
        } else {
            if (card.getValue().equalsIgnoreCase("A") && !acesAreOne) {
                aceCanBeAdjusted = true;
            }

            playerTotal += acesAreOne ? getCardValueAceOne(card) : getCardValueAceEleven(card);
        }
    }

    /**
     * Adds the value of the card to the dealer's total,
     * making any adjustments necessary.
     * 
     * @param card The card to add to the dealer's total.
     */
    private void addCardValueDealer(PlayingCard card) {
        if ((!card.getValue().equalsIgnoreCase("A"))
            && (getDealerDeck().getSubDeck(1, getDealerDeck().size()).containsValue("A") && ((dealerTotal + getCardValueAceOne(card) - 10) <= 21) && dealerAceCanBeAdjusted)) {
            dealerTotal -= 10;
            dealerAceCanBeAdjusted = false;
        }

        if (dealerTotal <= 10) {
            if (card.getValue().equalsIgnoreCase("A")) {
                dealerAceCanBeAdjusted = true;
            }
            dealerTotal += getCardValueAceEleven(card);
        } else {
            dealerTotal += getCardValueAceOne(card);
        }
    }

    /**
     * Returns true if the player can currently go, false otherwise.
     * 
     * @return True if the player can currently go, false otherwise.
     */
    public boolean playerCanGo() {
        return playerCanGo;
    }

    /**
     * Returns true if the dealer can currently go, false otherwise.
     * 
     * @return True if the dealer can currently go, false otherwise.
     */
    public boolean dealerCanGo() {
        return getDealerTotal() < 17 && isActive();
    }

    /**
     * Returns true if the dealer has busted, false otherwise.
     * 
     * @return True if the dealer has busted, false otherwise.
     */
    public boolean dealerBusted() {
        return dealerTotal > 21;
    }

    /**
     * Returns true if the player has busted, false otherwise.
     * 
     * @return True if the player has busted, false otherwise.
     */
    public boolean playerBusted() {
        return playerTotal > 21;
    }

    /**
     * Returns the total of the dealer's shown cards.
     * 
     * @return The total of the dealer's shown cards.
     */
    public int getDealerTotal() {
        int total = dealerTotal;
        for (PlayingCard card : getDealerDeck().getCardList()) {
            if (card.isFaceDown()) {
                if (total <= 10) {
                    total -= getCardValueAceOne(card);
                } else {
                    total -= getCardValueAceEleven(card);
                }
            }
        }
        return total < 0 ? 0 : total; // Edge case where there are cards, but they haven't been added to the total yet
    }

    /**
     * Returns the total of the player's shown cards.
     * @return The total of the player's shown cards.
     */
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
        return total < 0 ? 0 : total; // Edge case where there are cards, but they haven't been added to the total yet
    }

    /**
     * Returns the correct move for the dealer based on the current game state.
     * 
     * @return The correct move for the dealer based on the current game state.
     */
    private Move getDealerMove() {
        if (dealerTotal < 17) {
            return Move.HIT;
        } else {
            return Move.STAND;
        }
    }

    /**
     * Returns the value of the provided playing card if aces are treated as ones.
     * 
     * @param card The card to get the value of.
     * @return The value of the card.
     */
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

    /**
     * Returns the value of the provided playing card if aces are treated as elevens.
     * 
     * @param card The card to get the value of.
     * @return The value of the card.
     */
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

    /**
     * Returns true if the player has won, false otherwise.
     * 
     * @return Returns true if the player has won, false otherwise.
     */
    public boolean playerWon() {
        return (!playerBusted() && dealerBusted()) || ((playerTotal > dealerTotal) && !playerBusted());
    }

    /**
     * Returns true if the dealer has won, false otherwise.
     * 
     * @return Returns true if the dealer has won, false otherwise.
     */
    public boolean dealerWon() {
        return (playerBusted() && !dealerBusted()) || ((playerTotal < dealerTotal) && !dealerBusted());
    }

    /**
     * Returns the cards of this game as a String.
     * 
     * @return The cards of this game as a String.
     */
    public String getCardsAsString() {
        StringBuilder sb = new StringBuilder("Dealer cards:\n");
        getDealerDeck().getCardStack().forEach(card -> sb.append(card.isFaceDown() ? "**F** | " : "**" + card.getValue() + "** | "));
        sb.append("(" + getDealerTotal() + ")\n");
        sb.append("Player cards:\n");
        getPlayerCards().getCardStack().forEach(card -> sb.append(card.isFaceDown() ? "**F** | " : "**" + card.getValue() + "** | "));
        sb.append("(" + getPlayerTotal() + ")");

        return sb.toString();
    }
}
