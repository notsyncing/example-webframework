package org.example.webframework.lesson12;

import org.example.webframework.lesson12.file.Range;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Response {
    private static final Map<Integer, String> statusMap = new HashMap<>();

    static {
        statusMap.put(200, "OK");
        statusMap.put(206, "Partial Content");
        statusMap.put(301, "Moved Permanently");
        statusMap.put(302, "Found");
        statusMap.put(307, "Temporary Redirect");
        statusMap.put(308, "Permanent Redirect");
        statusMap.put(304, "Not Modified");
        statusMap.put(403, "Forbidden");
        statusMap.put(404, "Not Found");
        statusMap.put(406, "Requested Range Not Satisfiable");
        statusMap.put(500, "Internal Server Error");
    }

    private final HttpConnection connection;
    private int statusCode;
    private final List<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
    private final List<Cookie> cookies = new ArrayList<>();
    private boolean isHeaderWritten;
    private boolean isChunked;
    private boolean end;
    private String contentType;

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

    public Response setDateHeader(String field, Instant time) {
        return setHeader(field, Request.HTTP_DATE_FORMATTER.format(time));
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

        if (!cookies.isEmpty()) {
            for (final var cookie : cookies) {
                headerBuilder.append("Set-Cookie: ")
                        .append(cookie.toString())
                        .append("\r\n");
            }
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

    public void endWithStatusCode(int code) {
        setStatusCode(code)
                .setContentLength(0)
                .end();
    }

    public void endWithStatusCode(int code, String message) {
        setStatusCode(code)
                .setContentType("text/plain")
                .setContentLength(message.length())
                .write(ByteBuffer.wrap(message.getBytes()))
                .end();
    }

    public Response setContentType(String type) {
        contentType = type;
        return setHeader("Content-Type", type);
    }

    public Response setContentLength(long length) {
        return setHeader("Content-Length", String.valueOf(length));
    }

    public Response useChunkedEncoding() {
        isChunked = true;
        return setHeader("Transfer-Encoding", "chunked");
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public Response addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    public Response addCookie(String name, String value) {
        return addCookie(new Cookie(name, value));
    }

    public void sendFile(Path path) throws IOException {
        if (!isHeaderWritten) {
            writeHeader();
        }

        connection.sendFile(path);
        end = true;
    }

    public void sendFilePartially(Path path, List<Range> ranges) throws IOException {
        if (ranges.size() <= 0) {
            throw new IllegalArgumentException("Must have at least 1 range!");
        }

        setStatusCode(206);
        final var fileLength = Files.size(path);

        if (ranges.size() == 1) {
            final var range = ranges.get(0);
            range.setIfNoEnd(fileLength - 1);
            setHeader("Content-Range", "bytes " + range + "/" + fileLength);
            setContentLength(range.getLength());

            writeHeader();

            connection.sendFilePartially(path, range.getStart(), range.getEnd());
        } else {
            final var boundary = "--" + UUID.randomUUID().toString().replace("-", "");
            final var oldContentType = contentType;
            setContentType("multipart/byteranges; boundary=" + boundary);

            final var partHeaders = new ArrayList<String>();
            var contentLength = 0L;

            for (var i = 0; i < ranges.size(); i++) {
                final var range = ranges.get(i);
                range.setIfNoEnd(fileLength - 1);

                final var b = new StringBuilder();

                if (i != 0) {
                    b.append("\r\n");
                }

                b.append("--").append(boundary).append("\r\n")
                        .append("Content-Type: ").append(oldContentType).append("\r\n")
                        .append("Content-Range: bytes ").append(range).append("/").append(fileLength)
                        .append("\r\n\r\n");

                partHeaders.add(b.toString());
                contentLength += b.length() + range.getLength();
            }

            final var tail = "\r\n--" + boundary + "--\r\n";
            contentLength += tail.length();

            setContentLength(contentLength);
            writeHeader();

            var cf = CompletableFuture.<Void>completedFuture(null);

            for (var i = 0; i < ranges.size(); i++) {
                final var currentIndex = i;

                cf = cf.thenCompose(r -> {
                    connection.write(partHeaders.get(currentIndex));

                    try {
                        final var range = ranges.get(currentIndex);
                        return connection.sendFilePartially(path, range.getStart(), range.getEnd());
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                });
            }

            cf.thenAccept(r -> {
                connection.write(tail);
                end();
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }

        end = true;
    }
}
