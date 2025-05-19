package gg.sunken.sdk.features;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaperFeatureTicker implements Runnable {
    
    private final PaperFeatureManager manager;
    private long tick = 0;
    
    @Override
    public void run() {
        manager.call(input -> {
            tick++;
            input.tick(tick);
            if (tick == Long.MAX_VALUE) {
                tick = 0;
            }
        });
    }
    
}