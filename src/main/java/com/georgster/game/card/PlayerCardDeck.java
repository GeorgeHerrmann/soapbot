package com.georgster.game.card;

import discord4j.core.object.entity.Member;

/**
 * A {@link CardDeck} for an individual player or deck holder.
 */
public class PlayerCardDeck extends CardDeck {
    private String playerId;

    /**
     * Creates a new PlayerCardDeck for the provided member,
     * filling the {@code startingCardAmount} amount of cards from the GlobalCardDeck
     * 
     * @param startingCardAmount The amount of cards to give to the player.
     * @param globalDeck The deck to draw the cards from.
     * @param member The member the deck is for.
     */
    public PlayerCardDeck(int startingCardAmount, GlobalCardDeck globalDeck, Member member) {
        for (int i = 0; i < startingCardAmount; i++) {
            transferTopCardFrom(globalDeck);
        }

        this.playerId = member.getId().asString();
    }

    /**
     * Creates a new PlayerCardDeck for an automated member identified by the {@code descriptor},
     * filling the {@code startingCardAmount} amount of cards from the GlobalCardDeck
     * 
     * @param startingCardAmount The amount of cards to give to the player.
     * @param globalDeck The deck to draw the cards from.
     * @param descriptor An identifying String for the deck.
     */
    public PlayerCardDeck(int startingCardAmount, GlobalCardDeck globalDeck, String descriptor) {
        for (int i = 0; i < startingCardAmount; i++) {
            transferTopCardFrom(globalDeck);
        }

        this.playerId = descriptor;
    }

    /**
     * Returns the id for the player of this deck.
     * 
     * @return The id for the player of this deck.
     */
    public String getPlayerId() {
        return playerId;
    }
    
}
