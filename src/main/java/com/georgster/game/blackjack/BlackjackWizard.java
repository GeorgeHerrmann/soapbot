package com.georgster.game.blackjack;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.card.PlayingCard;
import com.georgster.util.commands.wizard.InputWizard;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.Member;

public class BlackjackWizard extends InputWizard {

    private BlackJackGame game;
    private Member member;
    
    public BlackjackWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Blackjack"));
        this.member = event.getDiscordEvent().getAuthorAsMember();
        this.game = new BlackJackGame(event);
    }

    public BlackjackWizard(CommandExecutionEvent event, BlackJackGame game) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Blackjack"));
        this.member = event.getDiscordEvent().getAuthorAsMember();
        this.game = game;
    }

    public void begin() {
        game.getDealerDeck().getCard(0).show();
        game.getPlayerDeck(member.getId().asString()).getCardList().forEach(PlayingCard::show);
    }

    protected void playerTurn() {
        
    }
}
