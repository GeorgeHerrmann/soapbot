package com.georgster.util.commands.wizard;

import java.util.List;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.poll.PollEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.channel.TextChannel;

public class PollEventWizard extends InputWizard {

    private static final String TITLE = "Poll Event Wizard";
    private static final SoapEventType TYPE = SoapEventType.POLL;

    private SoapEventManager eventManager;

    public PollEventWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eventManager = event.getEventManager();
    }

    public void begin() {
        handler.sendText("Welcome to the poll event wizard. At any time you can type \"stop\", or react :x: to exit the wizard");
        wizardOptions();
        end();
    }

    private void wizardOptions() {
        String prompt = "What would you like to do?";
        String[] options = {"create a poll", "vote on a poll", "edit a poll"};
        while (isActive()) {
            withResponseFirst((response -> {
                if (response.equals("create a poll")) {
                    createPoll();
                } else if (response.equals("vote on a poll")) {
                    if (!eventManager.hasAny(TYPE)) {
                        handler.sendText("No polls currently exist", TITLE);
                    } else {
                        pollVotingOptions();
                    }
                } else {
                    handler.sendText("Sorry, editing poll events is not currently supported", TITLE);
                }
            }), prompt, options);
        }
    }

    private void createPoll() {
        String prompt = "Please enter the prompt for the poll";

        while (isActive()) {
            WizardResponse wizardResponse = withResponse((response -> {
                if (!eventManager.exists(response, TYPE)) {
                    PollEvent event = new PollEvent(response, ((TextChannel) getChannel()).getName(), getUser().getTag());
                    addOptions(event);
                } else {
                    handler.sendText("A poll with that title already exists, please pick a new name", TITLE);
                }
            }), prompt);

            if (wizardResponse == WizardResponse.BACK) {
                return;
            }
        }
    }

    private void addOptions(PollEvent event) {
        String prompt = "Please type the options for the poll, one at a time in their own messages, then click continue when complete.";
        String[] options = {"continue"};

        while (isActive()) {
            withResponseFirst((response -> {
                if (response.equals("continue")) {
                    event.addVoter(event.getOptions().get(0), getUser().getTag());
                    eventManager.add(event);
                    StringBuilder sb = new StringBuilder("A new poll " + event.getIdentifier() + " has been created with the following options:\n");
                    event.getOptions().forEach(option -> sb.append("- " + option + "\n"));
                    sb.append("Type !poll vote to vote!");
                    handler.sendText(sb.toString(), "Poll Created");
                    wizardOptions();
                } else {
                    event.addOption(response);
                    handler.sendText("Added " + response + " to this event's options.", TITLE);
                }
            }), prompt, options);
        }
    }

    private void pollVotingOptions() {
        while (isActive()) {
            String prompt = "Which poll would you like to vote on?";
            String[] options = new String[eventManager.getCount()];
            List<SoapEvent> events = eventManager.getAll(TYPE);
            for (int i = 0; i < eventManager.getCount(); i++) {
                options[i] = events.get(i).getIdentifier();
            }

            WizardResponse wizardResponse = withResponse((response -> {
                PollEvent event = (PollEvent) eventManager.get(response);
                pollVote(event);
            }), prompt, options);

            if (wizardResponse == WizardResponse.BACK) {
                return;
            }
        }
    }

    private void pollVote(PollEvent event) {
        String voter = getUser().getTag();

        String prompt = "Please select an option to vote for.";
        List<String> options = event.getOptions();
        String[] optionsArr = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            optionsArr[i] = options.get(i);
        }

        withResponse((response -> {
            event.removeVoter(voter);
            event.addVoter(response, voter);
            eventManager.update(event);
            handler.sendText("You have voted for: " + response);
        }), prompt, optionsArr);
    }
}
