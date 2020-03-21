package org.example.webframework.lesson3;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class Request {
    public static final String HTTP_VERSION_1_0 = "HTTP/1.0";
    public static final String HTTP_VERSION_1_1 = "HTTP/1.1";

    private final HttpConnection connection;
    private String version;
    private HttpMethod method;
    private String scheme;
    private String pathAndQuery;
    private URL url;
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private String contentType;
    private String contentCharset = "utf-8";

    public Request(HttpConnection connection) {
        this.connection = connection;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setPathAndQuery(String pathAndQuery) {
        this.pathAndQuery = pathAndQuery;
        this.url = null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String field, String value) {
        if (!headers.containsKey(field)) {
            headers.put(field, value);
        } else {
            headers.put(field, headers.get(value) + ", " + value);
        }
    }

    public String getScheme() {
        if (scheme != null) {
            return scheme;
        }

        scheme = getHeaderValue("X-Forwarded-Proto");

        if (scheme != null) {
            return scheme;
        }

        scheme = "http";
        return scheme;
    }

    public URL getUrl() throws MalformedURLException {
        if (url != null) {
            return url;
        }

        url = new URL(getScheme(), getHost(), pathAndQuery);
        return url;
    }

    public String getHeaderValue(String field) {
        return headers.get(field);
    }

    public boolean hasHeader(String field) {
        return headers.containsKey(field);
    }

    public boolean hasHeader(String field, String value) {
        return value.equals(getHeaderValue(field));
    }

    public boolean isKeepAliveConnection() {
        return (HTTP_VERSION_1_0.equals(version) && hasHeader("Connection", "keep-alive"))
                || (HTTP_VERSION_1_1.equals(version) && !hasHeader("Connection", "close"));
    }

    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }

        final var value = getHeaderValue("Content-Type");

        if (value == null) {
            contentType = "";
            return contentType;
        }

        final var parts = value.split(";");
        contentType = parts[0].trim();

        if (parts.length > 1) {
            for (final var part : parts) {
                final var trimmedPart = part.trim();
                final var i = trimmedPart.indexOf("=");
                final var partName = i >= 0 ? trimmedPart.substring(0, i) : trimmedPart;
                final var partValue = i >= 0 ? trimmedPart.substring(i + 1) : "";

                if ("charset".equals(partName)) {
                    contentCharset = partValue;
                }
            }
        }

        return contentType;
    }

    public String getContentCharset() {
        if (contentType == null) {
            getContentType();
        }

        return contentCharset;
    }

    public long getContentLength() {
        final var length = getHeaderValue("Content-Length");

        if (length == null) {
            return 0;
        }

        return Long.parseLong(length);
    }

    public boolean isMultipart() {
        final var type = getContentType();

        if (type == null) {
            return false;
        }

        return type.startsWith("multipart/");
    }

    public String getHost() {
        return getHeaderValue("Host");
    }

    public CompletableFuture<ByteBuffer> readBody() {
        return connection.readBody(getContentLength());
    }

    public CompletableFuture<ByteBuffer> readBody(int length) {
        return connection.readBody(length);
    }
}
