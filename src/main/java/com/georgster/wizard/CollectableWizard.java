package com.georgster.wizard;

import com.georgster.collectable.Collectable;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.input.InputListenerFactory;

public final class CollectableWizard extends InputWizard {
    private UserProfileManager userManager;
    private CollectableManager manager;

    public CollectableWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Collectable Wizard").builder().requireMatch(false, false).disableAutoFormatting().build());
        this.manager = event.getCollectableManager();
    }

    public void begin() {
        nextWindow("createCollectable");
        end();
    }

    protected void viewAllCollectables() {
        String[] cards = new String[manager.getCount()];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = manager.getAll().get(i).getName();
        }

        withResponse(response -> {

        }, isActive(), null, cards);
    }

    protected void createCollectable() {
        String prompt = "What is the name of the card?";
        withResponse(response -> {
            Collectable c = Collectable.initialize(prompt);
            nextWindow("collectableDescription", c);
        }, true, prompt);
    }

    protected void collectableDescription(Collectable current) {
        String prompt = "What is the description of the card?";
        withResponse(response -> {
            current.setDescription(response);
            nextWindow("collectableImageUrl", current, false);
        }, true, prompt);
    }

    protected void collectableImageUrl(Collectable current, Boolean retry) {
        String prompt = "";
        if (Boolean.TRUE.equals(retry)) {
            prompt = "Invalid url. Please try again.";
        } else {
            prompt = "What is the image url of the card?";
        }

        withResponse(response -> {
            try {
                sendMessage("I found the following image", "Image found from URL", response);
                current.setImageUrl(response);
                nextWindow("collectableCost", current);
            } catch (Exception e) {
                nextWindow("collectableImageUrl", current, true);
            }
        }, true, prompt);
    }

    protected void collectableCost(Collectable current) {
        String prompt = "What is the cost of the card?";
        withResponse(response -> {
            try {
                long cost = Long.parseLong(response);
                current.setInitialCost(cost);
                handler.sendMessage(current.getDescription(), current.getName(), current.getImageUrl());
            } catch (Exception e) {
                sendMessage("Invalid cost. Please try again.", "Invalid cost");
            }
        }, true, prompt);
    }
}
