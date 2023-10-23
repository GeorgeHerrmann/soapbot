package com.georgster.wizard;

import com.georgster.control.manager.CollectableManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

public class CollectableMarketWizard extends InputWizard {
    
    private final CollectableManager manager;
    private boolean startAtSelect; // True if begin() should start at viewAllCollectablesSelect()

    public CollectableMarketWizard(CommandExecutionEvent event, boolean startAtSelect) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Collectable Marketplace"));
        this.manager = event.getCollectableManager();
        this.startAtSelect = startAtSelect;
    }

    public void begin() {
        if (startAtSelect) {
            nextWindow("viewAllCollectablesSelect");
        } else {
            nextWindow("viewAllCollectables");
        }
        end();
    }

    protected void viewAllCollectablesSelect() {
        InputListener listener = InputListenerFactory.createMenuMessageListener(event, "Collectable Marketplace");

        String prompt = "Which card would you like to view?";
        String[] cards = new String[manager.getCount()];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = manager.getAll().get(i).getName();
        }

        withResponse(response -> {

        }, false, listener, prompt, cards);
    }

    
}
