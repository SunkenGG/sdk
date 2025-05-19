package gg.sunken.sdk.featureflag;

/**
 * A key for a feature, used to uniquely identify a feature.
 *
 * @param <T> The type of the feature
 * @author santio
 */
public record FeatureKey<T>(
        String name,
        Class<T> type
) {
}