package com.georgster.util.handler;

import java.util.List;
import java.util.Optional;

import com.georgster.util.Unwrapper;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;

/**
 * An {@link InteractionHandler} for a Discord {@link Guild}.
 * <p>
 * A {@link GuildInteractionHandler}
 * provides specific utility in getting information about a {@link Guild}, and will assist
 * in replying to {@link ApplicationCommandInteractionEvent ApplicationCommandInteractionEvents}
 * when set with {@link #setActiveCommandInteraction(ApplicationCommandInteractionEvent)}, and will
 * work with reply deferrals when notified with {@link #enableReplyDeferring()}.
 */
public final class GuildInteractionHandler extends InteractionHandler {
    private Guild guild; // The guild being interacted with
    private Optional<ApplicationCommandInteractionEvent> activeCommandInteraction;

    private boolean replyWasDeferred; // If a reply was deferred, requiring an edit on response.

    /**
     * Creates a new {@link GuildInteractionHandler} for the provided {@link Guild}.
     * 
     * @param guild The {@link Guild} to handle interactions for.
     */
    public GuildInteractionHandler(Guild guild) {
        super(guild.getId());
        this.guild = guild;
        this.replyWasDeferred = false;

        this.activeCommandInteraction = Optional.empty();
    }

    /**
     * Replies to the provided {@link DeferrableInteractionEvent} and
     * switches this handler to reply deferral response mode.
     * <p>
     * If reply deferral response mode is on, this handler will edit replies from
     * {@link ApplicationCommandInteractionEvent ApplicationCommandInteractionEvents},
     * and will automatically disable once a successful defer edit has been made.
     * 
     * @param event The {@link DeferrableInteractionEvent} to defer a reply to.
     */
    public void deferReply(DeferrableInteractionEvent event) {
        event.deferReply().block();
        this.replyWasDeferred = true;
    }

    /**
     * Returns the {@link Guild} for this GuildInteractionHandler.
     * 
     * @return the {@link Guild} for this GuildInteractionHandler
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A {@link GuildInteractionHandler} will only accept a {@link GuildMessageChannel}.
     * 
     * @param messageChannel The new {@code activeChannel}.
     */
    @Override
    public void setActiveMessageChannel(MessageChannel messageChannel) {
        if (messageChannel instanceof GuildMessageChannel) {
            this.activeChannel = Optional.of(messageChannel);
        } else {
            throw new IllegalArgumentException("Only a GuildMessageChannel may be used for a GuildInteractionHandler");
        }
    }

    /**
     * Sets the {@link ApplicationCommandInteractionEvent} that this handler is actively performing actions for.
     * If there is an interaction active, the handler will reply to the interaction instead
     * of sending a message to the active channel on {@link #sendText(String)}.
     * 
     * @param interaction the new active interaction event
     */
    public void setActiveCommandInteraction(ApplicationCommandInteractionEvent newEvent) {
        this.activeCommandInteraction = Optional.of(newEvent);
    }

    /**
     * Returns the {@link ApplicationCommandInteractionEvent} this handler is actively performing actions for, if present.
     * 
     * @return The {@link ApplicationCommandInteractionEvent} this handler is actively performing actions for, if present.
     */
    public Optional<ApplicationCommandInteractionEvent> getActiveCommandInteraction() {
        return activeCommandInteraction;
    }

