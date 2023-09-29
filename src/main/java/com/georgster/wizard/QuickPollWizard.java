package com.georgster.wizard;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.poll.PollEvent;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

/**
 * A wizard designed to handle {@link PollEvent PollEvents} which are {@code QuickPolls}.
 */
public class QuickPollWizard extends InputWizard {
    private PollEvent pollEvent;
    private final SoapEventManager eventManager;
    private static final SoapEventType TYPE = SoapEventType.POLL;

    /**
     * Creates a new QuickPollWizard with a ReactionListener as the way to vote for the poll.
     * 
     * @param executionEvent The event that prompted the wizard's creation.
     * @param event The PollEvent (a quickpoll).
     */
    public QuickPollWizard(CommandExecutionEvent executionEvent, PollEvent event) {
        super(executionEvent, InputListenerFactory.createReactionListener(executionEvent, event.getIdentifier()).builder().withApiCallsOnSeparateThread(true).allowAllResponses(true).withXReaction(false).withTimeoutDuration(120000).build());
        this.pollEvent = event;
        this.eventManager = executionEvent.getEventManager();
    }

    /**
     * Creates a new QuickPollWizard with a MenuMessageListener as a way to present all QuickPolls.
     * 
     * @param executionEvent The event that prompted the wizard's creation.
     */
    public QuickPollWizard(CommandExecutionEvent executionEvent) {
        super(executionEvent, InputListenerFactory.createMenuMessageListener(executionEvent, "All Quick Polls"));
        this.eventManager = executionEvent.getEventManager();
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
        if (!pollEvent.isQuickPoll()) {
            throw new IllegalStateException("Poll " + pollEvent.getIdentifier() + " is not a quick poll. For non-quick polls, use the PollEventWizard.");
        }

        if (!eventManager.exists(pollEvent.getIdentifier())) {
            return;
        }
        final PollEvent localEvent = (PollEvent) eventManager.get(pollEvent.getIdentifier());

        String prompt = localEvent.toString() + "\n*If this window stops working, type !poll present and select the poll's tite*";

        InputListener qpListener = InputListenerFactory.createReactionListener(event, pollEvent.getIdentifier()).builder().withApiCallsOnSeparateThread(true).allowAllResponses(true).withXReaction(false).withTimeoutDuration(120000).build();

        withFullResponse((output -> {
            String response = output.getResponse();
            
            if (response.equalsIgnoreCase("U+2705")) {
                localEvent.removeVoter(user.getId().asString());
                if (output.getNotes().equals("added")) {
                    localEvent.addVoter("yes", user.getId().asString());
                }
                eventManager.update(localEvent);
            } else if (response.equalsIgnoreCase("U+274C")) {
                localEvent.removeVoter(user.getId().asString());
                if (output.getNotes().equals("added")) {
                    localEvent.addVoter("no", user.getId().asString());
                }
                eventManager.update(localEvent);
            }
            nextWindow("voteForPoll");
        }), false, qpListener, prompt, "U+2705", "U+274C");
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
                pollEvent = (PollEvent) eventManager.get(response);
                nextWindow("voteForPoll");
            }), false, prompt, prompts.toArray(new String[prompts.size()]));
        }


    }
}
