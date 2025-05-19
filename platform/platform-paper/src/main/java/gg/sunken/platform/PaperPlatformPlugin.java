package gg.sunken.platform;

import com.google.inject.Inject;
import com.google.inject.Injector;
import gg.sunken.sdk.features.PaperFeatureManager;
import gg.sunken.sdk.plugin.SdkPaperPlugin;
import gg.sunken.sdk.scheduler.SchedulerAdapter;

/**
 * This is the main plugin class for the Paper platform.
 * It extends SdkPaperPlugin to leverage the SDK's functionality.
 */
public class PaperPlatformPlugin extends SdkPaperPlugin {
    
    @Inject
    public PaperPlatformPlugin(
        PaperFeatureManager featureManager,
        SchedulerAdapter schedulerAdapter,
        Injector injector
    ) {
        super(featureManager, schedulerAdapter, injector);
    }
    
    @Override
    public void enable() {

    }
}
