package com.georgster.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.card.CardDeck;
import com.georgster.game.card.GlobalCardDeck;
import com.georgster.game.card.PlayerCardDeck;
import com.georgster.game.card.PlayingCard;

import discord4j.core.object.entity.Member;

/**
 * A {@link DiscordGame} with a {@link GlobalCardDeck DrawingDeck}, a
 * {@link GlobalCardDeck DiscardDeck} and multiple {@link PlayerCardDeck player decks}.
 */
public abstract class CardGame extends DiscordGame {
    private GlobalCardDeck globalDrawingDeck;
    private GlobalCardDeck globalDiscardDeck;
    private List<PlayerCardDeck> playerDecks;
    
    /**
     * Creates a CardGame with the provided amount of starting cards per player with no entry amount.
     * If {@code shuffleDrawingDeck}, this games Drawing Deck will be shuffled upon creation,
     * otherwise card generation will be the same each game instance.
     * 
     * @param event The event that prompted this game's creation
     * @param startingCardAmount The amount of cards players should start with.
     * @param shuffleDrawingDeck True if the drawing deck should be shuffled immediately, false otherwise.
     */
    protected CardGame(CommandExecutionEvent event, int startingCardAmount, boolean shuffleDrawingDeck) {
        super(event);
        this.globalDrawingDeck = new GlobalCardDeck(false);
        this.globalDiscardDeck = new GlobalCardDeck(true);
        if (shuffleDrawingDeck) globalDrawingDeck.shuffle();
        this.playerDecks = new ArrayList<>();
        this.playerDecks.add(new PlayerCardDeck(startingCardAmount, globalDrawingDeck, event.getDiscordEvent().getAuthorAsMember()));
    }

    /**
     * Creates a CardGame with the provided amount of starting cards per player and the provided coin entry amount.
     * If {@code shuffleDrawingDeck}, this games Drawing Deck will be shuffled upon creation,
     * otherwise card generation will be the same each game instance.
     * 
     * @param event The event that prompted this game's creation
     * @param startingCardAmount The amount of cards players should start with.
     * @param shuffleDrawingDeck True if the drawing deck should be shuffled immediately, false otherwise.
     * @param entryAmount The coin entry amount.
     */
    protected CardGame(CommandExecutionEvent event, int startingCardAmount, boolean shuffleDrawingDeck, long entryAmount) {
        super(event, entryAmount, 0);
        this.globalDrawingDeck = new GlobalCardDeck(false);
        this.globalDiscardDeck = new GlobalCardDeck(true);
        if (shuffleDrawingDeck) globalDrawingDeck.shuffle();
        this.playerDecks = new ArrayList<>();
        this.playerDecks.add(new PlayerCardDeck(startingCardAmount, globalDrawingDeck, event.getDiscordEvent().getAuthorAsMember()));
    }

    /**
     * Executes the given consumer on each {@link PlayingCard} the deck
     * with the provided {@code playerId} contains.
     */
    public void forEachPlayerCard(String playerId, Consumer<PlayingCard> consumer) {
        getPlayerDeck(playerId).getCardStack().forEach(consumer::accept);
    }

    /**
     * Adds a Member to this game with {@code startingCardAmount} amount of cards.
     * 
     * @param startingCardAmount The number of cards to start the player with.
     * @param member The member who is being added.
     */
    public void addPlayer(int startingCardAmount, Member member) {
        this.playerDecks.add(new PlayerCardDeck(startingCardAmount, globalDrawingDeck, member));
    }

    /**
     * Adds an automated player with {@code startingCardAmount} amount of cards.
     * 
     * @param startingCardAmount The amount of cards to start the player with.
     * @param descriptor The descriptor for the automated player.
     */
    public void addAutomatedPlayer(int startingCardAmount, String descriptor) {
        this.playerDecks.add(new PlayerCardDeck(startingCardAmount, globalDrawingDeck, descriptor));
    }

    /**
     * Shuffles the drawing deck for this game.
     */
    public void shuffleDrawingDeck() {
        this.globalDrawingDeck.shuffle();
    }

    /**
     * Shuffles the discard deck for this game.
     */
    public void shuffleDiscardDeck() {
        this.globalDiscardDeck.shuffle();
    }

    /**
     * Mixes the cards from the discard deck into the drawing deck, then shuffles the drawing deck.
     */
    public void mixDiscardsIntoDrawingDeck() {
        globalDrawingDeck.transferAllFrom(globalDiscardDeck);
        globalDrawingDeck.shuffle();
    }

    /**
     * Returns the GlobalDrawingDeck for this game.
     * 
     * @return The GlobalDrawingDeck for this game.
     */
    public CardDeck getGlobalDrawingDeck() {
        return globalDrawingDeck;
    }

    /**
     * Returns the GlobalDiscardDeck for this game.
     * 
     * @return The GlobalDiscardDeck for this game.
     */
    public CardDeck getGlobalDiscardDeck() {
        return globalDiscardDeck;
    }

    /**
     * Returns all Player Decks in this game.
     * 
     * @return All player decks in this game.
     */
    public List<PlayerCardDeck> getPlayerDecks() {
        return playerDecks;
    }

    /**
     * Returns all card decks in this game.
     * 
     * @return All card decks in this game.
     */
    public List<CardDeck> getAllDecks() {
        List<CardDeck> allDecks = new ArrayList<>();

        allDecks.addAll(playerDecks);
        allDecks.add(globalDrawingDeck);
        allDecks.add(globalDiscardDeck);
        
        return allDecks;
    }

    /**
     * Returns the player card deck with the associated id.
     * 
     * @param id The ID of the deck to get.
     * @return The card deck, or null if none exist.
     */
    public CardDeck getPlayerDeck(String id) {
        return playerDecks.stream().filter(card -> card.getPlayerId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }



}
