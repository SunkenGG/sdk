package gg.sunken.sdk.io.response;

import lombok.experimental.Delegate;

/**
 * Represents a JSON response from a request.
 * @param response The response.
 * @param body The parsed body of the response.
 * @param <T> The type of the body.
 */
public record JSONResponse<T>(
    @Delegate
    Response response,
    boolean deserialized,
    T body
) {
    
    /**
     * @return {@code true} if the response was successful and the body was
     *         successfully deserialized.
     */
    public boolean isValid() {
        return this.success() && deserialized;
    }
    
}