    /**
     * Kills the active {@link ApplicationCommandInteractionEvent}.
     */
    public void killActiveCommandInteraction() {
        this.activeCommandInteraction = Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendPlainMessage(String text) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            if (replyWasDeferred) {
                interaction.editReply(text).block();
            } else {
                interaction.reply(text).block();
                replyWasDeferred = false;
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendPlainMessage(channel, text))));

        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text))));

        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, MessageFormatting format) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, format))));

        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title, MessageFormatting format) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, format))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title, String imageUrl) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).image(imageUrl).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title, String imageUrl, MessageFormatting format) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).image(imageUrl).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl, format))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, components))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title, MessageFormatting format, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, format, components))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title, String imageUrl, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).image(imageUrl).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl, components))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(String text, String title, String imageUrl, MessageFormatting format, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).image(imageUrl).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl, format, components))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(EmbedCreateSpec spec) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            if (replyWasDeferred) {
                InteractionReplyEditSpec editSpec = InteractionReplyEditSpec.builder().addEmbed(spec).build();
                interaction.editReply(editSpec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec replySpec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(spec).build();
                interaction.reply(replySpec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, spec))));
        return message.getObject();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this {@link GuildInteractionHandler} has an {@link #getActiveCommandInteraction() ActiveCommandInteraction}, it
     * will reply to the event when sending the message instead.
     */
    @Override
    public Message sendMessage(EmbedCreateSpec spec, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            if (replyWasDeferred) {
                InteractionReplyEditSpec editSpec = InteractionReplyEditSpec.builder().addEmbed(spec).addAllComponents(List.of(components)).build();
                interaction.editReply(editSpec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec replySpec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(spec).addAllComponents(List.of(components)).build();
                interaction.reply(replySpec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, spec, components))));
        return message.getObject();
    }

    /**
     * Returns a list of all the channels in the guild.
     * 
     * @return a list of all the channels in the guild
     */
    public List<GuildChannel> getAllChannels() {
        return guild.getChannels().collectList().block();
    }

    /**
     * Returns a list of all the message channels in the guild.
     * 
     * @return a list of all the message channels in the guild
     */
    public List<GuildMessageChannel> getMessageChannels() {
        return guild.getChannels().ofType(GuildMessageChannel.class).collectList().block();
    }

    /**
     * Returns a list of all the voice channels in the guild.
     * 
     * @return a list of all the voice channels in the guild
     */
    public List<VoiceChannel> getVoiceChannels() {
        return guild.getChannels().ofType(VoiceChannel.class).collectList().block();
    }

    /**
     * Returns a list of all the members in the guild.
     * 
     * @return a list of all the members in the guild
     */
    public List<Member> getAllMembers() {
        return guild.getMembers().collectList().block();
    }

    /**
     * Returns a list of all the roles in the guild.
     * 
     * @return a list of all the roles in the guild
     */
    public List<Role> getAllRoles() {
        return guild.getRoles().collectList().block();
    }

    /**
     * Returns the {@link Member} in this {@link Guild} that has the given tag.
     * 
     * @param memberTag The tag of the member to get
     * @return the {@link Member} in this {@link Guild} that has the given tag
     */
    public Member getMemberByTag(String memberTag) {
        for (Member member : getAllMembers()) {
            if (member.getTag().equals(memberTag))
                return member;
        }
        return null;
    }

    /**
     * Returns the {@link Member} in this {@link Guild} that has the given username.
     * 
     * @param memberName The username of the member to get
     * @return the {@link Member} in this {@link Guild} that has the given username
     */
    public Member getMemberByName(String memberName) {
        for (Member member : getAllMembers()) {
            if (member.getUsername().equals(memberName)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Returns the {@link Member} in this {@link Guild} that has the given id.
     * 
     * @param id The id of the member to get
     * @return the {@link Member} in this {@link Guild} that has the given id
     */
    public Member getMemberById(String id) {
        for (Member member : getAllMembers()) {
            if (member.getId().asString().equals(id)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Returns the {@link Role} in this {@link Guild} that has the given name.
     * 
     * @param roleName The name of the role to get
     * @return the {@link Role} in this {@link Guild} that has the given name
     */
    public Role getRole(String roleName) {
        for (Role role : getAllRoles()) {
            if (role.getName().equals(roleName))
                return role;
        }
        return null;
    }

    /**
     * Returns the {@link Role} in this {@link Guild} that has the given id.
     * 
     * @param roleId The id of the role to get
     * @return the {@link Role} in this {@link Guild} that has the given id
     */
    public Role getRoleById(String roleId) {
        for (Role role : getAllRoles()) {
            if (role.getId().asString().equals(roleId))
                return role;
        }
        return null;
    }

    /**
     * Returns the {@link GuildMessageChannel} in this {@link Guild} that has the given name.
     * 
     * @param channelName The name of the channel to get
     * @return the {@link GuildMessageChannel} in this {@link Guild} that has the given name
     */
    public GuildMessageChannel getMessageChannel(String channelName) {
        for (GuildMessageChannel channel : getMessageChannels()) {
            if (channel.getName().equals(channelName)) {
                return channel;
            }
        }
        return null;
    }
}
