package gg.sunken.sdk.scheduler;

public interface SchedulerAdapter {

    /**
     * Returns the synchronous executor for the current scheduler.
     * @return the synchronous executor
     */
    ScheduledExecutor sync();

    /**
     * Returns the synchronous executor for the current scheduler.
     * @return the synchronous executor
     */
    default ScheduledExecutor global() {
        return sync();
    }

    /**
     * Returns the asynchronous executor for the current scheduler.
     * @return the asynchronous executor
     */
    ScheduledExecutor async();

    /**
     * Returns the virtual executor for the current scheduler.
     * @return the virtual executor
     */
    ScheduledExecutor virtual();

    /**
     * Schedules a command to be executed on the main thread.
     * @param entity the entity to run the command on
     * @return a ScheduledExecutor for the entity
     */
    ScheduledExecutor entity(Object entity);

    /**
     * Schedules a command to be executed on the main thread.
     * @param block the block to run the command on
     * @return a ScheduledExecutor for the block
     */
    ScheduledExecutor block(Object block);
}
