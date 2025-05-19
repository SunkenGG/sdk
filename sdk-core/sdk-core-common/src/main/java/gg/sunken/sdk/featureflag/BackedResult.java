package gg.sunken.sdk.featureflag;

import lombok.ToString;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A result that may contain no data, or a single (possibly null) value. This is extremely
 * similar to {@link java.util.Optional}, but instead allowing for nullable values.
 *
 * @param <T> The type of the value
 * @author santio
 */
@ApiStatus.Internal
@ToString
public final class BackedResult<T> {

    private final boolean hasValue;
    private final @Nullable T value;

    private BackedResult(@Nullable T value) {
        this.value = value;
        this.hasValue = true;
    }

    private BackedResult() {
        this.value = null;
        this.hasValue = false;
    }

    /**
     * Creates a new result with the given value.
     *
     * @param value The value to store
     * @param <T>   The type of the value
     * @return The result
     */
    public static <T> BackedResult<T> of(@Nullable T value) {
        return new BackedResult<>(value);
    }

    /**
     * Creates a new empty result.
     *
     * @param <T> The type of the value
     * @return The result
     */
    public static <T> BackedResult<T> empty() {
        return new BackedResult<>();
    }

    /**
     * @return True if the result has a value, false otherwise
     */
    public boolean isPresent() {
        return this.hasValue;
    }

    /**
     * @return True if the result has no value, false otherwise
     */
    public boolean isEmpty() {
        return !this.hasValue;
    }

    /**
     * Returns the value of the result, if present, otherwise throws an exception.
     *
     * @return The value of the result
     * @throws IllegalStateException If the result is empty
     */
    public T get() {
        if (!this.hasValue) {
            throw new IllegalStateException("No value present");
        }

        return this.value;
    }

    /**
     * Returns the value of the result, if present, otherwise returns the given default value.
     *
     * @param defaultValue The default value to return if the result is empty
     * @return The value of the result
     */
    public T orElse(T defaultValue) {
        if (!this.hasValue) {
            return defaultValue;
        }

        return this.value;
    }

    /**
     * If the result is present, performs the given action with the value, otherwise does nothing.
     *
     * @param consumer The action to perform
     */
    public void ifPresent(Consumer<? super T> consumer) {
        if (this.hasValue) {
            consumer.accept(this.value);
        }
    }

}