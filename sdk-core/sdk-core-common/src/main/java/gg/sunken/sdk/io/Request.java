package gg.sunken.sdk.io;

import gg.sunken.sdk.io.response.JSONResponse;
import gg.sunken.sdk.io.response.Response;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A lightweight library for sending RESTful requests.
 * @author santio
 */
@SuppressWarnings("ParameterHidesMemberVariable")
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class Request {
    
    private static final String userAgent = "WildWood/sdk (Automated Request)";
    
    private final HttpClient client;
    private final Gson gson;
    private final URI uri;
    private final MethodType method;
    
    private ContentType contentType = ContentType.APPLICATION_JSON;
    private HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.noBody();
    
    @Inject
    public Request(
        HttpClient client,
        Gson gson,
        @Assisted String uri,
        @Assisted MethodType method
    ) {
        this.client = client;
        this.gson = gson;
        this.uri = URI.create(uri);
        this.method = method;
    }
    
    /**
     * Set the body of the request to the given string. This will <b>not</b>
     * set the content type.
     *
     * @param body The body of the request.
     * @return This request.
     */
    public Request body(String body) {
        this.body = HttpRequest.BodyPublishers.ofString(body);
        return this;
    }
    
    /**
     * Set the body of the request to the given byte array. This will <b>not</b>
     * set the content type.
     *
     * @param body The body of the request.
     * @return This request.
     */
    public Request body(byte[] body) {
        this.body = HttpRequest.BodyPublishers.ofByteArray(body);
        return this;
    }
    
    /**
     * Set the body of the request to the given object. This uses the {@link Gson}
     * instance provided by the injector to serialize the object to JSON.
     *
     * <p>
     * This will set the content type to {@link ContentType#APPLICATION_JSON}.
     *
     * @param body The body of the request.
     * @return This request.
     * @param <T> The type of the body.
     */
    public <T> Request body(T body) {
        this.body = HttpRequest.BodyPublishers.ofString(gson.toJson(body));
        return this;
    }
    
    /**
     * Set the body of the request to the given map. If the content-type happens to
     * already be set to {@link ContentType#APPLICATION_FORM_URLENCODED}, then the
     * map will be encoded as a query string. Otherwise, the map will be serialized
     * to JSON and set as the body with the content type set to
     * {@link ContentType#APPLICATION_JSON}.
     *
     * @param body The body of the request.
     * @return This request.
     */
    public Request body(Map<String, Object> body) {
        if (this.contentType.is(ContentType.APPLICATION_FORM_URLENCODED)) {
            return this.body(body.entrySet().stream()
                .map(entry -> {
                    final String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    final String value = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
                    
                    return key + "=" + value;
                })
                .collect(Collectors.joining("&")));
        }
        
        return this.body(gson.toJson(body));
    }
    
    /**
     * Send a request to the remote resource with a given body handler on how to handle the response.
     * In most cases, you should use {@link #json(Class)} instead for JSON responses.
     * @return A {@link CompletableFuture} that will be completed with the response.
     */
    @SuppressWarnings("WeakerAccess")
    public CompletableFuture<Response> send() {
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(this.uri)
            .method(this.method.name(), this.body)
            .header("User-Agent", userAgent)
            .header("Content-Type", this.contentType.value())
            .header("Accept", "application/json")
            .build();
        
        return this.client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
            .thenApply(response -> {
                final int status = response.statusCode();
                final byte[] raw = response.body();
                final String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse(null);
                
                return new Response(
                    status,
                    raw,
                    contentType
                );
            });
    }
    
    /**
     * Send a request to the remote resource and parse the response as JSON.
     * This will use the {@link Gson} instance provided by the injector.
     *
     * @param clazz The class to parse the response as.
     * @return A {@link CompletableFuture} that will be completed with the parsed response, or null
     *         if the response could not be parsed.
     * @param <T> The type of the response.
     */
    public <T> CompletableFuture<JSONResponse<T>> json(Class<? extends T> clazz) {
        return this.send()
            .thenApply(response -> {
                @Nullable T value = null;
                
                try {
                    value = this.gson.fromJson(new String(response.raw(), StandardCharsets.UTF_8), clazz);
                } catch (JsonSyntaxException ignored) {}
                
                return new JSONResponse<>(
                    response,
                    true,
                    value
                );
            });
    }
    
    @SuppressWarnings({"WeakerAccess", "InterfaceMayBeAnnotatedFunctional", "MissingJavadoc", "PublicInnerClass"})
    public interface Factory {
        /**
         * Creates a new {@link Request} which is used to send requests to remote resources.
         * @param uri The URI of the remote resource.
         * @param method The HTTP method to use.
         * @return A new {@link Request} instance.
         * @throws IllegalArgumentException If the URI provided is invalid.
         */
        Request create(@Assisted String uri, @Assisted MethodType method);
    }
    
}
