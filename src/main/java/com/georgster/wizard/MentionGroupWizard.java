package com.georgster.wizard;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.manager.MentionGroupManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.mentiongroups.MentionGroup;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

/**
 * A wizard for creating and editing {@link MentionGroup MentionGroups}.
 */
public final class MentionGroupWizard extends InputWizard {
    
    private final MentionGroupManager manager;
    private boolean isEdit; // If this wizard should start on the edit path

    /**
     * Creates a new MentionGroupWizard.
     * 
     * @param event The event that triggered this wizard
     * @param isEdit If this wizard should start on the edit path
     */
    public MentionGroupWizard(CommandExecutionEvent event, boolean isEdit) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Mention Group Wizard").builder().requireMatch(false, false).withTimeoutDuration(120000).build());
        this.manager = event.getMentionGroupManager();
        this.isEdit = isEdit;
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        if (isEdit) {
            nextWindow("editGroups");
        } else {
            nextWindow("newGroupName");
        }
        end();
    }

    /**
     * The window that starts the process of creating a new mention group.
     */
    protected void newGroupName() {
        final String prompt = "What should the name of this group be? You will use this name to mention the group.";

        withResponse(response -> {
            MentionGroup group = new MentionGroup(response);
            nextWindow("addMembers", group);
        }, false, prompt);
    }

    /**
     * The window that adds members to a new mention group.
     * 
     * @param group The group to add members to
     */
    protected void addMembers(MentionGroup group) {
        final StringBuilder prompt = new StringBuilder();
        prompt.append("Please mention the members you would like to add to this group. You may do this all in one message or in multiple messages.\n");
        prompt.append("Current members:\n");
        group.getMemberIds().forEach(memberId -> prompt.append("- " + event.getGuildInteractionHandler().getMemberById(memberId).getMention()).append("\n"));
        prompt.append("\n *Once you are done, type \"done\"*");

        withFullResponse(response -> {
            if (response.getResponse().equals("done")) {
                if (group.getMemberIds().isEmpty()) {
                    sendMessage("You must add at least one member to this group.", "Missing Members");
                } else {
                    sendMessage("Successfully created mention group: " + group.getIdentifier() + "\nThis group can be mentioned with !mention " + group.getIdentifier(), "Group " + group.getIdentifier() + " Created");
                    manager.add(group);
                    end();
                }
            } else {
                Message msg = response.getMessage();
                msg.getMemberMentions().forEach(member -> group.addMember(member.getId().asString()));
            }
        }, true, prompt.toString(), "done");
    }

    /**
     * The window that starts the process of editing a mention group, by listing all mention groups.
     */
    protected void editGroups() {

        if (manager.isEmpty()) {
            sendMessage("There are no mention groups to edit.", "No Mention Groups");
            end();
        } else {
            final InputListener newListener = InputListenerFactory.createMenuMessageListener(event, "Mention Group Wizard");

            final String prompt = "Please select the group you would like to edit.";
            List<String> opList = new ArrayList<>();

            for (MentionGroup group : manager.getAll()) {
                opList.add(group.getIdentifier());
            }

            withResponse(response -> {
                if (manager.exists(response)) {
                    MentionGroup group = manager.get(response);
                    nextWindow("editGroupOptions", group);
                } else {
                    sendMessage("There is no mention group with that name.", "Invalid Group");
                }
            }, false, newListener, prompt, opList.toArray(new String[opList.size()]));
        }
    }

    /**
     * The window that lists the options for editing a mention group.
     * 
     * @param group The group to edit
     */
    protected void editGroupOptions(MentionGroup group) {
        final String prompt = "What would you like to do with mention group " + group.getIdentifier() + "?";

        withResponse(response -> {
            if (response.equals("add members")) {
                nextWindow("addGroupMembers", group);
            } else if (response.equals("remove members")) {
                nextWindow("removeGroupMembers", group);
            } else if (response.equals("delete group")) {
                nextWindow("confirmGroupDelete", group);
            }
        }, true, prompt, "add members", "remove members", "delete group");
    }

    /**
     * The window that adds members to an existing mention group.
     * 
     * @param group The group to add members to
     */
    protected void addGroupMembers(MentionGroup group) {
        final StringBuilder prompt = new StringBuilder();
        prompt.append("Please mention the members you would like to add to this group. You may do this all in one message or in multiple messages.\n");
        prompt.append("Current members:\n");
        group.getMemberIds().forEach(memberId -> prompt.append("- " + event.getGuildInteractionHandler().getMemberById(memberId).getMention()).append("\n"));
        prompt.append("\n *Once you are done, type or click \"back\"*");

        withFullResponse(response -> {
            Message msg = response.getMessage();
            msg.getMemberMentions().forEach(member -> group.addMember(member.getId().asString()));
            manager.update(group);
        }, true, prompt.toString());
    }

    /**
     * The window that removes members from an existing mention group.
     * 
     * @param group The group to remove members from
     */
    protected void removeGroupMembers(MentionGroup group) {
        final InputListener newListener = InputListenerFactory.createMenuMessageListener(event, "Select Members to Remove");

        final String prompt = "Please select the members you would like to remove from this group, or select or type back when you are finished. *Removing all members will delete the group.*";
        final List<String> options = new ArrayList<>();
        final List<Member> members = new ArrayList<>();
        group.getMemberIds().forEach(memberId -> {
            Member member = event.getGuildInteractionHandler().getMemberById(memberId);
            options.add(member.getUsername());
            members.add(member);
        });

        withResponse(response -> {
            if (group.getMemberIds().size() == 1) {
                nextWindow("confirmGroupDelete", group);
            } else {
                group.removeMember(members.stream().filter(m -> m.getUsername().equals(response)).findFirst().get().getId().asString());
                manager.update(group);
            }
        }, true, newListener, prompt, options.toArray(new String[options.size()]));
    }

    /**
     * The window that confirms the deletion of an existing mention group.
     * 
     * @param group The group to delete
     */
    protected void confirmGroupDelete(MentionGroup group) {
        final String prompt = "Are you sure you want to delete mention group " + group.getIdentifier() + "? ***This action cannot be undone.***";

        withResponse(response -> {
            if (response.equals("confirm")) {
                sendMessage("Successfully deleted mention group: " + group.getIdentifier(), "Group " + group.getIdentifier() + " Deleted");
                manager.remove(group);
                if (manager.isEmpty()) {
                    end();
                } else {
                    nextWindow("editGroups");
                }
            }
        }, true, prompt, "confirm");
    }

}
