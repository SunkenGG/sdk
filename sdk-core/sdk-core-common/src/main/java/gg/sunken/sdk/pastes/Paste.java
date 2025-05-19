package gg.sunken.sdk.pastes;

import java.util.concurrent.CompletableFuture;

/**
 * Exposes an asynchronous way to create pasts to a various paste services.
 * @author santio
 * @see Hastebin
 * @see PastesDev
 * @see MCLogs
 */
public interface Paste {
    
    /**
     * Creates a new paste with the given content.
     * @param content The content to paste.
     * @return The URL of the paste.
     */
    CompletableFuture<String> paste(String content);
    
}
