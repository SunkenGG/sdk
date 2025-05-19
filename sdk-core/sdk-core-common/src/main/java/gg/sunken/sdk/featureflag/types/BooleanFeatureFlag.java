package gg.sunken.sdk.featureflag.types;

import gg.sunken.sdk.featureflag.FeatureFlag;
import com.mongodb.lang.NonNull;

/**
 * A boolean feature flag, that holds a simple true/false value.
 *
 * @author santio
 */
public final class BooleanFeatureFlag extends FeatureFlag<Boolean> {

    private @NonNull Boolean value = false;

    @SuppressWarnings("MissingJavadoc")
    public BooleanFeatureFlag(String name) {
        super(name, Boolean.class);
    }

    @Override
    public @NonNull Boolean get() {
        return this.value;
    }

    @Override
    public void set(@NonNull Boolean value) {
        this.value = value;
    }

}