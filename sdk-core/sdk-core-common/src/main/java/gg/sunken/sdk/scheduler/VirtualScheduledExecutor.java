package gg.sunken.sdk.scheduler;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class VirtualScheduledExecutor implements ScheduledExecutor {

    private static final String VIRTUAL_THREAD_PREFIX = "sunken-sdk-virtual-executor";
    private static final int DEFAULT_POOL_SIZE = 4;

    private final Executor virtualExecutor;
    private final ScheduledExecutorService scheduler;

    public VirtualScheduledExecutor() {
        this.virtualExecutor = runnable -> Thread.ofVirtual()
                .name(VIRTUAL_THREAD_PREFIX + "-" + System.nanoTime())
                .start(runnable);

        this.scheduler = Executors.newScheduledThreadPool(DEFAULT_POOL_SIZE, r -> {
            Thread thread = new Thread(r);
            thread.setName("sunken-sdk-virtual-scheduler-" + System.nanoTime());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) throw new IllegalArgumentException("Command cannot be null");
        virtualExecutor.execute(command);
    }

    @Override
    public SdkScheduledTask schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = scheduler.schedule(() -> virtualExecutor.execute(command), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public SdkScheduledTask scheduleAtFixedRate(Consumer<SdkScheduledTask> command, long initialDelay, long period, TimeUnit unit) {
        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> virtualExecutor.execute(() -> command.accept(task)), initialDelay, period, unit);
        task.setFuture(future);
        return task;
    }

    @Override
    public SdkScheduledTask scheduleIterated(Consumer<SdkScheduledTask> command, long iterations, long initialDelay, long period, TimeUnit unit) {
        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command, iterations);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> virtualExecutor.execute(() -> {
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
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
