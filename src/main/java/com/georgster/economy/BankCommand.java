package com.georgster.economy;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.profile.UserProfile;

import discord4j.core.object.entity.Member;

public class BankCommand implements Command {

    private final UserProfileManager manager;
    
    public BankCommand(ClientContext context) {
        this.manager = context.getUserProfileManager();
    }

    public void execute(CommandExecutionEvent event) {
        Member member = event.getDiscordEvent().getAuthorAsMember();
        UserProfile profile = manager.get(member.getId().asString());
        event.getGuildInteractionHandler().sendText("You have **" + profile.getBank().getBalance() + "** coins", member.getUsername() + "'s bank");
        event.getLogger().append("- Displaying a user's coin balance", LogDestination.NONAPI, LogDestination.API);
    }

    public List<String> getAliases() {
        return List.of("bank", "coins");
    }

    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- '!bank' to view your coin balance" +
        "\n*This feature is currently in beta*";
    }
}
