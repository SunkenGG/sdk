package gg.sunken.sdk.scheduler;

import gg.sunken.sdk.utils.TickUtil;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BlockSchedulerExecutor implements ScheduledExecutor {

    private final RegionScheduler scheduler;
    private final Plugin plugin;
    private final Location location;

    public BlockSchedulerExecutor(Plugin plugin, Block block) {
        this.scheduler = Bukkit.getRegionScheduler();
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        this.location = block.getLocation();
        this.plugin = plugin;
    }

    public BlockSchedulerExecutor(Plugin plugin, Location location) {
        this.scheduler = Bukkit.getRegionScheduler();
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        this.location = location;
        this.plugin = plugin;
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        scheduler.execute(plugin, location, command);
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
            return () -> {};
        }

        ScheduledTask delayed = scheduler.runDelayed(plugin, location, scheduledTask -> command.run(), ticks);
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

        if (initialTicks == 0) {
            execute(() -> command.accept(null));
        }

        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command);
        scheduler.runAtFixedRate(plugin, location, scheduled -> {
            if (!task.isCancelled() && task.tick()) {
                command.accept(task);
            } else {
                task.cancel();
            }
        }, initialTicks, periodTicks);

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

        if (initialTicks == 0) {
            execute(() -> command.accept(null));
        }

        IteratedSdkScheduledTask task = new IteratedSdkScheduledTask(command, iterations);
        scheduler.runAtFixedRate(plugin, location, scheduled -> {
            if (!task.isCancelled() && task.tick()) {
                command.accept(task);
            } else {
                task.cancel();
            }
        }, initialTicks, periodTicks);

        return task;
    }
}
