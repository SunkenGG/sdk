package gg.sunken.sdk.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class TickUtil {

    private final static int TICK_RATE_DEFAULT = 50;

    public long toTicks(long time, TimeUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        if (time < 0) {
            throw new IllegalArgumentException("Time cannot be negative");
        }

        long ticks = unit.toMillis(time) / (1000 / TICK_RATE_DEFAULT);
        return ticks > 0 ? ticks : 1;
    }

    public long fromTicks(long ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("Ticks cannot be negative");
        }

        return ticks * (1000 / TICK_RATE_DEFAULT);
    }
}
