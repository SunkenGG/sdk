package gg.sunken.sdk.featureflag;

import gg.sunken.sdk.featureflag.backed.MongoBackedFeatureFlag;
import gg.sunken.sdk.featureflag.types.BooleanFeatureFlag;
import gg.sunken.sdk.featureflag.types.EnumFeatureFlag;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.client.MongoDatabase;
import gg.sunken.sdk.scheduler.SchedulerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An injectable holder for all the feature flags.
 *
 * @author santio
 */
public class MongoFeatureStore implements FeatureStore {

    private final Map<FeatureKey<?>, MongoBackedFeatureFlag<?>> featureFlags = new ConcurrentHashMap<>();

    private final SchedulerAdapter scheduler;
    private final MongoDatabase database;

    @SuppressWarnings("MissingJavadoc")
    @Inject
    public MongoFeatureStore(
            SchedulerAdapter scheduler,
            @Assisted MongoDatabase database
    ) {
        this.scheduler = scheduler;
        this.database = database;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MongoBackedFeatureFlag<Boolean> toggle(String name) {
        final FeatureKey<Boolean> key = new FeatureKey<>(name, Boolean.class);
        if (this.featureFlags.containsKey(key)) {
            return (MongoBackedFeatureFlag<Boolean>) this.featureFlags.get(key);
        }

        final BooleanFeatureFlag featureFlag = new BooleanFeatureFlag(name);
        final MongoBackedFeatureFlag<Boolean> backedFeatureFlag = new MongoBackedFeatureFlag<>(
                this.scheduler,
                database,
                featureFlag
        );

        this.featureFlags.put(featureFlag.key(), backedFeatureFlag);
        return backedFeatureFlag;
    }

    @Override
    public <T extends Enum<T>> MongoBackedFeatureFlag<T> enumerated(String name, Class<T> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Enum class must be an enum");
        }

        final EnumFeatureFlag<T> featureFlag = new EnumFeatureFlag<>(name, enumClass);
        final MongoBackedFeatureFlag<T> backedFeatureFlag = new MongoBackedFeatureFlag<>(
                this.scheduler,
                database,
                featureFlag
        );

        this.featureFlags.put(featureFlag.key(), backedFeatureFlag);
        return backedFeatureFlag;
    }

    public interface Factory {
        MongoFeatureStore create(MongoDatabase database);
    }
}