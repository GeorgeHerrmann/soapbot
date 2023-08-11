package com.georgster.game.card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * A deck of {@link PlayingCard PlayingCards}.
 * <p>
 * The cards are held by a stack, therefore {@link #placeOnTop(PlayingCard)} and {@link #takeTopCard()}
 * are the primary ways to add and take from the deck, unless {@link #getCardList()} is used.
 */
public abstract class CardDeck {
    private Random random;
    private Deque<PlayingCard> cards;

    /**
     * Creates a new CardDeck with no cards.
     */
    protected CardDeck() {
        this.random = new Random();
        this.cards = new ArrayDeque<>();
    }

    /**
     * Creates a new card deck containing the provided cards.
     * 
     * @param cards The {@link PlayingCard PlayingCards} to be in the card deck.
     */
    protected CardDeck(PlayingCard... cards) {
        this.cards = new ArrayDeque<>(List.of(cards));
        this.random = new Random();
    }

    /**
     * Returns is this deck has any cards.
     * 
     * @return True if the deck has cards, false otherwise.
     */
    public boolean hasCards() {
        return !cards.isEmpty();
    }

    /**
     * Removes and returns the card on top of this deck.
     * 
     * @return The removed card.
     * @throws NoSuchElementException If this deck has no cards.
     */
    public PlayingCard takeTopCard() throws NoSuchElementException {
        return cards.pop();
    }

    /**
     * Returns the card on top of this deck without removing it.
     * 
     * @return The card on top of this deck
     */
    public PlayingCard peekTopCard() {
        return cards.peek();
    }

    /**
     * Places the provided card on top of the deck.
     * 
     * @param card The card to place on top.
     */
    public void placeOnTop(PlayingCard card) {
        cards.push(card);
    }

    /**
     * Returns true if this deck has a {@link PlayingCard} with a
     * {@code value} equal to the provided value, ignoring case.
     * 
     * @param value The value to match against this deck's cards.
     * @return True if a card contains the value, false otherwise.
     */
    public boolean containsValue(String value) {
        return getCardList().stream().anyMatch(card -> card.getValue().equalsIgnoreCase(value));
    }

    /**
     * Shuffles the cards of this deck randomly.
     */
    public void shuffle() {
        PlayingCard[] elements = cards.toArray(new PlayingCard[cards.size()]);

        for (int i = elements.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            PlayingCard temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
        }

        cards.clear();
        for (PlayingCard element : elements) {
            cards.push(element);
        }
    }

    /**
     * Returns how many cards are in this deck.
     * 
     * @return The number of cards in this deck.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Returns the card at the provided index in this deck.
     * 
     * @param index The index to get the card at.
     * @return The card at the index.
     * @throws NoSuchElementException If there are no cards at the provided index.
     */
    public PlayingCard getCard(int index) throws NoSuchElementException {
        return getCardList().get(index);
    }

    /**
     * Returns the cards of this deck as a stack.
     * 
     * @return The cards of this deck as a stack.
     */
    public Deque<PlayingCard> getCardStack() {
        return cards;
    }

    /**
     * Returns the cards of this deck as a list.
     * 
     * @return The cards of this deck as a list.
     */
    public List<PlayingCard> getCardList() {
        return new ArrayList<>(cards);
    }

    /**
     * Transfers the top card from the provided card deck to the top of this deck.
     * 
     * @param otherDeck The deck to take the top card from.
     * @throws NoSuchElementException If the provided deck has no cards.
     */
    public void transferTopCardFrom(CardDeck otherDeck) throws NoSuchElementException {
        this.placeOnTop(otherDeck.takeTopCard());
    }

    /**
     * Transfers the top card of this deck to the top of the provided deck.
     * 
     * @param otherDeck The deck to give the card to.
     * @throws NoSuchElementException If this deck has no cards.
     */
    public void transferTopCardTo(CardDeck otherDeck) throws NoSuchElementException {
        otherDeck.placeOnTop(this.takeTopCard());
    }

    /**
     * Transfers all cards from the provided deck to the top of this deck.
     * <p>
     * This results in the provided deck's cards being placed on the top of this
     * deck's cards from the bottom up.
     * 
     * @param otherDeck The deck to transfer cards from.
     */
    public void transferAllFrom(CardDeck otherDeck) {
        for (int i = 0; i < otherDeck.size(); i++) {
            this.transferTopCardFrom(otherDeck);
        }
    }

    /**
     * Transfers all cards from the this deck to the top of the provided deck.
     * <p>
     * This results in the this deck's cards being placed on the top of the
     * provided deck's cards from the bottom up.
     * 
     * @param otherDeck The deck to transfer cards to.
     */
    public void transferAllTo(CardDeck otherDeck) {
        for (int i = 0; i < this.size(); i++) {
            this.transferTopCardTo(otherDeck);
        }
    }

    /**
     * Returns a deck of cards containing this deck's cards from the provided
     * beginning range to the provided end range.
     * 
     * @param beginRange The beginning range of the deck (inclusive).
     * @param endRange The ending range of the deck (exclusive).
     * @return A CardDeck with the cards of this deck within the provided bounds.
     * @throws NoSuchElementException The begin or end range is out of bounds for this deck.
     */
    public CardDeck getSubDeck(int beginRange, int endRange) throws NoSuchElementException {
        PlayingCard[] subdeck = new PlayingCard[cards.size() - beginRange - (cards.size() - endRange)];
        List<PlayingCard> cardList = getCardList();

        int cardIndex = 0;
        for (int i = 0; i < cards.size(); i++) {
            if (i >= beginRange && i < endRange) {
                subdeck[cardIndex] = cardList.get(i);
                cardIndex++;
            }
        }

        return new CardDeck(subdeck) {};
    }

    /**
     * Returns a String representation of this deck's cards.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        cards.forEach(card -> sb.append(card.toString() + " | "));

        return sb.toString();
    }
}
