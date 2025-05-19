package gg.sunken.sdk.features;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class RegisteredFeature<F extends Feature> {
    
    private final F feature;
    private final FeatureInfo info;
    @Setter(AccessLevel.PACKAGE) private boolean enabled;
    
}