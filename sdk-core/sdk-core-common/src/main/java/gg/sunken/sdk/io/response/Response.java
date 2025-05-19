package gg.sunken.sdk.io.response;

import java.nio.charset.StandardCharsets;

/**
 * Represents a JSON response from a request.
 * @param status The status code of the response.
 * @param raw The raw response body.
 * @param contentType The content type of the response.
 */
@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
public record Response(
    int status,
    byte[] raw,
    String contentType
) {
    
    /**
     * Checks if the response was successful, this checks if the status code
     * is a 2xx code.
     *
     * @return {@code true} if the response was successful.
     */
    public boolean success() {
        return status % 100 == 2;
    }
    
    /**
     * @return The response as a UTF-8 string.
     */
    public String asString() {
        return new String(this.raw, StandardCharsets.UTF_8);
    }
    
}
