package com.georgster.elo;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.elo.manager.EloBattleManager;
import com.georgster.elo.wizard.BattleWizard;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A simple {@link Command} for starting battles.
 */
public class BattleCommand implements Command {

    private final EloBattleManager battleManager;

    /**
     * Creates a new BattleCommand from the provided context.
     * 
     * @param context The context for this command's SOAPClient.
     */
    public BattleCommand(ClientContext context) {
        MultiLogger.logSystem("BattleCommand constructor called", getClass());
        MultiLogger.logSystem("ClientContext is: " + (context != null ? "NOT NULL" : "NULL"), getClass());
        
        this.battleManager = context.getEloBattleManager();
        MultiLogger.logSystem("EloBattleManager obtained: " + (this.battleManager != null ? "NOT NULL" : "NULL"), getClass());
        
        if (this.battleManager == null) {
            MultiLogger.logSystem("WARNING: EloBattleManager is NULL!", getClass());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandExecutionEvent event) {
        event.getLogger().append("- Starting battle wizard", LogDestination.NONAPI);
        MultiLogger.logSystem("BattleCommand.execute() called for user: " + event.getDiscordEvent().getUser().getUsername(), getClass());
        
        try {
            // Start battle wizard - it will handle active battles, pending battles, or new battle creation
            MultiLogger.logSystem("Creating new BattleWizard...", getClass());
            BattleWizard wizard = new BattleWizard(event);
            MultiLogger.logSystem("BattleWizard created, calling begin()...", getClass());
            wizard.begin();
            MultiLogger.logSystem("BattleWizard.begin() completed", getClass());
            
        } catch (Exception e) {
            MultiLogger.logSystem("ERROR in BattleCommand.execute(): " + e.getMessage(), getClass());
            e.printStackTrace();
            event.getGuildInteractionHandler().sendMessage(
                "An error occurred while starting the battle: " + e.getMessage(),
                "Battle Error"
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAliases() {
        return List.of("battle", "fight", "duel");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Aliases: " + getAliases().toString() +
               "\n- '!battle' - Start a new Elo battle" +
               "\n*Challenge other players to ranked or unranked battles!*";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Start a new Elo battle")
                .build();
    }
}
