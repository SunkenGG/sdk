package gg.sunken.sdk.featureflag;

import gg.sunken.sdk.featureflag.backed.BackedFeatureFlag;

/**
 * A generic interface for feature stores, implementations of this interface allow for
 * creating differently backed feature flags.
 *
 * @author santio
 * @see MongoFeatureStore
 */
public interface FeatureStore {

    /**
     * Create a new feature flag that is backed by a boolean value. This flag acts
     * as a toggle, and can be used to enable/disable features.
     *
     * @param name The name of the feature flag
     * @return The feature flag
     */
    BackedFeatureFlag<Boolean> toggle(String name);

    /**
     * Create a new feature flag that is backed by an enumerated value. This flag acts as a switch where how a feature
     * works or the behavior of a feature can be changed.
     *
     * @param name      The name of the feature flag
     * @param enumClass The enum class that the feature flag will hold
     * @param <T>       The inner type of the enum class
     * @return The feature flag
     */
    <T extends Enum<T>> BackedFeatureFlag<T> enumerated(String name, Class<T> enumClass);

}