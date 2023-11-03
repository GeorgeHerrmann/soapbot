package com.georgster.wizard;

import java.util.List;

import com.georgster.collectable.Collectable;
import com.georgster.collectable.Collected;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;

/**
 * A {@link InputWizard} that handles managing a {@link Collectable} in a User's private message channel.
 */
public class ManageCollectableWizard extends InputWizard {
    
    private final CollectableManager manager;
    private final UserProfileManager userManager;
    private Collectable collectable;
    private final UserProfile profile;

    /**
     * Constructs a {@link ManageCollectableWizard} with the given parameters.
     * 
     * @param event the event to construct for
     * @param collectable the {@code Collectable} to manage
     * @param user the {@code User} to manage the {@code Collectable} for
     */
    public ManageCollectableWizard(CommandExecutionEvent event, Collectable collectable, User user) {
        super(event, InputListenerFactory.createButtonMessageListener(event, collectable.getName()).builder().withXReaction(false).requireMatch(false, false).build());
        this.collectable = collectable;
        this.manager = event.getCollectableManager();
        this.userManager = event.getUserProfileManager();
        this.profile = event.getUserProfileManager().get(user.getId().asString());
        this.user = user;
        swtichToUserWizard(user);
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("viewCollectable");
        end();
    }

    /**
     * The window that handles the viewing of the {@link Collectable}.
     */
    protected void viewCollectable() {
        EmbedCreateSpec spec = collectable.getGeneralEmbed(userManager, manager);
            
        
        withResponse(response -> {
            if (response.equals("sell")) {
                nextWindow("confirmCollectedSell", 0);
            } else if (response.equals("buy")) {
                nextWindow("purchaseCollected");
            } else if (response.equals("view")) {
                displayDetailedCollectable();
            } else if (response.equals("inflate")) {
                nextWindow("inflateCollectable");
            }
        }, false, spec, getOptions());
    }

    /**
     * The window that handles the purchasing of a new {@link Collected} of the {@link Collectable}.
     */
    protected void purchaseCollected() {
        final String prompt = "Are you sure you want to purchase a " + collectable.getName() + " card for " + collectable.getCost() + " coins?";
        withResponse(response -> {
            if (response.equals("confirm")) {
                if (profile.getBank().hasBalance(collectable.getCost())) {
                    collectable.purchaseCollected(profile);
                    manager.update(collectable);
                    userManager.update(profile);
                    userManager.updateFromCollectables(manager); // update all profiles with that collectable
                    sendMessage("You have purchased a " + collectable.getName() + " card. You can view it with !cards in " + userManager.getGuild().getName(), "Card Purchased Successfully");
                    nextWindow("viewCollectable");
                } else {
                    sendMessage("Sorry, you need " + collectable.getCost() + " coins to purchase this card", "Insufficient funds");
                    nextWindow("viewCollectable");
                }
            }
        }, true, prompt, "Confirm");
    }

    /**
     * The window that confirms the selling of a {@link Collected} of the {@link Collectable}.
     * 
     * @param index the index of the {@code Collected} to sell
     */
    protected void confirmCollectedSell(Integer index) {
        final List<Collected> collecteds = collectable.getUserCollecteds(profile);
        Collected collected = collecteds.get(index);
        String[] options = new String[]{"Confirm"};
        
        boolean hasPrevious = index != 0;
        boolean hasNext = index != collecteds.size() - 1;

        EmbedCreateSpec spec = collected.getDetailedEmbed(userManager, manager);

        if (hasPrevious) {
            if (hasNext) {
                options = new String[]{"confirm", "back", "next"};
            } else {
                options = new String[]{"confirm", "back"};
            }
        } else if (hasNext) {
            options = new String[]{"confirm", "next"};
        }

        withResponse(response -> {
            if (response.equals("back")) {
                nextWindow("confirmCollectedSell", index - 1);
            } else if (response.equals("next")) {
                nextWindow("confirmCollectedSell", index + 1);
            } else if (response.equals("confirm")) {
                collectable.sellCollected(profile, collected);
                userManager.update(profile);
                manager.update(collectable);
                userManager.updateFromCollectables(manager); // update all profiles with that collectable
                sendMessage("You have sold a card with ID " + collected.getIdentifier() + "\nThe new price is " + collectable.getCost(), "Card Sold");
                nextWindow("viewCollectable");
            }
        }, false, spec, options);
    }

    /**
     * Returns the User's options for the {@link Collectable}.
     * 
     * @return the User's options for the {@code Collectable}.
     */
    private String[] getOptions() {
        if (collectable.owns(profile)) {
            return new String[]{"Sell", "Buy", "View"};
        } else {
            return new String[]{"Buy", "View"};
        }
    }

    /**
     * Displays the detailed information of the {@link Collectable} in a new Message.
     */
    private void displayDetailedCollectable() {
        StringBuilder sb = new StringBuilder();
        sb.append("*" + collectable.getDescription() + "*\n");
        sb.append("Rarity: ***" + collectable.getRarity(userManager, manager).toString() + "***\n");
        sb.append("Total Cards: " + collectable.getCollecteds().size() + "\n");
        sb.append("Current Cost: " + collectable.getCost() + "\n");
        sb.append("Initial Cost: " + collectable.getInitialCost() + "\n");

        EmbedCreateSpec spec = EmbedCreateSpec.builder()
            .title(collectable.getName())
            .description(sb.toString())
            .image(collectable.getImageUrl())
            .color(Collectable.getRarityColor(collectable.getRarity(userManager, manager)))
            .build();

        handler.sendMessage(spec);
    }

}
