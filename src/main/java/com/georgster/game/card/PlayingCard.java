package com.georgster.game.card;

public class PlayingCard {
    private CardSuit suit;
    private String value;
    private boolean faceDown;

    public PlayingCard(CardSuit suit, String value) {
        this.suit = suit;
        this.value = value;
        this.faceDown = true;
    }

    public CardSuit getSuit() {
        return suit;
    }

    public String getValue() {
        return value;
    }

    public void flip() {
        faceDown = !faceDown;
    }

    public void facedown() {
        faceDown = true;
    }

    public void show() {
        faceDown = true;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public boolean isFaceUp() {
        return !faceDown;
    }

    @Override
    public String toString() {
        return value + " of " + suit.toString();
    }
}
