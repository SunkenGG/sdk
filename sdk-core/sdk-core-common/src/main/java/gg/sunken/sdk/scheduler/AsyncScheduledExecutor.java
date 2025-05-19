package gg.sunken.sdk.scheduler;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncScheduledExecutor implements ScheduledExecutor {

    private static final String THREAD_NAME = "sunken-sdk-async-executor";
    private static final int DEFAULT_POOL_SIZE = 4;

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    public AsyncScheduledExecutor() {
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler = Executors.newScheduledThreadPool(DEFAULT_POOL_SIZE, r -> {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME + "-scheduler-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        executor.execute(command);
    }

    @Override
    public SdkScheduledTask schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = scheduler.schedule(() -> executor.execute(command), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public SdkScheduledTask scheduleAtFixedRate(Consumer<SdkScheduledTask> command, long initialDelay, long period, TimeUnit unit) {
        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> executor.execute(() -> command.accept(task)), initialDelay, period, unit);
        task.setFuture(future);
        return task;
    }

    @Override
    public SdkScheduledTask scheduleIterated(Consumer<SdkScheduledTask> command, long iterations, long initialDelay, long period, TimeUnit unit) {
        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command, iterations);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> executor.execute(() -> {
            if (!task.isCancelled() && task.tick()) {
                command.accept(task);
            } else {
                task.cancel();
            }
        }), initialDelay, period, unit);
        task.setFuture(future);
        return task;
    }

    public void shutdown() {
        scheduler.shutdown();
        executor.shutdown();
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
