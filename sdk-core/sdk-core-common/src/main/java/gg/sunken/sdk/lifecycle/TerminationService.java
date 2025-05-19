package gg.sunken.sdk.lifecycle;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handles execution of deferred termination routines, categorized by execution tier.
 */
@Singleton
@Slf4j
public final class TerminationService {

    private final EnumMap<Phase, Queue<Callable<?>>> taskMap = new EnumMap<>(Phase.class);
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();

    public void queue(Runnable task) {
        enqueue(Phase.MID, wrap(task));
    }

    public void queue(Callable<?> task) {
        enqueue(Phase.MID, task);
    }

    public void queue(Phase phase, Runnable task) {
        enqueue(phase, wrap(task));
    }

    public void queue(Phase phase, Callable<?> task) {
        enqueue(phase, task);
    }

    public void flush() {
        mutex.readLock().lock();
        log.info("Commencing termination sequence across {} tiers", taskMap.size());
        try {
            for (Phase phase : Phase.values()) {
                dispatch(taskMap.get(phase));
            }
            log.info("Termination sequence completed successfully");
        } finally {
            mutex.readLock().unlock();
        }
    }

    private void enqueue(Phase phase, Callable<?> task) {
        mutex.writeLock().lock();
        try {
            taskMap.computeIfAbsent(phase, ignored -> new ArrayDeque<>()).add(task);
        } finally {
            mutex.writeLock().unlock();
        }
    }

    private void dispatch(Queue<Callable<?>> tasks) {
        if (tasks == null) return;
        while (!tasks.isEmpty()) {
            Callable<?> action = tasks.poll();
            if (action == null) continue;
            try {
                action.call();
            } catch (Exception ex) {
                log.warn("Termination hook threw an exception", ex);
            }
        }
    }

    private Callable<?> wrap(Runnable r) {
        return () -> {
            r.run();
            return null;
        };
    }

    public enum Phase {
        HIGH,
        MID,
        LOW
    }

}
