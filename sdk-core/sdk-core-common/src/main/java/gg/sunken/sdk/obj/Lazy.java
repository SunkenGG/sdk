package gg.sunken.sdk.obj;

public class Lazy<T> {
    private final LazySupplier<T> supplier;
    private T value;

    public Lazy(LazySupplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(LazySupplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }

    @FunctionalInterface
    public interface LazySupplier<T> {
        T get();
    }
}
