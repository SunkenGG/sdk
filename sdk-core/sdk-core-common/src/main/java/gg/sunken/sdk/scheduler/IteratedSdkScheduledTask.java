package gg.sunken.sdk.scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class IteratedSdkScheduledTask implements SdkScheduledTask {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final Consumer<SdkScheduledTask> consumer;
    private long remainingIterations = -1; // -1 means infinite
    private ScheduledFuture<?> future;

    public IteratedSdkScheduledTask(Consumer<SdkScheduledTask> consumer) {
        this.consumer = consumer;
    }

    public IteratedSdkScheduledTask(Consumer<SdkScheduledTask> consumer, long iterations) {
        this.consumer = consumer;
        this.remainingIterations = iterations;
    }

    @Override
    public void cancel() {
        cancelled.set(true);
        if (future != null) {
            future.cancel(false);
        }
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean tick() {
        if (remainingIterations == -1) {
            return true;
        }
        if (remainingIterations > 0) {
            remainingIterations--;
            return true;
        }
        return false;
    }
}
