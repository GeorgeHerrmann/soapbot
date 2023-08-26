package com.georgster.game.card;

/**
 * A card containing a Suit and a Value which can be face down or up.
 */
public class PlayingCard {
    private CardSuit suit;
    private String value;
    private boolean faceDown;

    /**
     * Creates a PlayingCard of the provided {@link CardSuit} and Value.
     * 
     * @param suit The {@link CardSuit} for the card.
     * @param value The value for the card.
     */
    public PlayingCard(CardSuit suit, String value) {
        this.suit = suit;
        this.value = value;
        this.faceDown = true;
    }

    /**
     * Returns the {@link CardSuit} of this card.
     * @return The {@link CardSuit} of this card.
     */
    public CardSuit getSuit() {
        return suit;
    }

    /**
     * Returns the {@code Value} of this card.
     * 
     * @return The {@code Value} of this card.
     */
    public String getValue() {
        return value;
    }

    /**
     * Flips the card.
     */
    public void flip() {
        faceDown = !faceDown;
    }

    /**
     * Faces this card down.
     */
    public void facedown() {
        faceDown = true;
    }

    /**
     * Faces this card up.
     */
    public void show() {
        faceDown = false;
    }

    /**
     * Returns true if this card is face down, false otherwise.
     * 
     * @return True if this card is face down, false otherwise.
     */
    public boolean isFaceDown() {
        return faceDown;
    }

    /**
     * Returns true if this card is face up, false otherwise.
     * 
     * @return True if this card is face up, false otherwise.
     */
    public boolean isFaceUp() {
        return !faceDown;
    }

    /**
     * Returns a String representation of this playing card
     * as "VALUE of SUIT".
     * <p>
     * If this card is face down "F" is returned.
     */
    @Override
    public String toString() {
        return faceDown ? "F" : value + " of " + suit.toString();
    }
}
