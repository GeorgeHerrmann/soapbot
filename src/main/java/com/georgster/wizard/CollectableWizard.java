package com.georgster.wizard;

import com.georgster.collectable.Collectable;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.util.thread.ThreadPoolFactory;
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

    protected void createCollectable() {
        String prompt = "What is the name of the card? Please note that this cannot be changed upon creation.";
        withResponse(response -> {
            Collectable c = Collectable.initialize(response, user.getId().asString());
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

                if (cost < 5) {
                    sendMessage("Sorry, a card must have an initial cost of at least **5 coins**", "Invalid card cost");
                } else {
                    if (ownerProfile.getBank().hasBalance(cost)) {
                        current.setInitialCost(cost);
                        current.purchaseCollected(ownerProfile);
                        manager.add(current);
                        sendMessage(current.getName() + " created successfully.", "Card created");
                        shutdown();
                        ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new CollectableViewWizard(event, false).begin("viewCollectable", current));
                    } else {
                        sendMessage("You do not have enough money to create this card.", "Insufficient funds");
                    }
                }
            } catch (Exception e) {
                sendMessage("Invalid cost. Please try again.", "Invalid cost");
            }
        }, true, prompt);
    }

}
