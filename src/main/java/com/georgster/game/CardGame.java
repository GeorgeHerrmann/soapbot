package com.georgster.game;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.card.CardDeck;
import com.georgster.game.card.GlobalCardDeck;
import com.georgster.game.card.PlayerCardDeck;

import discord4j.core.object.entity.Member;

public abstract class CardGame extends DiscordGame {
    private GlobalCardDeck globalDrawingDeck;
    private GlobalCardDeck globalDiscardDeck;
    private List<PlayerCardDeck> playerDecks;
    
    protected CardGame(CommandExecutionEvent event, int startingCardAmount) {
        super(event);
        this.globalDrawingDeck = new GlobalCardDeck(false);
        this.globalDiscardDeck = new GlobalCardDeck(true);
        this.playerDecks = new ArrayList<>();
        this.playerDecks.add(new PlayerCardDeck(startingCardAmount, globalDrawingDeck, event.getDiscordEvent().getAuthorAsMember()));
    }

    public void addPlayer(int startingCardAmount, Member member) {
        this.playerDecks.add(new PlayerCardDeck(startingCardAmount, globalDrawingDeck, member));
    }

    public void addAutomatedPlayer(int startingCardAmount, String descriptor) {
        this.playerDecks.add(new PlayerCardDeck(startingCardAmount, globalDrawingDeck, descriptor));
    }

    public void shuffleDrawingDeck() {
        this.globalDrawingDeck.shuffle();
    }

    public void shuffleDiscardDeck() {
        this.globalDiscardDeck.shuffle();
    }

    public void mixDiscardsIntoDrawingDeck() {
        globalDrawingDeck.transferAllFrom(globalDiscardDeck);
        globalDrawingDeck.shuffle();
    }

    public CardDeck getGlobalDrawingDeck() {
        return globalDrawingDeck;
    }

    public CardDeck getGlobalDiscardDeck() {
        return globalDiscardDeck;
    }

    public List<PlayerCardDeck> getPlayerDecks() {
        return playerDecks;
    }

    public List<CardDeck> getAllDecks() {
        List<CardDeck> allDecks = new ArrayList<>();

        allDecks.addAll(playerDecks);
        allDecks.add(globalDrawingDeck);
        allDecks.add(globalDiscardDeck);
        
        return allDecks;
    }

    public CardDeck getPlayerDeck(String id) {
        return playerDecks.stream().filter(card -> card.getPlayerId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }



}
