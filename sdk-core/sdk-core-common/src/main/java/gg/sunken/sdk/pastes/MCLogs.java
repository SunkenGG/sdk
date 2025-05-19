package gg.sunken.sdk.pastes;

import gg.sunken.sdk.io.MethodType;
import gg.sunken.sdk.io.Request;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
public class MCLogs implements Paste {
    
    private static final String MCLOGS_URL = "https://api.mclo.gs/1/log";
    private final Request.Factory request;
    
    @Inject
    public MCLogs(
        Request.Factory request
    ) {
        this.request = request;
    }
    
    @SuppressWarnings("FeatureEnvy")
    @Override
    public CompletableFuture<String> paste(String content) {
        return this.request.create(MCLOGS_URL, MethodType.POST)
//            .contentType(ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8))
            .body(Map.of("content", content))
            .json(PasteResponse.class)
            .thenApply(response -> {
                if (!response.isValid()) {
                    log.error("Failed to parse response from MCLogs: {}", response.asString());
                    return "unknown";
                }
                
                return response.body().url();
            });
    }
    
    private record PasteResponse(
        boolean success,
        @Nullable String url
    ) {}
    
}
