package gg.sunken.sdk.scheduler;

import gg.sunken.sdk.utils.TickUtil;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EntitySchedulerExecutor implements ScheduledExecutor {

    private final EntityScheduler scheduler;
    private final Plugin plugin;

    public EntitySchedulerExecutor(Plugin plugin, Entity entity) {
        this.scheduler = entity.getScheduler();
        this.plugin = plugin;
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        scheduler.execute(plugin, command, () -> {}, 0);
    }

    @Override
    public SdkScheduledTask schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("Delay cannot be negative");
        }

        long ticks = TickUtil.toTicks(delay, unit);
        if (ticks <= 0) {
            execute(command);
            return () -> {}; // No task to cancel, just return a no-op
        }

        ScheduledTask delayed = scheduler.runDelayed(plugin, scheduledTask -> command.run(), () -> {
        }, ticks);

        if (delayed == null) {
            throw new IllegalStateException("Failed to schedule task with delay: " + delay + " " + unit);
        }

        if (delayed.isCancelled()) {
            return () -> {}; // No task to cancel, just return a no-op
        }

        return delayed::cancel;
    }

    @Override
    public SdkScheduledTask scheduleAtFixedRate(Consumer<SdkScheduledTask> command, long initialDelay, long period, TimeUnit unit) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (initialDelay < 0 || period <= 0) {
            throw new IllegalArgumentException("Initial delay cannot be negative and period must be positive");
        }

        long initialTicks = TickUtil.toTicks(initialDelay, unit);
        long periodTicks = TickUtil.toTicks(period, unit);

        if (initialTicks < 0 || periodTicks <= 0) {
            throw new IllegalArgumentException("Initial delay cannot be negative and period must be positive");
        }

        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command);
        scheduler.runAtFixedRate(plugin, scheduledTask -> {
            if (!task.isCancelled() && task.tick()) {
                command.accept(task);
            } else {
                task.cancel();
            }
        }, () -> {}, initialTicks, periodTicks);

        return task;
    }

    @Override
    public SdkScheduledTask scheduleIterated(Consumer<SdkScheduledTask> command, long iterations, long initialDelay, long period, TimeUnit unit) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (iterations < 0 || initialDelay < 0 || period <= 0) {
            throw new IllegalArgumentException("Iterations cannot be negative, initial delay cannot be negative, and period must be positive");
        }

        long initialTicks = TickUtil.toTicks(initialDelay, unit);
        long periodTicks = TickUtil.toTicks(period, unit);

        if (initialTicks < 0 || periodTicks <= 0) {
            throw new IllegalArgumentException("Initial delay cannot be negative and period must be positive");
        }

        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command, iterations);
        scheduler.runAtFixedRate(plugin, scheduledTask -> {
            if (!task.isCancelled() && task.tick()) {
                command.accept(task);
            } else {
                task.cancel();
            }
        }, () -> {}, initialTicks, periodTicks);

        return task;
    }
}
