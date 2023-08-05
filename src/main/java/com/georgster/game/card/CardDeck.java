package com.georgster.game.card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public abstract class CardDeck {
    private Random random;
    private Deque<PlayingCard> cards;

    protected CardDeck() {
        this.random = new Random();
        this.cards = new ArrayDeque<>();
    }

    protected CardDeck(PlayingCard... cards) {
        this.cards = new ArrayDeque<>(List.of(cards));
        this.random = new Random();
    }

    public boolean hasCards() {
        return !cards.isEmpty();
    }

    public PlayingCard takeTopCard() throws NoSuchElementException {
        return cards.pop();
    }

    public PlayingCard peekTopCard() {
        return cards.peek();
    }

    public void placeOnTop(PlayingCard card) {
        cards.push(card);
    }

    public boolean containsValue(String value) {
        return getCardList().stream().anyMatch(card -> card.getValue().equalsIgnoreCase(value));
    }

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

    public int size() {
        return cards.size();
    }

    public PlayingCard getCard(int index) throws NoSuchElementException {
        return getCardList().get(index);
    }

    public Deque<PlayingCard> getCardStack() {
        return cards;
    }

    public List<PlayingCard> getCardList() {
        return new ArrayList<>(cards);
    }

    public void transferTopCardFrom(CardDeck otherDeck) throws NoSuchElementException {
        this.placeOnTop(otherDeck.takeTopCard());
    }

    public void transferTopCardTo(CardDeck otherDeck) throws NoSuchElementException {
        otherDeck.placeOnTop(this.takeTopCard());
    }

    public void transferAllFrom(CardDeck otherDeck) {
        for (int i = 0; i < otherDeck.size(); i++) {
            this.transferTopCardFrom(otherDeck);
        }
    }

    public void transferAllTo(CardDeck otherDeck) {
        for (int i = 0; i < this.size(); i++) {
            this.transferTopCardTo(otherDeck);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        cards.forEach(card -> sb.append(card.toString()+ " | "));

        return sb.toString();
    }
}
