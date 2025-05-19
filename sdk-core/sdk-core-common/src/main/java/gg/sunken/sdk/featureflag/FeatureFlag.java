package gg.sunken.sdk.featureflag;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;

/**
 * A simple system for storing the state of a feature, this is useful for limiting
 * access to a feature that may be in testing or development. Feature flags however
 * do not provide A/B testing or rollouts.
 * <p>
 * These feature flags are backed by a datastore, and therefore requires the appropriate
 * driver to be configured and connected before attempting to interact with  feature flags.
 *
 * @param <T> The type of the feature flag
 * @author santio
 */
@SuppressWarnings("WeakerAccess")
@Getter
@Accessors(fluent = true)
public abstract class FeatureFlag<T> {

    /**
     * The identifier of the feature flag, this is used to store and retrieve the value
     * from the database.
     */
    private final FeatureKey<T> key;

    /**
     * Whether the value has initially been fetched from the database.
     */
    @SuppressWarnings("RedundantFieldInitialization")
    @Setter
    private boolean cached = false;

    protected FeatureFlag(
            String name,
            Class<T> type
    ) {
        this.key = new FeatureKey<>(name, type);
    }

    /**
     * Get the value of the feature flag
     *
     * @return The value of the feature flag
     * @apiNote If the method has not been called before, it will attempt to
     * fetch the value from the database, which will block the current
     * thread until the value is fetched.
     */
    @Blocking
    public abstract T get();

    /**
     * Set the value of the feature flag
     *
     * @param value The value to set
     * @apiNote If the value is different, the backed value will be updated
     * in the database asynchronously and will not block the current
     * thread.
     */
    @NonBlocking
    public abstract void set(T value);

    /**
     * Write the value of the feature flag to the in-memory cache without
     * triggering any database writes.
     *
     * @param value The value to set
     */
    public void setEphemeral(T value) {
        this.set(value);
    }

}