package gg.sunken.sdk.featureflag.backed;

import gg.sunken.sdk.featureflag.BackedResult;
import gg.sunken.sdk.featureflag.FeatureFlag;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import gg.sunken.sdk.scheduler.SchedulerAdapter;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bson.Document;

import java.util.Arrays;

/**
 * A generic feature flag that is backed by a Mongo database.
 *
 * @param <T> The type of the feature flag
 * @author santio
 */
@Getter
@Accessors(fluent = true)
public final class MongoBackedFeatureFlag<T> extends BackedFeatureFlag<T> {

    private static final ReplaceOptions UPSERT = new ReplaceOptions().upsert(true);

    private final SchedulerAdapter scheduler;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    @SuppressWarnings("MissingJavadoc")
    public MongoBackedFeatureFlag(
            SchedulerAdapter scheduler,
            MongoDatabase database,
            FeatureFlag<T> featureFlag
    ) {
        super(scheduler, featureFlag);

        this.scheduler = scheduler;
        this.database = database;
        this.collection = database.getCollection("feature_flags");
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    protected BackedResult<T> fetch() {
        final Document document = this.collection.find(
                Filters.and(
                        Filters.eq("name", this.feature().key().name()),
                        Filters.eq("type", this.feature().key().type().getName())
                )
        ).first();

        this.markFresh();
        if (document == null) {
            return BackedResult.empty();
        }

        if (this.feature().key().type().isEnum()) {
            final T[] enumClazz = this.feature().key().type().getEnumConstants();
            final T value = Arrays.stream(enumClazz)
                    .filter(v -> ((Enum<?>) v).name().equals(document.get("value", String.class)))
                    .findFirst()
                    .orElse(null);

            return BackedResult.of(value);
        }

        return BackedResult.of(document.get("value", this.feature().key().type()));
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    protected void save() {
        this.scheduler.async().execute(() -> {
            final Document document = new Document()
                    .append("name", this.feature().key().name())
                    .append("type", this.feature().key().type().getName())
                    .append("value", this.feature().get());

            this.collection.replaceOne(
                    Filters.and(
                            Filters.eq("name", this.feature().key().name()),
                            Filters.eq("type", this.feature().key().type().getName())
                    ),
                    document,
                    UPSERT
            );
        });
    }
}