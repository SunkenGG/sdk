package gg.sunken.sdk.featureflag.backed;

import gg.sunken.sdk.featureflag.BackedResult;
import gg.sunken.sdk.featureflag.FeatureFlag;
import gg.sunken.sdk.scheduler.SchedulerAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NonBlocking;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A generic feature flag that is backed by some database.
 *
 * @param <T> The type of the feature flag
 * @author santio
 */
@Accessors(fluent = true)
@Getter(AccessLevel.PROTECTED)
public abstract class BackedFeatureFlag<T> extends FeatureFlag<T> {

    private final SchedulerAdapter scheduler;
    private final FeatureFlag<T> feature;
    private Instant lastFetched = Instant.EPOCH;

    protected BackedFeatureFlag(
            SchedulerAdapter scheduler,
            FeatureFlag<T> feature
    ) {
        super(feature.key().name(), feature.key().type());
        this.feature = feature;
        this.scheduler = scheduler;
    }

    @Override
    public T get() {
        final @Nullable T value = this.feature().get();

        if (!this.feature().cached()) {
            // Synchronously fetch the value from the database
            final BackedResult<T> fetched = this.fetch();

            fetched.ifPresent(this.feature()::setEphemeral);
            this.feature().cached(true);
            this.markFresh();

            return fetched.orElse(value);
        } else if (this.isStale()) {
            // Asynchronously fetch the value from the database
            this.scheduler.async().execute(this::fetch);
        }

        return value;
    }

    @Override
    public void setEphemeral(T value) {
        this.feature().setEphemeral(value);
    }

    @Override
    public void set(T value) {
        this.feature().set(value);
        this.save();
    }

    /**
     * Fetch the value of the feature flag from the database
     *
     * @return The value of the feature flag
     */
    protected abstract BackedResult<T> fetch();

    /**
     * Saves the value of the feature flag to the database, this is called
     * asynchronously and will not block the current thread.
     */
    @NonBlocking
    protected abstract void save();

    /**
     * Mark the feature flag as fresh, this should be called by the implementation
     * when the backing datastore value is fetched.
     */
    protected final void markFresh() {
        this.lastFetched = Instant.now();
    }

    protected final boolean isStale() {
        final Instant now = Instant.now();
        return this.lastFetched.isBefore(
                now.minus(5, ChronoUnit.MINUTES)
        );
    }

}