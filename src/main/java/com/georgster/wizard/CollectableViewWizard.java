package com.georgster.wizard;

import java.util.List;

import com.georgster.collectable.Collectable;
import com.georgster.collectable.Collected;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;

public class CollectableViewWizard extends InputWizard {
    
    private final GuildInteractionHandler guildHandler; // Specifically needed apart from handler for member utility
    private final CollectableManager manager;
    private final UserProfileManager userManager;
    private boolean startAtSelect; // True if begin() should start at viewAllCollectablesSelect()

    public CollectableViewWizard(CommandExecutionEvent event, boolean startAtSelect) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Collectable Viewer"));
        this.manager = event.getCollectableManager();
        this.userManager = event.getUserProfileManager();
        this.guildHandler = event.getGuildInteractionHandler();
        this.startAtSelect = startAtSelect;
    }

    public void begin() {
        if (startAtSelect) {
            nextWindow("viewAllCollectablesSelect");
        } else {
            nextWindow("viewAllCollectables", 0);
        }
        end();
    }

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

    protected void viewAllCollectables(Integer index) {
        List<Collectable> collectables = manager.getAll();
        Collectable collectable = collectables.get(index);

        String[] options = new String[]{"Card Manager", "View Cards"};
        
        boolean hasPrevious = index != 0;
        boolean hasNext = index != collectables.size() - 1;

        EmbedCreateSpec spec = EmbedCreateSpec.builder()
            .title(collectable.getName())
            .description(collectable.toString() + "\nRarity: ***" + collectable.getRarity(userManager).toString() + "***\nCreated by: " + guildHandler.getMemberById(collectable.getCreatorId()).getMention())
            .image(collectable.getImageUrl())
            .color(Collectable.getRarityColor(collectable.getRarity(userManager)))
            .build();

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

    protected void viewAllCollecteds(Collectable collectable, Integer index) {
        List<Collected> collecteds = collectable.getCollecteds();
        Collected collected = collecteds.get(index);

        String[] options = new String[]{"Card Manager"};
        
        boolean hasPrevious = index != 0;
        boolean hasNext = index != collecteds.size() - 1;

        Member owner = guildHandler.getMemberById(collected.getMemberId());

        EmbedCreateSpec spec = EmbedCreateSpec.builder()
                .title(owner.getDisplayName() + "'s " + collectable.getName())
                .description(collected.toString() + "\nOwned by: " + owner.getMention())
                .image(collectable.getImageUrl())
                .color(Collectable.getRarityColor(collectable.getRarity(userManager)))
                .build();

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

    protected void viewCollectable(Collectable collectable) {
        InputListener listener = InputListenerFactory.createButtonMessageListener(event, collectable.getName()).builder().allowAllResponses(true).requireMatch(true, true).build();

        EmbedCreateSpec spec = EmbedCreateSpec.builder()
            .title(collectable.getName())
            .description(collectable.toString() + "\nRarity: ***" + collectable.getRarity(userManager).toString() + "***\nCreated by: " + guildHandler.getMemberById(collectable.getCreatorId()).getMention())
            .image(collectable.getImageUrl())
            .color(Collectable.getRarityColor(collectable.getRarity(userManager)))
            .build();
        
        withFullResponse(fullResponse -> {
            String response = fullResponse.getResponse();
            if (response.equals("card manager")) {
                sendMessage("A Card Manager was started in your direct message channel", fullResponse.getResponder().getUsername() + "'s Card Manager");
                ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> new ManageCollectableWizard(event, collectable, fullResponse.getResponder()).begin());
            } else if (response.equals("view cards")) {
                nextWindow("viewAllCollecteds", collectable, 0);
            }
        }, false, listener, spec, "Card Manager", "View Cards");
    }

}
