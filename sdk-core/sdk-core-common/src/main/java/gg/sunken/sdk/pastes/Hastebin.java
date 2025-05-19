package gg.sunken.sdk.pastes;

import gg.sunken.sdk.io.MethodType;
import gg.sunken.sdk.io.Request;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
public class Hastebin implements Paste {

    private static final String HASTEBIN_URL = "https://hastebin.com/documents";
    private final Request.Factory request;
    
    @Inject
    public Hastebin(
        Request.Factory request
    ) {
        this.request = request;
    }
    
    @Override
    public CompletableFuture<String> paste(String content) {
        return this.request.create(HASTEBIN_URL, MethodType.POST)
            .body(content)
            .json(PasteResponse.class)
            .thenApply(response -> {
                if (!response.isValid()) {
                    log.error("Failed to parse response from Hastebin");
                    return "unknown";
                }
                
                return "https://hastebin.com/" + response.body().key();
            });
    }
    
    private record PasteResponse(String key) {}

}
