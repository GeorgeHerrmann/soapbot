package com.georgster.util.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
    private String guildId;

    private final ExecutorService databaseThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService eventThreadPool = Executors.newFixedThreadPool(10); //Can schedule 10 events at once
    private final ExecutorService commandThreadPool = Executors.newFixedThreadPool(3);
    private final ExecutorService voiceThreadPool = Executors.newSingleThreadExecutor();

    protected ThreadPoolManager(String guildId) {
        this.guildId = guildId;
    }

    protected String getGuildId() {
        return guildId;
    }

    protected void scheduleDatabaseTask(Runnable task) {
        databaseThreadPool.submit(task);
    }

    protected void scheduleEventTask(Runnable task) {
        eventThreadPool.submit(task);
    }

    protected void scheduleCommandTask(Runnable task) {
        commandThreadPool.submit(task);
    }

    protected void scheduleVoiceTask(Runnable task) {
        voiceThreadPool.submit(task);
    }
}
