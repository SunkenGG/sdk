package gg.sunken.sdk.pastes;

import gg.sunken.sdk.io.ContentType;
import gg.sunken.sdk.io.MethodType;
import gg.sunken.sdk.io.Request;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
public class PastesDev implements Paste {
    
    private static final String PASTES_DEV_URL = "https://api.pastes.dev/post";
    private final Request.Factory request;
    
    @Inject
    public PastesDev(
        Request.Factory request
    ) {
        this.request = request;
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    public CompletableFuture<String> paste(String content) {
        return this.request.create(PASTES_DEV_URL, MethodType.POST)
            .contentType(ContentType.TEXT_PLAIN)
            .body(content)
            .json(PasteResponse.class)
            .thenApply(response -> {
                if (!response.isValid()) {
                    log.error("Failed to parse response from pastes.dev: {}", response.asString());
                    return "unknown";
                }
                
                return "https://pastes.dev/" + response.body().key();
            });
    }
    
    private record PasteResponse(
        String key
    ) {}

}
