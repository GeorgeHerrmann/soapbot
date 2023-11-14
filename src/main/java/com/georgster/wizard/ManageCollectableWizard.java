package com.georgster.wizard;

import java.util.List;

import com.georgster.collectable.Collectable;
import com.georgster.collectable.Collected;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.economy.exception.InsufficientCoinsException;
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
        super(event, InputListenerFactory.createButtonMessageListener(event, collectable.getName()).builder().requireMatch(true, false).build());
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
            } else if (response.equals("lock")) {
                try {
                    collectable.lock(true);
                    manager.update(collectable);
                    sendMessage("This card has been locked. No one may purchase copies of nor inflate this card. *Note: this card will be automatically unlocked if no copies remain*", "Card Locked");
                } catch (Exception e) {
                    sendMessage(e.getMessage(), "An error occured");
                }
            } else if (response.equals("unlock")) {
                collectable.lock(false);
                manager.update(collectable);
                sendMessage("This card has been unlocked. Anyone may now purchase copies of and inflate this card.", "Card Unlocked");
            } else if (response.equals("edit")) {
                nextWindow("editImageUrl");
            } else if (response.equals("delete")) {
                if (collectable.numCards() == 0) {
                    nextWindow("confirmDelete");
                } else {
                    sendMessage("You cannot delete a card that has copies. Please sell all copies of this card before deleting it.", "Cannot Delete Card");
                }
            }
        }, false, spec, getOptions());
    }

    /**
     * The window that confirms the deletion of the {@link Collectable}.
     */
    protected void confirmDelete() {
        final String prompt = "Are you sure you want to delete the " + collectable.getName() + " card? This action cannot be undone.";

        withResponse(response -> {
            if (response.equals("confirm")) {
                manager.remove(collectable); // Only need to update collectableManager, since no copies should exist, none should be in the UserProfiles
                sendMessage("The " + collectable.getName() + " card has been deleted.", "Card Deleted");
                end();
            }
        }, true, prompt, "Confirm");
    }

    /**
     * The window to inflate the cost of a {@link Collectable}.
     */
    protected void inflateCollectable() {
        if (collectable.isLocked()) {
            sendMessage("Sorry, this card is locked and cannot be inflated", "Card Locked");
            goBack();
            return;
        }

        final String prompt = "How much would you like to inflate the cost of this card by? Please note that the actual inflation amount will be divived by " + collectable.numCards() * 2 + ", as the cost is shared between all cards.";

        withResponse(response -> {
            try {
                long value = Long.parseLong(response);
                collectable.inflateCost(profile, value);
                manager.update(collectable);
                userManager.update(profile);
                userManager.updateFromCollectables(manager); // update all profiles with that collectable
                sendMessage("You have used " + response + " coins to inflate the cost of this card by " + ((int) value / (collectable.numCards() * 2)) + " coins. The new cost is " + collectable.getCost(), "Card Inflated");
                goBack();
            } catch (NumberFormatException e) {
                sendMessage("Invalid number. Please try again.", "Invalid Number");
            } catch (InsufficientCoinsException e2) {
                sendMessage("You do not have enough coins to inflate the cost of this card by " + response + " coins.", "Insufficient Coins");
            }
        }, true, prompt);
    }

    /**
     * The window that handles the purchasing of a new {@link Collected} of the {@link Collectable}.
     */
    protected void purchaseCollected() {
        if (collectable.isLocked()) {
            sendMessage("Sorry, this card is locked and cannot be purchased", "Card Locked");
            goBack();
        } else {
            final String prompt = "Are you sure you want to purchase a " + collectable.getName() + " card for " + collectable.getCost() + " coins?";
            withResponse(response -> {
                if (response.equals("confirm")) {
                    try {
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
                    } catch (Exception e) {
                        sendMessage(e.getMessage(), "An error occured");
                    }
                }
            }, true, prompt, "Confirm");
        }
    }

    /**
     * The window that confirms the selling of a {@link Collected} of the {@link Collectable}.
     * 
     * @param index the index of the {@code Collected} to sell
     */
    protected void confirmCollectedSell(Integer index) {
        final List<Collected> collecteds = collectable.getUserCollecteds(profile);

        if (collecteds.isEmpty()) {
            sendMessage("You do not own any copies of this card", "No Copies Owned");
            goBack();
            return;
        }

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
     * The window that handles the editing of the image url of the {@link Collectable}.
     */
    protected void editImageUrl() {
        final String prompt = "What would you like to set the image url to?";
        withResponse(response -> {
            collectable.setImageUrl(response);
            manager.update(collectable);
            userManager.updateFromCollectables(manager);
            sendMessage("The image url has been set to " + response + ". If the image does not show up in the card preview, please try a new link.", "Image Url Set");
            nextWindow("viewCollectable");
        }, true, prompt);
    }

    /**
     * Returns the User's options for the {@link Collectable}.
     * 
     * @return the User's options for the {@code Collectable}.
     */
    private String[] getOptions() {
        if (collectable.getCreatorId().equals(user.getId().asString())) {
            if (collectable.isLocked()) {
                return new String[]{"Sell", "Buy", "View", "Inflate", "Unlock", "Edit", "!Delete"};
            } else {
                return new String[]{"Sell", "Buy", "View", "Inflate", "Lock", "Edit", "!Delete"};
            }
        } else if (collectable.owns(profile)) {
            return new String[]{"Sell", "Buy", "View", "Inflate"};
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
