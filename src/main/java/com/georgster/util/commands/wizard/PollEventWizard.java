package com.georgster.util.commands.wizard;

import java.util.List;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.poll.PollEvent;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.channel.TextChannel;

/**
 * An {@link OldInputWizard} for handling PollEvents.
 */
public class PollEventWizard extends InputWizard {

    private static final String TITLE = "Poll Event Wizard";
    private static final SoapEventType TYPE = SoapEventType.POLL;

    private SoapEventManager eventManager;

    /**
     * Creates a PollEventWizard.
     */
    public PollEventWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eventManager = event.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        handler.sendText("Welcome to the poll event wizard. At any time you can type \"stop\", or react :x: to exit the wizard", TITLE);
        nextWindow("wizardOptions");
        end();
    }

    /**
     * The main menu for the wizard.
     */
    protected void wizardOptions() {
        String prompt = "What would you like to do?";
        String[] options = {"create a poll", "vote on a poll", "view a poll"};
        withResponse((response -> {
            if (response.equals("create a poll")) {
                nextWindow("createPoll");
            } else if (response.equals("vote on a poll")) {
                if (!eventManager.hasAny(TYPE)) {
                    sendMessage("No polls currently exist", TITLE);
                } else {
                    nextWindow("pollVotingOptions");
                }
            } else if (response.equals("view a poll")) {
                if (!eventManager.hasAny(TYPE)) {
                    sendMessage("No polls currently exist", TITLE);
                } else {
                    nextWindow("pollViewingOptions");
                }
            }
        }), prompt, options);
    }

    /**
     * Options for viewing a poll.
     */
    protected void pollViewingOptions() {
        String prompt = "Which poll would you like to view?";
        String[] options = new String[eventManager.getCount(TYPE)];
        List<SoapEvent> events = eventManager.getAll(TYPE);
        for (int i = 0; i < eventManager.getCount(TYPE); i++) {
            options[i] = events.get(i).getIdentifier();
        }

        withResponseBack((response -> {
            PollEvent event = (PollEvent) eventManager.get(response);
            handler.sendText(event.toString(), event.getIdentifier());
        }), prompt, options);
    }

    /**
     * Window for creating the title for a poll.
     */
    protected void createPoll() {
        String prompt = "Please enter the prompt for the poll";

        withResponseBack((response -> {
            if (!eventManager.exists(response, TYPE)) {
                PollEvent event = new PollEvent(response, ((TextChannel) getChannel()).getName(), getUser().getTag());
                nextWindow("setExpiration", event);
            } else {
                sendMessage("A poll with that title already exists, please pick a new name", TITLE);
            }
        }), prompt);
    }

    /**
     * Window for setting the expiration for a poll.
     * 
     * @param event The PollEvent that is being built.
     */
    protected void setExpiration(PollEvent event) {
        String prompt = "Please type how long the poll should last for, or select use default setting.\n" +
                        "You can type things like: 10 days, 1 hour, 15 minutes";
        String[] options = {"use default setting"};

        withResponseBack((response -> {
            if (response.equals("use default setting")) {
                event.setDateTime("5 mins");
                nextWindow("addOptions", event);
            } else {
                try {
                    event.setDateTime(response);
                    nextWindow("addOptions", event);
                } catch (IllegalArgumentException e) {
                    sendMessage(e.getMessage(), TITLE);
                }
            }
        }), prompt, options);

    }

    /**
     * Window for adding options to a poll.
     * 
     * @param event The PollEvent that is being built.
     */
    protected void addOptions(PollEvent event) {
        String prompt = "Please type the options for the poll, one at a time in their own messages, then click continue when complete.";
        String[] options = {"continue"};

        withResponseBack((response -> {
            if (response.equals("continue")) {
                if (event.getOptions().isEmpty()) {
                    sendMessage("You must add at least 1 option", TITLE);
                } else {
                    eventManager.add(event);
                    StringBuilder sb = new StringBuilder("A new poll " + event.getIdentifier() + " has been created with the following options:\n");
                    event.getOptions().forEach(option -> sb.append("- " + option + "\n"));
                    sb.append("This poll lasts for: " + SoapUtility.convertSecondsToHoursMinutes((int) event.until()) + ". Type !poll to vote!");
                    handler.sendText(sb.toString(), "Poll Created");
                    nextWindow("wizardOptions");
                }
            } else {
                event.addOption(response);
                sendMessage("Added " + response + " to this event's options.", TITLE);
            }
        }), prompt, options);
    }

    /**
     * Window for voting on a poll.
     */
    protected void pollVotingOptions() {
        String prompt = "Which poll would you like to vote on?";
        String[] options = new String[eventManager.getCount(TYPE)];
        List<SoapEvent> events = eventManager.getAll(TYPE);
        for (int i = 0; i < eventManager.getCount(TYPE); i++) {
            options[i] = events.get(i).getIdentifier();
        }

        withResponseBack((response -> {
            PollEvent event = (PollEvent) eventManager.get(response);
            nextWindow("pollVote", event);
        }), prompt, options);
    }

    /**
     * Window for selecting an option for a poll to vote for.
     * 
     * @param event The PollEvent to vote for.
     */
    protected void pollVote(PollEvent event) {
        String voter = getUser().getTag();

        String prompt = "Please select an option to vote for.";
        List<String> options = event.getOptions();
        String[] optionsArr = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            optionsArr[i] = options.get(i);
        }

        withResponseBack((response -> {
            event.removeVoter(voter);
            event.addVoter(response, voter);
            eventManager.update(event);
            handler.sendText(getUser().getUsername() + " has voted for: " + response + ".\n Current votes are:\n" + event.toString(), event.getIdentifier());
        }), prompt, optionsArr);
    }
}