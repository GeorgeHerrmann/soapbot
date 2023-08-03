package com.georgster.game.card;

import discord4j.core.object.entity.Member;

public class PlayerCardDeck extends CardDeck {
    private String playerId;

    public PlayerCardDeck(int startingCardAmount, GlobalCardDeck globalDeck, Member member) {
        for (int i = 0; i < startingCardAmount; i++) {
            transferTopCardFrom(globalDeck);
        }

        this.playerId = member.getId().asString();
    }

    public PlayerCardDeck(int startingCardAmount, GlobalCardDeck globalDeck, String descriptor) {
        for (int i = 0; i < startingCardAmount; i++) {
            transferTopCardFrom(globalDeck);
        }

        this.playerId = descriptor;
    }

    public String getPlayerId() {
        return playerId;
    }
    
}
