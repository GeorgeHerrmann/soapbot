package com.georgster.game.card;

import java.util.ArrayList;
import java.util.List;

public class GlobalCardDeck extends CardDeck {

    public GlobalCardDeck(boolean empty) {
        super(generateStandardDeck(empty));
    }

    private static PlayingCard[] generateStandardDeck(boolean empty) {
        if (empty) return new PlayingCard[0];
        List<PlayingCard> deck = new ArrayList<>();
        
        for (CardSuit suit : CardSuit.values()) {
            for (String value : getCardValues()) {
                PlayingCard card = new PlayingCard(suit, value);
                deck.add(card);
            }
        }

        return deck.toArray(new PlayingCard[0]);
    }

    private static String[] getCardValues() {
        return new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    }
}
