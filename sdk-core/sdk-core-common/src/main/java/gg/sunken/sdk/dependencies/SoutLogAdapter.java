package gg.sunken.sdk.dependencies;

import com.alessiodp.libby.logging.LogLevel;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoutLogAdapter implements LogAdapter {
    @Override
    public void log(@NotNull LogLevel logLevel, @Nullable String s) {
        if (s == null) {
            return;
        }
        switch (logLevel) {
            case DEBUG:
                System.out.println("[DEBUG] " + s);
                break;
            case INFO:
                System.out.println("[INFO] " + s);
                break;
            case WARN:
                System.out.println("[WARN] " + s);
                break;
            case ERROR:
                System.err.println("[ERROR] " + s);
                break;
            default:
                System.out.println(s);
        }
    }

    @Override
    public void log(@NotNull LogLevel logLevel, @Nullable String s, @Nullable Throwable throwable) {
        if (s == null) {
            return;
        }
        switch (logLevel) {
            case DEBUG:
                System.out.println("[DEBUG] " + s);
                break;
            case INFO:
                System.out.println("[INFO] " + s);
                break;
            case WARN:
                System.out.println("[WARN] " + s);
                break;
            case ERROR:
                System.err.println("[ERROR] " + s);
                break;
            default:
                System.out.println(s);
        }
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}
