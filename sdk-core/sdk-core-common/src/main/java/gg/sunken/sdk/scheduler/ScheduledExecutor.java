package gg.sunken.sdk.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface ScheduledExecutor {

    /**
     * Executes a command immediately on the executor.
     * @param command the command to execute
     */
    void execute(Runnable command);

    /**
     * Schedules a command to be executed immediately.
     * @param command the command to execute
     */
    default void schedule(Runnable command) {
        execute(command);
    }

    /**
     * Schedules a command to be executed after a delay.
     * @param command the command to execute
     * @param delay the delay before execution
     * @param unit the time unit of the delay
     * @return a ScheduledTask representing the scheduled command
     */
    SdkScheduledTask schedule(Runnable command, long delay, TimeUnit unit);

    /**
     * Schedules a command to be executed after a delay, with a consumer to handle the ScheduledTask.
     * @param command the command to execute
     * @param initialDelay the initial delay before execution
     * @param period the period between successive executions
     * @param unit the time unit of the initial delay and period
     * @return a ScheduledTask representing the scheduled command
     */
    SdkScheduledTask scheduleAtFixedRate(Consumer<SdkScheduledTask> command, long initialDelay, long period, TimeUnit unit);

    /**
     * Schedules a command to be executed repeatedly at a fixed rate.
     * @param command the command to execute
     * @param period the period between successive executions
     * @param unit the time unit of the period
     * @return a ScheduledTask representing the scheduled command
     */
    default SdkScheduledTask scheduleRepeated(Consumer<SdkScheduledTask> command, long period, TimeUnit unit) {
        return scheduleAtFixedRate(command, 0, period, unit);
    }

    /**
     * Schedules a command to be executed repeatedly for a specified number of iterations.
     * @param command the command to execute
     * @param iterations the number of times to execute the command
     * @param initialDelay the initial delay before the first execution
     * @param period the period between successive executions
     * @param unit the time unit of the initial delay and period
     * @return a ScheduledTask representing the scheduled command
     */
    SdkScheduledTask scheduleIterated(Consumer<SdkScheduledTask> command, long iterations, long initialDelay, long period, TimeUnit unit);
}
