package com.georgster.util.commands.wizard.input;

import discord4j.core.event.EventDispatcher;
import reactor.core.Disposable;

/**
 * Functional interface for creating {@link Disposable} temporary listeners.
 */
@FunctionalInterface
public interface ListenerFactory {
    /**
     * Creates a new {@link Disposable} temporary event listener.
     * Generally, using {@link EventDispatcher#on(Class)} is reccomended to gain a {@code Flux}
     * that can be manipulated. {@code Flux.Subscribe()} can be used to subscribe logic to the event
     * and gain the {@link Disposable}.
     * 
     * @param dispatcher The {@link EventDispatcher} dispatching events.
     * @return A temporary {@link Disposable} event listener.
     */
    public Disposable createListener(EventDispatcher dispatcher);
}
