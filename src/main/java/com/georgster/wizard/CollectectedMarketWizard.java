package com.georgster.wizard;

import java.util.List;

import com.georgster.collectable.Collectable;
import com.georgster.collectable.Collected;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.spec.EmbedCreateSpec;

/**
 * A {@link InputWizard} that handles the marketplace for {@link Collected Collecteds}.
 */
public final class CollectectedMarketWizard extends InputWizard {
    private final CollectableManager manager;
    private final UserProfileManager userManager;
    private final UserProfile profile;

    /**
     * Constructs a {@link CollectectedMarketWizard} with the given parameters.
     * 
     * @param event the event to construct for
     */
    public CollectectedMarketWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Card Market"));
        this.manager = event.getCollectableManager();
        this.userManager = event.getUserProfileManager();
        this.profile = userManager.get(user.getId().asString());
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("selectOption");
        end();
    }

    /**
     * The window that handles the selection of an option.
     */
    protected void selectOption() {
        final String prompt = "Welcome to the Card Marketplace! What would you like to do?";
        withResponse(response -> {
            if (response.equals("view market")) {
                nextWindow("viewAllCollecteds", 0);
            } else if (response.equals("create listing")) {
                nextWindow("selectCollectedCreate");
            } else if (response.equals("remove listing")) {
                nextWindow("selectCollectedRemove");
            }
        }, false, prompt, "View Market", "Create Listing", "Remove Listing");
    }

    /**
     * The window that handles the selection of a {@link Collected} to place on the market.
     */
    protected void selectCollectedCreate() {
        InputListener newListener = InputListenerFactory.createMenuMessageListener(event, "Card Market");
        
        List<Collected> collecteds = profile.getCollecteds().stream().filter(c -> !c.isOnMarket()).toList();
        if (collecteds.isEmpty()) {
            sendMessage("You do not have any cards to sell.", "No cards to sell");
            goBack();
        } else {
            String[] options = new String[collecteds.size()];
            for (int i = 0; i < options.length; i++) {
                options[i] = collecteds.get(i).getName() + " - ID: " + collecteds.get(i).getId();
            }
            withResponse(response -> {
                String id = response.substring(response.indexOf("id: ") + 4);
                Collected collected = collecteds.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
                nextWindow("confirmCollectedSell", collected);
            }, true, newListener, "Which card would you like to place for sale?", options);
        }
    }

    /**
     * The window that handles the selection of a {@link Collected} to remove from the market.
     */
    protected void selectCollectedRemove() {
        InputListener newListener = InputListenerFactory.createMenuMessageListener(event, "Card Market");

        List<Collected> collecteds = profile.getCollecteds().stream().filter(Collected::isOnMarket).toList();
        if (collecteds.isEmpty()) {
            sendMessage("You do not have any cards on the market.", "No cards on market");
            goBack();
        } else {
            String[] options = new String[collecteds.size()];
            for (int i = 0; i < options.length; i++) {
                options[i] = collecteds.get(i).getName() + " - ID: " + collecteds.get(i).getId();
            }
            withResponse(response -> {
                String id = response.substring(response.indexOf("id: ") + 4);
                Collected collected = collecteds.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
                collected.setOnMarket(false);
                collected.setCurrentMarketPrice(collected.getCollectable().getCost());
                profile.updateCollected(collected);
                manager.get(collected.getName()).updateCollected(collected);
                manager.update(manager.get(collected.getName()));
                userManager.update(profile);
                sendMessage("You have removed your card with ID " + collected.getId() + " from the market.", "Card Removed from Market");
                nextWindow("selectOption");
            }, true, newListener, "Which card would you like to remove from the market?", options);
        }
    }

    /**
     * The window that handles the confirmation of a {@link Collected} to place on the market.
     * 
     * @param collected the {@code Collected} to place on the market
     */
    protected void confirmCollectedSell(Collected collected) {
        final String prompt = "How much should this card be sold for?";
        Collectable collectable = manager.get(collected.getName());

        withResponse(response -> {
            try {
                long price = Long.parseLong(response);

                if (price < 0) {
                    sendMessage("Invalid price. Please try again.", "Invalid Price");
                } else {
                    collected.setOnMarket(true);
                    collected.setCurrentMarketPrice(price);
                    profile.updateCollected(collected);
                    collectable.updateCollected(collected);
                    manager.update(collectable);
                    userManager.update(profile);
                    sendMessage("You have placed your card with ID " + collected.getId() + " on the market for " + price + " coins.", "Card Placed on Market");
                    nextWindow("selectOption");
                }
            } catch (NumberFormatException e) {
                sendMessage("Invalid price. Please try again.", "Invalid Price");
            }
        }, true, prompt);
    }

    /**
     * The window that handles the selection of a {@link Collected} to view.
     */
    protected void viewAllCollecteds(Integer index) {
        List<Collected> collecteds = manager.getAllCollecteds().stream().filter(Collected::isOnMarket).toList();

        if (collecteds.isEmpty()) {
            sendMessage("There are no cards on the market.", "No cards on market");
            goBack();
            return;
        }

        Collected collected = collecteds.get(index);
        String[] options = new String[]{"Buy", "View"};

        EmbedCreateSpec spec = collected.getDetailedEmbed(userManager, manager);
        
        boolean hasPrevious = index != 0;
        boolean hasNext = index != collecteds.size() - 1;

        if (hasPrevious) {
            if (hasNext) {
                options = new String[]{"Buy", "View", "back", "next"};
            } else {
                options = new String[]{"Buy", "View", "back"};
            }
        } else if (hasNext) {
            options = new String[]{"Buy", "View", "next"};
        }

        withResponse(response -> {
            if (response.equals("back")) {
                nextWindow("viewAllCollecteds", index - 1);
            } else if (response.equals("next")) {
                nextWindow("viewAllCollecteds", index + 1);
            } else if (response.equals("buy")) {
                nextWindow("confirmCollectedBuy", collected);
            } else if (response.equals("view")) {
                nextWindow("viewCollected", collected);
            }
        }, false, spec, options);
    }

    /**
     * The window that handles the viewing of a {@link Collected}.
     * 
     * @param collected the {@code Collected} to view
     */
    protected void viewCollected(Collected collected) {
        EmbedCreateSpec spec = collected.getDetailedEmbed(userManager, manager);

        withResponse(response -> {
            if (response.equals("buy")) {
                nextWindow("confirmCollectedBuy", collected);
            }
        }, true, spec, "Buy");
    }

    /**
     * The window that handles the confirmation of a {@link Collected} to purchase.
     * 
     * @param collected the {@code Collected} to purchase
     */
    protected void confirmCollectedBuy(Collected collected) {
        final Collectable collectable = manager.get(collected.getName());

        String prompt = "Are you sure you want to purchase this card for " + collected.getCurrentMarketPrice() + " coins?";
        withResponse(response -> {
            if (response.equals("confirm")) {
                if (collected.getMemberId().equals(user.getId().asString())) {
                    sendMessage("You cannot purchase your own card.", "Invalid Purchase");
                    goBack();
                } else {
                    if (profile.getBank().hasBalance(collected.getCurrentMarketPrice())) {
                        UserProfile profile2 = userManager.get(collected.getMemberId());
                        collected.setRecentPurchasePrice(collected.getCurrentMarketPrice());
                        collected.trade(profile2, profile);
                        collected.setOnMarket(false);
                        profile2.getBank().deposit(collected.getCurrentMarketPrice());
                        profile.getBank().withdrawl(collected.getCurrentMarketPrice());
                        collectable.updateCollected(collected);
                        manager.update(collectable);
                        userManager.update(profile);
                        userManager.update(profile2);
                        sendMessage("You have purchased card with ID " + collected.getId() + " for " + collected.getCurrentMarketPrice() + " coins. This card has been taken off the market", "Card Purchased Successfully");
                        goBack();
                    } else {
                        sendMessage("Sorry, you need " + collected.getCurrentMarketPrice() + " coins to purchase this card", "Insufficient funds");
                        goBack();
                    }
                }
            }
        }, true, prompt, "Confirm");
    }
}
