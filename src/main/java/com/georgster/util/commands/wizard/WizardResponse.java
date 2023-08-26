package com.georgster.util.commands.wizard;

import com.georgster.util.DateTimed;

import discord4j.core.object.entity.User;

public class WizardResponse extends DateTimed {
    private final User responder;
    private final String response;
    private final String notes;

    protected WizardResponse(User responser, String response) {
        this.responder = responser;
        this.response = response;
        this.notes = "";
    }

    protected WizardResponse(User responser, String response, String notes) {
        this.responder = responser;
        this.response = response;
        this.notes = notes;
    }

    public User getResponder() {
        return responder;
    }

    public String getResponse() {
        return response;
    }

    public String getNotes() {
        return notes;
    }
}
