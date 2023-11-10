package com.georgster.wizard;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import com.georgster.collectable.Collectable;
import com.georgster.collectable.Collected;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.spec.EmbedCreateSpec;

/**
 * A {@link InputWizard} that handles viewing {@link Collectable Collectables} and {@link Collected Collecteds}.
 */
public class CollectableViewWizard extends InputWizard {
    
    private final GuildInteractionHandler guildHandler; // Specifically needed apart from handler for member utility
    private final CollectableManager manager;
    private final UserProfileManager userManager;
    private final String startingWindow;
    private final Object[] startingParams;

    /**
     * Constructs a {@link CollectableViewWizard} for the given {@link CommandExecutionEvent}.
     * 
     * @param event the event to construct for
     */
    public CollectableViewWizard(CommandExecutionEvent event, boolean startAtSelect) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Collectable Viewer"));
        this.manager = event.getCollectableManager();
        this.userManager = event.getUserProfileManager();
        this.guildHandler = event.getGuildInteractionHandler();
        if (startAtSelect) {
            this.startingWindow = "viewAllCollectablesSelect";
            this.startingParams = new Object[0];
        } else {
            this.startingWindow = "viewAllCollectables";
            this.startingParams = new Object[]{0};
        }
    }

    public CollectableViewWizard(CommandExecutionEvent event, String startingWindow, Object... startingParams) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Collectable Viewer"));
        this.manager = event.getCollectableManager();
        this.userManager = event.getUserProfileManager();
        this.guildHandler = event.getGuildInteractionHandler();
        this.startingWindow = startingWindow;
        this.startingParams = startingParams;
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow(startingWindow, startingParams);
        shutdown();
    }

    /**
     * The window to select a {@link Collectable} to view.
     */
    protected void viewAllCollectablesSelect() {
        InputListener listener = InputListenerFactory.createMenuMessageListener(event, "Collectable Viewer");

        String prompt = "Which card would you like to view?";
        String[] cards = new String[manager.getCount()];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = manager.getAll().get(i).getName();
        }

        withResponse(response -> {
            if (manager.exists(response)) {
                nextWindow("viewCollectable", manager.get(response));
            } else {
                sendMessage("A card with that name does not exist inside of " + guildHandler.getGuild().getName(), "Card not found");
            }
        }, false, listener, prompt, cards);
    }

    /**
     * The window to view all {@link Collectable Collectables}.
     * 
     * @param index the index of the {@link Collectable} to view
     */
    protected void viewAllCollectables(Integer index) {
        List<Collectable> collectables = manager.getAll();
        Collectable collectable = collectables.get(index);

        String[] options = new String[]{"Card Manager", "View Cards"};
        
        boolean hasPrevious = index != 0;
        boolean hasNext = index != collectables.size() - 1;

        EmbedCreateSpec spec = collectable.getGeneralEmbed(userManager, manager);

        if (hasPrevious) {
            if (hasNext) {
                options = new String[]{"Card Manager", "View Cards", "back", "next"};
            } else {
                options = new String[]{"Card Manager", "View Cards", "back"};
            }
        } else if (hasNext) {
            options = new String[]{"Card Manager", "View Cards", "next"};
        }

        withFullResponse(fullResponse -> {
            String response = fullResponse.getResponse();
            if (response.equals("card manager")) {
                sendMessage("A Collectable Manager was started in your direct message channel", fullResponse.getResponder().getUsername() + " Card Manager");
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new ManageCollectableWizard(event, collectable, fullResponse.getResponder()).begin());
            } else if (response.equals("view cards")) {
                if (!collectable.getCollecteds().isEmpty()) {
                    nextWindow("viewAllCollecteds", collectable, 0);
                } else {
                    sendMessage("Nobody in " + getGuild().getName() + " has any " + collectable.getName() + " cards currently. You can use the Card Manager to buy one.", "No cards");
                }
            } else if (response.equals("back")) {
                nextWindow("viewAllCollectables", index - 1);
            } else if (response.equals("next")) {
                nextWindow("viewAllCollectables", index + 1);
            }
        }, false, spec, options);
    }

    /**
     * The window to view all {@link Collected Collecteds} of a {@link Collectable}.
     * 
     * @param collectable the {@code Collectable} to view
     * @param index the index of the {@code Collected} to view
     */
    protected void viewAllCollecteds(Collectable collectable, Integer index) {
        List<Collected> collecteds = collectable.getCollecteds();
        Collected collected = collecteds.get(index);

        String[] options = new String[]{"Card Manager"};
        
        boolean hasPrevious = index != 0;
        boolean hasNext = index != collecteds.size() - 1;

        EmbedCreateSpec spec = collected.getGeneralEmbed(userManager, manager);

        if (hasPrevious) {
            if (hasNext) {
                options = new String[]{"Card Manager", "back", "next"};
            } else {
                options = new String[]{"Card Manager", "back"};
            }
        } else if (hasNext) {
            options = new String[]{"Card Manager", "next"};
        }

        withResponse(response -> {
            if (response.equals("back")) {
                nextWindow("viewAllCollecteds", collectable, index - 1);
            } else if (response.equals("next")) {
                nextWindow("viewAllCollecteds", collectable, index + 1);
            } else if (response.equals("card manager")) {
                sendMessage("A Collectable Manager was started in your direct message channel", "Card Manager");
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new ManageCollectableWizard(event, collectable, user).begin());
            }
        }, false, spec, options);
    }

    /**
     * The window to view all {@link Collected Collecteds} of a {@link Collectable} in order of value.
     * 
     * @param index the index of the {@code Collected} to view
     */
    protected void viewAllRankedCollecteds(Integer index) {
        List<Collectable> collectables = manager.getAll();
        Collections.sort(collectables, Comparator.comparingLong(c -> -c.getCost()));
        Collectable collectable = collectables.get(index);

        String[] options = new String[]{"Card Manager"};
        
        boolean hasPrevious = index != 0;
        boolean hasNext = index != collectables.size() - 1;

        EmbedCreateSpec spec = collectable.getGeneralEmbed(userManager, manager);

        if (hasPrevious) {
            if (hasNext) {
                options = new String[]{"back", "next", "Card Manager", "View Cards"};
            } else {
                options = new String[]{"back", "Card Manager", "View Cards"};
            }
        } else if (hasNext) {
            options = new String[]{"next", "Card Manager", "View Cards"};
        }

        withResponse(response -> {
            if (response.equals("back")) {
                nextWindow("viewAllRankedCollecteds", index - 1);
            } else if (response.equals("next")) {
                nextWindow("viewAllRankedCollecteds", index + 1);
            } else if (response.equals("card manager")) {
                sendMessage("A Collectable Manager was started in your direct message channel", "Card Manager");
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new ManageCollectableWizard(event, collectable, user).begin());
            } else if (response.equals("view cards")) {
                if (!collectable.getCollecteds().isEmpty()) {
                    nextWindow("viewAllCollecteds", collectable, 0);
                } else {
                    sendMessage("Nobody in " + getGuild().getName() + " has any " + collectable.getName() + " cards currently. You can use the Card Manager to buy one.", "No cards");
                }
            }
        }, false, spec, options);
    }

    /**
     * The window to view a {@link Collected}.
     * 
     * @param collected the {@code Collected} to view
     */
    protected void viewCollectable(Collectable collectable) {
        InputListener listener = InputListenerFactory.createButtonMessageListener(event, collectable.getName()).builder().allowAllResponses(true).requireMatch(true, true).build();

        EmbedCreateSpec spec = EmbedCreateSpec.builder()
            .title(collectable.getName())
            .description(collectable.toString() + "\nRarity: ***" + collectable.getRarity(userManager, manager).toString() + "***\nCreated by: " + guildHandler.getMemberById(collectable.getCreatorId()).getMention())
            .image(collectable.getImageUrl())
            .color(Collectable.getRarityColor(collectable.getRarity(userManager, manager)))
            .build();
        
        withFullResponse(fullResponse -> {
            String response = fullResponse.getResponse();
            if (response.equals("card manager")) {
                sendMessage("A Card Manager was started in your direct message channel", fullResponse.getResponder().getUsername() + "'s Card Manager");
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new ManageCollectableWizard(event, collectable, fullResponse.getResponder()).begin());
            } else if (response.equals("view cards")) {
                if (!collectable.getCollecteds().isEmpty()) {
                    nextWindow("viewAllCollecteds", collectable, 0);
                } else {
                    sendMessage("Nobody in " + getGuild().getName() + " has any " + collectable.getName() + " cards currently. You can use the Card Manager to buy one.", "No cards");
                }
            }
        }, false, listener, spec, "Card Manager", "View Cards");
    }

    /**
     * The window to view a {@link Collected}.
     * 
     * @param collected the {@code Collected} to view
     */
    protected void viewCollected(Collected collected) {
        Collectable collectable = manager.get(collected.getName());
        
        EmbedCreateSpec spec = collected.getDetailedEmbed(userManager, manager);
        
        String[] options;

        if (collected.isOnMarket()) {
            options = new String[]{"Card Manager", "View on Market"};
        } else {
            options = new String[]{"Card Manager", "Place on Market"};
        }

        withResponse(response -> {
            if (response.equals("card manager")) {
                sendMessage("A Collectable Manager was started in your direct message channel", "Card Manager");
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new ManageCollectableWizard(event, collectable, user).begin());
            } else if (response.equals("view on market")) {
                end();
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new CollectectedMarketWizard(event).begin("viewCollected", collected));
            } else if (response.equals("place on market")) {
                end();
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new CollectectedMarketWizard(event).begin("confirmCollectedSell", collected));
            }
        }, true, spec, options);
    }

    /**
     * The window to view all {@link Collected Collecteds} in a {@link UserProfile}.
     * 
     * @param profile the {@code UserProfile} to view
     * @param index the index of the {@code Collected} to view
     */
    protected void viewMemberCards(UserProfile profile, Integer index) {
        List<Collected> collecteds = profile.getCollecteds();

        if (collecteds.isEmpty()) {
            sendMessage("This user does not have any cards in " + getGuild().getName() + " currently.", "No cards");
            end();
        } else {
            Collected collected = collecteds.get(index);

            String[] options = new String[]{"Card Manager"};
            
            boolean hasPrevious = index != 0;
            boolean hasNext = index != collecteds.size() - 1;

            EmbedCreateSpec spec = collected.getDetailedEmbed(userManager, manager);

            if (hasPrevious) {
                if (hasNext) {
                    options = new String[]{"Card Manager", "back", "next"};
                } else {
                    options = new String[]{"Card Manager", "back"};
                }
            } else if (hasNext) {
                options = new String[]{"Card Manager", "next"};
            }

            withResponse(response -> {
                if (response.equals("back")) {
                    nextWindow("viewMemberCards", profile, index - 1);
                } else if (response.equals("next")) {
                    nextWindow("viewMemberCards", profile, index + 1);
                } else if (response.equals("card manager")) {
                    sendMessage("A Collectable Manager was started in your direct message channel", "Card Manager");
                    ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new ManageCollectableWizard(event, manager.get(collected.getName()), user).begin());
                }
            }, false, spec, options);
        }
    }

}
