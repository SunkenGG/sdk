package gg.sunken.platform;

import com.google.auto.service.AutoService;
import gg.sunken.sdk.features.Feature;
import gg.sunken.sdk.features.FeatureInfo;
import gg.sunken.sdk.features.PaperFeature;
import org.jetbrains.annotations.NotNull;

@AutoService(PaperFeature.class)
@FeatureInfo("test")
public class TestFeature implements PaperFeature {
    
    @Override
    public void load() {
        PaperFeature.super.load();
    }
    
    @Override
    public void tick(long tick) {
        PaperFeature.super.tick(tick);
    }
    
    @Override
    public int compareTo(@NotNull Feature o) {
        return 0;
    }
    
    @Override
    public void enable() {
        System.out.println("Test Feature Enabled");
        PaperFeature.super.enable();
    }
    
    @Override
    public void disable() {
        System.out.println("Test Feature Disabled");
        PaperFeature.super.disable();
    }
}
