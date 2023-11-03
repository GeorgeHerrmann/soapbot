package com.georgster.wizard;

import com.georgster.collectable.Collectable;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

/**
 * A {@link InputWizard} that handles the creation of a {@link Collectable}.
 */
public final class CollectableWizard extends InputWizard {
    private UserProfile ownerProfile;
    private final UserProfileManager userManager;
    private final CollectableManager manager;

    /**
     * Constructs a {@link CollectableWizard} with the given parameters.
     * 
     * @param event the event to construct for
     */
    public CollectableWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Collectable Wizard").builder().requireMatch(false, false).disableAutoFormatting().build());
        this.manager = event.getCollectableManager();
        this.userManager = event.getUserProfileManager();
        this.ownerProfile = userManager.get(event.getDiscordEvent().getAuthorAsMember().getId().asString());
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("createCollectable");
        end();
    }

    /**
     * The window that handles the name input of the {@link Collectable}.
     */
    protected void createCollectable() {
        InputListener newListener = InputListenerFactory.createButtonMessageListener(event, "Collectable Wizard").builder().requireMatch(false, false).build();

        String prompt = "What is the name of the card? Please note that this cannot be changed upon creation.";
        withResponse(response -> {
            if (!manager.exists(response)) {
                Collectable c = Collectable.initialize(response, user.getId().asString());
                nextWindow("collectableDescription", c);
            } else {
                sendMessage("A card with that name already exists. Please try again.", "Card already exists");
            }
        }, true, newListener, prompt);
    }

    /**
     * The window that handles the description input of the {@link Collectable}.
     * 
     * @param current the {@code Collectable} being created
     */
    protected void collectableDescription(Collectable current) {
        String prompt = "What is the description of the card?";
        withResponse(response -> {
            current.setDescription(response);
            nextWindow("collectableImageUrl", current, false);
        }, true, prompt);
    }

    /**
     * The window that handles the image url input of the {@link Collectable}.
     * 
     * @param current the {@code Collectable} being created
     * @param retry   whether or not this is a retry
     */
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

    /**
     * The window that handles the cost input of the {@link Collectable}.
     * 
     * @param current the {@code Collectable} being created
     */
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
                        userManager.update(ownerProfile);
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
