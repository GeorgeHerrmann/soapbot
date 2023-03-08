package com.georgster.control.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.georgster.Command;
import com.georgster.control.PermissionsManager;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;

/**
 * A Pipeline that carries information about a Discord {@code Event} to a {@code Command}.
 * If event-specific data is requested from the pipeline, the pipeline will attempt to
 * retrieve the data from the event, returning some default value if not present.
 */
public class CommandPipeline {
    
    private final Event event; // The event that triggered the creation of this pipeline
    private Member member; // The member that triggered the event
    private PermissionsManager permissionsManager; // The permissions manager for the guild that the event was triggered in

    /**
     * Creates a new CommandPipeline with the given {@code Event}.
     * 
     * @param event the event
     */
    public CommandPipeline(Event event) {
        this.event = event;
    }

    /**
     * Returns the event that this pipeline is carrying.
     * 
     * @return the event that this pipeline is carrying
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Returns the guild that the event was triggered in.
     * Compatiable with the following Discord {@code Events}:
     * <ul>
     * <li>{@code MessageCreateEvent}</li>
     * <li>{@code ChatInputInteractionEvent}</li>
     * </ul>
     * 
     * @return the guild that the event was triggered in
     */
    public Guild getGuild() {
        if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getGuild().block();
        } else if (event instanceof ChatInputInteractionEvent) {
            return ((ChatInputInteractionEvent) event).getInteraction().getGuild().block();
        } else {
            return null;
        }
    }

    /**
     * Returns the name of the command that triggered the event in this pipeline.
     * Compatiable with the following Discord {@code Events}:
     * <ul>
     * <li>{@code MessageCreateEvent}</li>
     * <li>{@code ChatInputInteractionEvent}</li>
     * </ul>
     * 
     * @return the name of the command that triggered the event in this pipeline
     */
    public String getCommandName() {
        if (event instanceof ChatInputInteractionEvent) {
            return ((ChatInputInteractionEvent) event).getCommandName();
        } else if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getMessage().getContent().split(" ")[0].replace("!", "");
        } else {
            return null;
        }
    }

    /**
     * Returns a String representing the main content of the event.
     * The behavior for each supported {@code Event} is as follows:
     * <ul>
     * <li>{@code MessageCreateEvent}: the content of the {@code Message}</li>
     * <li>{@code ChatInputInteractionEvent}: The command name followed by each present option in the order the user inputted</li>
     * </ul>
     * 
     * @return a String representing the main content of the event
     */
    public String getFormattedMessage() {
        if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getMessage().getContent();
        } else if (event instanceof ChatInputInteractionEvent) {
            StringBuilder response = new StringBuilder();
            ((ChatInputInteractionEvent) event).getInteraction().getCommandInteraction().ifPresent(
                commandInteraction -> {
                    response.append(commandInteraction.getName().get() + " ");
                    for (ApplicationCommandInteractionOption option : commandInteraction.getOptions()) {
                        try {
                            response.append(option.getValue().get().asString() + " ");
                        } catch (IllegalArgumentException e) {
                            try {
                                response.append(option.getValue().get().asLong() + " ");
                            } catch (IllegalArgumentException e2) {
                                response.append(option.getValue().get().asUser().block().getMention() + " ");
                            }
                        }
                    }
                });
            ((ChatInputInteractionEvent) event).getInteraction().getMessage().ifPresent(message -> response.append(message.getContent()));

            return response.toString().trim();
        } else {
            return null;
        }
    }

    /**
     * Returns the channel that the event was triggered in.
     * Compatiable with the following Discord {@code Events}:
     * <ul>
     * <li>{@code MessageCreateEvent}</li>
     * <li>{@code ChatInputInteractionEvent}</li>
     * </ul>
     * 
     * @return the channel that the event was triggered in
     */
    public Channel getChannel() {
        if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getMessage().getChannel().block();
        } else if (event instanceof ChatInputInteractionEvent) {
            return ((ChatInputInteractionEvent) event).getInteraction().getChannel().block();
        } else {
            return null;
        }
    }

    /**
     * Attempts to pull any mentions or noted users from the event.
     * 
     * @return a list of users mentioned in the event
     */
    public List<User> getPresentUsers() {
        if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getMessage().getUserMentions();
        } else if (event instanceof ChatInputInteractionEvent) {
            List<User> users = new ArrayList<>();
            ((ChatInputInteractionEvent) event).getInteraction().getCommandInteraction().ifPresent(commandInteraction -> 
                users.addAll(commandInteraction.getOptions().stream()
                .filter(option -> option.getType().equals(ApplicationCommandOption.Type.USER))
                .map(option -> option.getValue().get().asUser().block())
                .collect(Collectors.toList()))
            );
            return users;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Attempts to get the author of the event as an {@code Optional}.
     * 
     * @return an {@code Optional} containing the author of the event,
     *        or an empty {@code Optional} if the event does not have an author.
     */
    public Optional<User> getAuthorOptionally() {
        if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getMessage().getAuthor();
        } else if (event instanceof ChatInputInteractionEvent) {
            return Optional.of(((ChatInputInteractionEvent) event).getInteraction().getUser());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Attempts to get the author of the event as a {@code Member}.
     * 
     * @return the author of the event as a {@code Member},
     *        or {@code null} if the event does not have an author.
     */
    public Member getAuthorAsMember() {
        if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getMessage().getAuthorAsMember().block();
        } else if (event instanceof ChatInputInteractionEvent) {
            ((ChatInputInteractionEvent) event).getInteraction().getMember().ifPresent(mem -> member = mem);
            return member;
        } else {
            return null;
        }
    }

    /**
     * Returns whether this pipeline's event is a {@code ChatInteractionEvent}.
     * 
     * @return whether this pipeline's event is a {@code ChatInteractionEvent}
     */
    public boolean isChatInteraction() {
        return event instanceof ChatInputInteractionEvent;
    }

    /**
     * Returns whether this pipeline's event is a {@code MessageCreateEvent}.
     * 
     * @return whether this pipeline's event is a {@code MessageCreateEvent}
     */
    public boolean isMessageCreate() {
        return event instanceof MessageCreateEvent;
    }

    public boolean hasPermission(Command command) {
        
    }

    /**
     * Sets the {@code PermissionsManager} for this pipeline.
     * 
     * @param permissionsManager the {@code PermissionsManager} to be sent to the {@code Command}.
     */
    public void setPermissionsManager(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    /**
     * Returns the {@code PermissionsManager} for this pipeline.
     * 
     * @return the {@code PermissionsManager} for this pipeline
     */
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

}
