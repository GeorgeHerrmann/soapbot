package com.georgster.game.card;

import java.util.ArrayList;
import java.util.List;

import com.georgster.game.CardGame;

/**
 * A {@link CardDeck} that all players of a {@link CardGame}, or any
 * other {@link CardDeck} environment may use.
 */
public class GlobalCardDeck extends CardDeck {

    /**
     * Creates a GlobalCardDeck that will be empty if {@code empty} is true,
     * otherwise populating with the cards of a standard card deck.
     * 
     * @param empty True if this deck should be empty, false otherwise.
     */
    public GlobalCardDeck(boolean empty) {
        super(generateStandardDeck(empty));
    }

    /**
     * Generates a standard deck of playing cards if {@link !empty},
     * or an empty deck otherwise.
     * 
     * @param empty True if this deck should be empty, false otherwise.
     * @return The array deck of cards.
     */
    private static PlayingCard[] generateStandardDeck(boolean empty) {
        if (empty) return new PlayingCard[0];
        List<PlayingCard> deck = new ArrayList<>();
        
        for (CardSuit suit : CardSuit.values()) {
            for (String value : getCardValues()) {
                PlayingCard card = new PlayingCard(suit, value);
                deck.add(card);
            }
        }

        return deck.toArray(new PlayingCard[deck.size()]);
    }

    /**
     * Returns the card values of a standard card deck.
     * 
     * @return The card values of a standard card deck.
     */
    private static String[] getCardValues() {
        return new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    }
}
