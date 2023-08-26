package com.georgster.util.commands.wizard;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.poll.PollEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

/**
 * A wizard designed to handle {@link PollEvent PollEvents} which are {@code QuickPolls}.
 */
public class QuickPollWizard extends InputWizard {
    private PollEvent event;
    private final SoapEventManager eventManager;
    private CommandExecutionEvent executionEvent;
    private static final SoapEventType TYPE = SoapEventType.POLL;

    /**
     * Creates a new QuickPollWizard with a ReactionListener as the way to vote for the poll.
     * 
     * @param executionEvent The event that prompted the wizard's creation.
     * @param event The PollEvent (a quickpoll).
     */
    public QuickPollWizard(CommandExecutionEvent executionEvent, PollEvent event) {
        super(executionEvent, InputListenerFactory.createReactionListener(executionEvent, event.getIdentifier(), false).builder().withXReaction(false).withTimeoutDuration(120000).build());
        this.event = event;
        this.eventManager = executionEvent.getEventManager();
        this.executionEvent = executionEvent;
    }

    /**
     * Creates a new QuickPollWizard with a MenuMessageListener as a way to present all QuickPolls.
     * 
     * @param executionEvent The event that prompted the wizard's creation.
     */
    public QuickPollWizard(CommandExecutionEvent executionEvent) {
        super(executionEvent, InputListenerFactory.createMenuMessageListener(executionEvent, "All Quick Polls"));
        this.eventManager = executionEvent.getEventManager();
        this.executionEvent = executionEvent;
    }

    /**
     * Begins the wizard with the quick poll voting page as the first window.
     */
    public void begin() {
        nextWindow("voteForPoll");
    }

    /**
     * Window to vote for this wizard's PollEvent.
     * 
     * @throws IllegalStateException If this wizard's PollEvent is not a QuickPoll.
     */
    protected void voteForPoll() throws IllegalStateException {
        if (!event.isQuickPoll()) {
            throw new IllegalStateException("Poll " + event.getIdentifier() + " is not a quick poll. For non-quick polls, use the PollEventWizard.");
        }

        if (!eventManager.exists(event.getIdentifier())) {
            return;
        }
        final PollEvent localEvent = (PollEvent) eventManager.get(event.getIdentifier());

        String prompt = localEvent.toString() + "\n*If this window stops working, type !poll present and select the poll's tite*";

        withResponse((response -> {
            
            if (response.equalsIgnoreCase("U+2705")) {
                localEvent.removeVoter(user.getTag());
                localEvent.addVoter("yes", user.getTag());
                eventManager.update(localEvent);
            } else if (response.equalsIgnoreCase("U+274C")) {
                localEvent.removeVoter(user.getTag());
                localEvent.addVoter("no", user.getTag());
                eventManager.update(localEvent);
            }
            nextWindow("voteForPoll");
        }), false, prompt, "U+2705", "U+274C");
    }

    /**
     * The present all quick polls window.
     */
    protected void presentQuickPolls() {
        List<SoapEvent> allPolls = eventManager.getAll(TYPE);
        List<String> prompts = new ArrayList<>();

        String prompt = "Please select a QuickPoll to display";

        for (SoapEvent poll : allPolls) {
            PollEvent pe = (PollEvent) poll;
            if (pe.isQuickPoll()) {
                prompts.add(pe.getIdentifier());
            }
        }
        if (prompts.isEmpty()) {
            sendMessage("There are no quick polls active. type !poll [prompt] to make a quick poll", "Quick Poll Wizard");
            end();
        } else {
            withResponse((response -> {
                end();
                event = (PollEvent) eventManager.get(response);
                new QuickPollWizard(executionEvent, event).begin();
            }), false, prompt, prompts.toArray(new String[prompts.size()]));
        }


    }
}
