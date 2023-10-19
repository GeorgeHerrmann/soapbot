package com.georgster.wizard;

import com.georgster.collectable.Collectable;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.wizard.input.InputListenerFactory;

public final class CollectableWizard extends InputWizard {
    private UserProfile ownerProfile;
    private final UserProfileManager userManager;
    private final CollectableManager manager;

    public CollectableWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Collectable Wizard").builder().requireMatch(false, false).disableAutoFormatting().build());
        this.manager = event.getCollectableManager();
        this.userManager = event.getUserProfileManager();
        this.ownerProfile = userManager.get(event.getDiscordEvent().getAuthorAsMember().getId().asString());
    }

    public void begin() {
        nextWindow("createCollectable");
        end();
    }

    protected void viewAllCollectables() {
        String prompt = "Which card would you like to view?";
        String[] cards = new String[manager.getCount() + 1];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = manager.getAll().get(i).getName();
        }
        cards[manager.getCount()] = "";

        withResponse(response -> {

        }, true, null, cards);
    }

    protected void createCollectable() {
        String prompt = "What is the name of the card? Please note that this cannot be changed upon creation.";
        withResponse(response -> {
            Collectable c = Collectable.initialize(response);
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
            prompt = "What is the image url of the card? Please note that this cannot be changed upon creation.";
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
        String prompt = "What is the cost of the card? Please note that this cannot be changed by you after creation.";
        withResponse(response -> {
            try {
                long cost = Long.parseLong(response);
                if (ownerProfile.getBank().hasBalance(cost)) {
                    current.setInitialCost(cost);
                    current.purchaseCollected(ownerProfile);
                    manager.add(current);
                    sendMessage(current.getName() + " created successfully.", "Card created");
                } else {
                    sendMessage("You do not have enough money to create this card.", "Insufficient funds");
                }
            } catch (Exception e) {
                sendMessage("Invalid cost. Please try again.", "Invalid cost");
            }
        }, true, prompt);
    }

}
