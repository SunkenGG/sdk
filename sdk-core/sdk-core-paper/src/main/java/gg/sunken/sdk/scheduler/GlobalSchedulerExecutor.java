package gg.sunken.sdk.scheduler;

import gg.sunken.sdk.utils.TickUtil;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GlobalSchedulerExecutor implements ScheduledExecutor {

    private final GlobalRegionScheduler scheduler;
    private final Plugin plugin;

    public GlobalSchedulerExecutor(Plugin plugin) {
        this.scheduler = Bukkit.getGlobalRegionScheduler();
        this.plugin = plugin;
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        scheduler.execute(plugin, command);
    }

    @Override
    public SdkScheduledTask schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("Delay cannot be negative");
        }

        io.papermc.paper.threadedregions.scheduler.ScheduledTask task = scheduler.runDelayed(plugin, scheduledTask -> command.run(), TickUtil.toTicks(delay, unit));
        return task::cancel;
    }

    @Override
    public SdkScheduledTask scheduleAtFixedRate(Consumer<SdkScheduledTask> command, long initialDelay, long period, TimeUnit unit) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (initialDelay < 0 || period <= 0) {
            throw new IllegalArgumentException("Initial delay cannot be negative and period must be positive");
        }

        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command);
        scheduler.runAtFixedRate(plugin, scheduledTask -> {
            if (!task.isCancelled() && task.tick()) {
                command.accept(task);
            } else {
                task.cancel();
            }
        }, TickUtil.toTicks(initialDelay, unit), TickUtil.toTicks(period, unit));

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

        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command, iterations);
        scheduler.runAtFixedRate(plugin, scheduledTask -> {
            if (!task.isCancelled() && task.tick()) {
                command.accept(task);
            } else {
                task.cancel();
            }
        }, TickUtil.toTicks(initialDelay, unit), TickUtil.toTicks(period, unit));

        return task;
    }
}
