package org.example.webframework.lesson2;

import java.nio.ByteBuffer;
import java.util.*;

public class Response {
    private static final Map<Integer, String> statusMap = new HashMap<>();

    public static final ByteBuffer END_BUFFER = ByteBuffer.allocate(0);

    static {
        statusMap.put(200, "OK");
    }

    private final HttpConnection connection;
    private int statusCode;
    private final List<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
    private boolean isHeaderWritten;
    private boolean isChunked;
    private boolean end;

    public Response(HttpConnection connection) {
        this.connection = connection;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Response setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public List<AbstractMap.SimpleEntry<String, String>> getHeaders() {
        return headers;
    }

    public Response addHeader(String field, String value) {
        headers.add(new AbstractMap.SimpleEntry<>(field, value));
        return this;
    }

    public Response setHeader(String field, String value) {
        headers.stream()
                .filter(e -> e.getKey().equals(field))
                .findFirst()
                .ifPresentOrElse(e -> e.setValue(value),
                        () -> addHeader(field, value));

        return this;
    }

    public String getStatusText() {
        return statusMap.get(statusCode);
    }

    public Response write(ByteBuffer data) {
        if (end) {
            throw new RuntimeException("Response end!");
        }

        if (!isHeaderWritten) {
            writeHeader();
        }

        if (isChunked) {
            connection.write(ByteBuffer.wrap((Long.toHexString(data.limit()) + "\r\n").getBytes()));
            connection.write(data);
            connection.write(ByteBuffer.wrap("\r\n".getBytes()));
        } else {
            connection.write(data);
        }

        return this;
    }

    private void writeHeader() {
        final var headerBuilder = new StringBuilder();

        headerBuilder.append(connection.getHttpVersion())
                .append(" ")
                .append(statusCode)
                .append(" ")
                .append(getStatusText())
                .append("\r\n");

        for (final var header : getHeaders()) {
            headerBuilder.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        headerBuilder.append("\r\n");

        connection.write(ByteBuffer.wrap(headerBuilder.toString().getBytes()));

        isHeaderWritten = true;
    }

    public void end() {
        if (end) {
            return;
        }

        if (!isHeaderWritten) {
            writeHeader();
        }

        if (isChunked) {
            connection.write(ByteBuffer.wrap("0\r\n\r\n".getBytes()));
        }

        connection.onRequestDone();
        end = true;
    }

    public boolean isEnd() {
        return end;
    }

    public Response setContentType(String type) {
        return setHeader("Content-Type", type);
    }

    public Response setContentLength(long length) {
        return setHeader("Content-Length", String.valueOf(length));
    }

    public Response useChunkedEncoding() {
        isChunked = true;
        return setHeader("Transfer-Encoding", "chunked");
    }
}
