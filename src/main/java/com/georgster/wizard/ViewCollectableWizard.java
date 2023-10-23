package com.georgster.wizard;

import com.georgster.collectable.Collectable;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.spec.EmbedCreateSpec;

public class ViewCollectableWizard extends InputWizard {
    
    private final CollectableManager manager;
    private Collectable collectable;

    public ViewCollectableWizard(CommandExecutionEvent event, Collectable collectable) {
        super(event, InputListenerFactory.createButtonMessageListener(event, collectable.getName()));
        this.collectable = collectable;
        this.manager = event.getCollectableManager();
    }

    public void begin() {
        nextWindow("viewCollectable");
        end();
    }

    protected void viewCollectable() {
        EmbedCreateSpec spec = EmbedCreateSpec.builder()
            .title(collectable.getName())
            .description(collectable.getDescription())
            .image(collectable.getImageUrl())
            .build();
    }

}
