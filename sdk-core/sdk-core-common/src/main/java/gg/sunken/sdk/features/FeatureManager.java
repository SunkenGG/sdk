package gg.sunken.sdk.features;

import com.google.inject.Injector;
import gg.sunken.sdk.guice.GuiceServiceLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a manager of features, this needs to be constructed by the host
 * of the features
 *
 * @author santio
 * @param <F> The type of feature being managed
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Accessors(fluent = true)
public abstract class FeatureManager<F extends Feature> {
    
    private final Class<F> clazz;
    private final Injector injector;
    
    @Getter(AccessLevel.NONE)
    private final Set<RegisteredFeature<F>> features = new HashSet<>();
    
    abstract boolean isDependencyAvailable(String identifier);
    
    /**
     * Initialize and load all features
     */
    public void loadAll() {
        this.features.clear();
        GuiceServiceLoader.load(clazz, this.getClass().getClassLoader())
            .forEach(this::registerFeature);
    }
    
    /**
     * Get a list of all features that are known of
     * @return The list of registered features
     */
    public Set<RegisteredFeature<F>> getFeatures() {
        return new HashSet<>(features);
    }
    
    /**
     * Get the list of all enabled features
     * @return The list of registered features that are enabled
     */
    public Set<RegisteredFeature<F>> getEnabledFeatures() {
        return this.getFeatures().stream()
            .filter(RegisteredFeature::enabled)
            .collect(Collectors.toSet());
    }
    
    /**
     * Enable a currently disabled feature
     * @param feature The registered feature to enable
     */
    public void enable(RegisteredFeature<F> feature) {
        if (feature.enabled()) return;
        feature.enabled(true);
        feature.feature().enable();
    }
    
    /**
     * Disable a currently enabled feature
     * @param feature The registered feature to disable
     */
    public void disable(RegisteredFeature<F> feature) {
        if (!feature.enabled()) return;
        feature.enabled(false);
        feature.feature().disable();
    }
    
    /**
     * Call a method on all enabled features
     * @param method The method to call on the features
     */
    public void call(Callable<? super F> method) {
        this.features.stream().filter(RegisteredFeature::enabled).forEach((registeredFeature) -> {
            try {
                method.call(registeredFeature.feature());
            } catch (Exception e) {
                log.error("An error occurred while calling method on {}", registeredFeature.info().value(), e);
            }
        });
    }
    
    /**
     * Get a feature by its unique identifier
     * @param identifier The feature name/identifier
     * @return The registered feature, or null if not known of
     */
    public @Nullable RegisteredFeature<F> get(String identifier) {
        return this.features.stream()
            .filter(feature -> feature.info().value().equalsIgnoreCase(identifier))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Check if the specified feature by name is enabled
     * @param identifier The feature name/identifier
     * @return Whether the feature is enabled or not
     */
    public boolean enabled(String identifier) {
        final RegisteredFeature<F> feature = this.get(identifier);
        if (feature == null) {
            log.warn("Attempted to check if feature '{}' is enabled, however it doesn't exist", identifier);
            return false;
        }
        
        return feature.enabled();
    }
    
    private void registerFeature(Class<? extends F> featureClass) {
        final FeatureInfo annotation = featureClass.getAnnotation(FeatureInfo.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Feature class " + featureClass.getName() + " is missing the @FeatureInfo annotation");
        }
        
        final F feature = this.injector.getInstance(featureClass);
        final RegisteredFeature<F> registeredFeature = new RegisteredFeature<>(
            feature,
            annotation,
            true
        );
        
        final String[] dependencies = annotation.dependencies();
        for (String dependency : dependencies) {
            if (!this.isDependencyAvailable(dependency)) {
                log.error("Feature {} requires a dependency on {}, however it is not present", annotation.value(), dependency);
                registeredFeature.enabled(false);
            }
        }
        
        this.features.add(registeredFeature);
    }
    
    @SuppressWarnings({"ProhibitedExceptionDeclared", "PublicInnerClass", "MissingJavadoc"})
    @FunctionalInterface
    public interface Callable<T> {
        void call(T input) throws Exception;
    }
    
}