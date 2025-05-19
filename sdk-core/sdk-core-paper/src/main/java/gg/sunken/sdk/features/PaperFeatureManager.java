package gg.sunken.sdk.features;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("BoundedWildcard")
@Singleton
public class PaperFeatureManager extends FeatureManager<PaperFeature> {
    private final Plugin plugin;
    
    @Inject
    public PaperFeatureManager(Plugin plugin, Injector injector) {
        super(PaperFeature.class, injector);
        this.plugin = plugin;
    }
    
    @Override
    boolean isDependencyAvailable(String identifier) {
        return Bukkit.getPluginManager().isPluginEnabled(identifier);
    }
    
    @Override
    public void enable(RegisteredFeature<PaperFeature> feature) {
        super.enable(feature);
        Bukkit.getPluginManager().registerEvents(feature.feature(), plugin);
    }
    
    @Override
    public void disable(RegisteredFeature<PaperFeature> feature) {
        super.disable(feature);
        HandlerList.unregisterAll(feature.feature());
    }
}