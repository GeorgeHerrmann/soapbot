package com.georgster.wizard;

import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.User;

public final class TradableWizard extends InputWizard {
    private final UserProfile profile1;
    private final UserProfile profile2;
    private final UserProfileManager manager;

    public class PersonalTradeWizard extends InputWizard {
        protected PersonalTradeWizard(CommandExecutionEvent event, User user, UserProfile offerer) {
            super(event, InputListenerFactory.createButtonMessageListener(event, "Trade Offer From " + offerer.getUsername()).builder().withTimeoutDuration(300000).build());
            swtichToUserWizard(user);
        }
    }

    public TradableWizard(CommandExecutionEvent event, UserProfile profile1, UserProfile profile2) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Trade Wizard").builder().requireMatch(false, false).disableAutoFormatting().build());
        this.profile1 = profile1;
        this.profile2 = profile2;
        this.manager = event.getUserProfileManager();
    }
}
