package com.georgster.coinfarm.wizard;

import com.georgster.coinfarm.model.CoinFactory;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.input.InputListenerFactory;

public final class CoinFactoryWizard extends InputWizard {

    private final CoinFactory factory;
    private final UserProfileManager manager;
    
    public CoinFactoryWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Coin Factory"));
        this.manager = event.getUserProfileManager();
        this.factory = manager.get(event.getDiscordEvent().getAuthorAsMember().getId().asString()).getFactory();
    }

    public void begin() {
        nextWindow("factoryHome");
    }

    protected void factoryHome() {
        String prompt = "Welcome to your Coin Factory! You have " + factory.getCurrentProductionValue() + " coins in this factory.\n\n"
                + "What would you like to do?";

        withResponse(response -> {

        }, false, prompt, "Upgrade Factory");
    }
}
