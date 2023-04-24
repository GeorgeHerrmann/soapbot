package com.georgster.util.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadPoolFactory {
    private static final Map<String, ThreadPoolManager> CLIENT_THREAD_POOL_MANAGERS = new ConcurrentHashMap<>();

    private ThreadPoolFactory() {
        throw new IllegalStateException("Utility factory class");
    }

    public static void createThreadPoolManager(String guildId) {
        CLIENT_THREAD_POOL_MANAGERS.computeIfAbsent(guildId, ThreadPoolManager::new);
    }

    public static void scheduleDatabaseTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleDatabaseTask(task);
    }

    public static void scheduleEventTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleEventTask(task);
    }

    public static void scheduleCommandTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleCommandTask(task);
    }

    public static void scheduleVoiceTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleVoiceTask(task);
    }
}
