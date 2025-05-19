package gg.sunken.sdk.io;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.charset.Charset;

/**
 * A class that represents a content type, holds common content types and allows for
 * custom content types to be created.
 * @author santio
 */
@SuppressWarnings({"unused", "MissingJavadoc"})
@Getter
@Accessors(fluent = true)
public class ContentType {
    
    public static final ContentType APPLICATION_JSON = new ContentType("application/json");
    public static final ContentType APPLICATION_OCTET_STREAM = new ContentType("application/octet-stream");
    public static final ContentType TEXT_PLAIN = new ContentType("text/plain");
    public static final ContentType TEXT_HTML = new ContentType("text/html");
    public static final ContentType TEXT_CSS = new ContentType("text/css");
    public static final ContentType TEXT_JAVASCRIPT = new ContentType("text/javascript");
    public static final ContentType IMAGE_JPEG = new ContentType("image/jpeg");
    public static final ContentType IMAGE_PNG = new ContentType("image/png");
    public static final ContentType IMAGE_GIF = new ContentType("image/gif");
    public static final ContentType IMAGE_SVG = new ContentType("image/svg+xml");
    public static final ContentType IMAGE_WEBP = new ContentType("image/webp");
    public static final ContentType MULTIPART_FORM_DATA = new ContentType("multipart/form-data");
    public static final ContentType APPLICATION_XML = new ContentType("application/xml");
    public static final ContentType APPLICATION_XHTML_XML = new ContentType("application/xhtml+xml");
    public static final ContentType APPLICATION_ATOM_XML = new ContentType("application/atom+xml");
    public static final ContentType APPLICATION_RSS_XML = new ContentType("application/rss+xml");
    public static final ContentType APPLICATION_FORM_URLENCODED = new ContentType("application/x-www-form-urlencoded");
    
    private final String value;
    
    public ContentType(String value) {
        this.value = value;
    }
    
    /**
     * Sets the charset of the content type.
     * @param charset The charset to set.
     * @return A new content type with the charset set.
     */
    public ContentType withCharset(Charset charset) {
        return new ContentType(this.value + "; charset=" + charset.name());
    }
    
    public String contentType() {
        return this.value.split(";")[0];
    }
    
    public @Nullable String charset() {
        final String[] parts = this.value.split(";");
        if (parts.length == 1) {
            return null;
        }
        
        return parts[1].split("=")[1];
    }
    
    /**
     * Checks if this content type is equal to the given content type.
     * If the given content type doesn't have a charset, then the charset of
     * this content type will be ignored during the comparison.
     *
     * @param other The content type to compare to.
     * @return {@code true} if this content type is equal to the given content type.
     */
    public boolean is(ContentType other) {
        if (other.value() != null) {
            return this.value.equals(other.value());
        } else {
            return this.contentType().equals(other.contentType());
        }
    }
    
    @Override
    public String toString() {
        return this.value;
    }

}
