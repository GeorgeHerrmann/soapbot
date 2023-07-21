package com.georgster.util.commands.wizard;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.poll.PollEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

public class QuickPollWizard extends InputWizard {
    private PollEvent event;
    private final SoapEventManager eventManager;
    private CommandExecutionEvent executionEvent;
    private static final SoapEventType TYPE = SoapEventType.POLL;

    public QuickPollWizard(CommandExecutionEvent executionEvent, PollEvent event) {
        super(executionEvent, InputListenerFactory.createReactionListener(executionEvent, event.getIdentifier(), false).builder().withXReaction(false).build());
        this.event = event;
        this.eventManager = executionEvent.getEventManager();
        this.executionEvent = executionEvent;
    }

    public QuickPollWizard(CommandExecutionEvent executionEvent) {
        super(executionEvent, InputListenerFactory.createMenuMessageListener(executionEvent, "All Quick Polls"));
        this.eventManager = executionEvent.getEventManager();
        this.executionEvent = executionEvent;
    }

    public void begin() {
        nextWindow("voteForPoll");
    }

    protected void voteForPoll() throws IllegalStateException {
        if (!event.isQuickPoll()) {
            throw new IllegalStateException("Poll " + event.getIdentifier() + " is not a quick poll. For non-quick polls, use the PollEventWizard.");
        }

        final PollEvent localEvent = (PollEvent) eventManager.get(event.getIdentifier());

        String prompt = localEvent.toString();

        withResponse((response -> {
            System.out.println(response);
            if (response.equalsIgnoreCase("U+2705")) {
                localEvent.removeVoter(user.getTag());
                localEvent.addVoter("yes", user.getTag());
                eventManager.update(localEvent);
                //handler.sendText(getUser().getUsername() + " has voted for: " + "yes" + ".\n Current votes are:\n" + localEvent.toString(), localEvent.getIdentifier());
            } else if (response.equalsIgnoreCase("U+274C")) {
                localEvent.removeVoter(user.getTag());
                localEvent.addVoter("no", user.getTag());
                eventManager.update(localEvent);
                //handler.sendText(getUser().getUsername() + " has voted for: " + "no" + ".\n Current votes are:\n" + localEvent.toString(), localEvent.getIdentifier());
            }
            nextWindow("voteForPoll");
        }), false, prompt, "U+2705", "U+274C");
    }

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
