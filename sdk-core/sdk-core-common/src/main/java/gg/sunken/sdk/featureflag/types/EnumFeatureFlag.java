package gg.sunken.sdk.featureflag.types;

import gg.sunken.sdk.featureflag.FeatureFlag;

/**
 * A feature flag that can hold a various list of values, either an enum or a dynamic list.
 * This is useful for quickly switching between different configurations.
 *
 * @param <T> The type of the feature flag, either an enum or a dynamic list
 * @author santio
 */
public class EnumFeatureFlag<T extends Enum<T>> extends FeatureFlag<T> {

    private T value;

    @SuppressWarnings("MissingJavadoc")
    public EnumFeatureFlag(String name, Class<T> enumClass) {
        super(name, enumClass);

        final T[] values = enumClass.getEnumConstants();
        if (values.length == 0) {
            throw new IllegalArgumentException("Enum class must have at least one value to be used as a feature flag");
        }

        this.value = values[0];
    }

    @Override
    public T get() {
        return this.value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

}