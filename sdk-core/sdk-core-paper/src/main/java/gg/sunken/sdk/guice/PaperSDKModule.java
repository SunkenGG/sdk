package gg.sunken.sdk.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import gg.sunken.sdk.scheduler.PaperSchedulerAdapter;
import gg.sunken.sdk.scheduler.SchedulerAdapter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

/**
 * Handles the default bindings for the SDK.
 *
 * @author santio
 */
@RequiredArgsConstructor
public class PaperSDKModule extends AbstractModule {

    private final Class<? extends Plugin> plugin;

    @Override
    protected void configure() {
        this.bind(Plugin.class).to(this.plugin).in(Scopes.SINGLETON);
        this.bind(SchedulerAdapter.class).to(PaperSchedulerAdapter.class).in(Scopes.SINGLETON);
    }

}