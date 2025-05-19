package gg.sunken.sdk.scheduler;

public abstract class AbstractSchedulerAdapter implements SchedulerAdapter {

    protected VirtualScheduledExecutor virtualExecutor = new VirtualScheduledExecutor();
    protected AsyncScheduledExecutor asyncExecutor = new AsyncScheduledExecutor();

    @Override
    public ScheduledExecutor virtual() {
        return virtualExecutor;
    }

    @Override
    public ScheduledExecutor async() {
        return asyncExecutor;
    }
}
