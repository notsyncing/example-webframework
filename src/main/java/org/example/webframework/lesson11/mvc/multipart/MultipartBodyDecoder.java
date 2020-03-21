package org.example.webframework.lesson11.mvc.multipart;

import org.example.webframework.lesson11.Request;
import org.example.webframework.lesson11.mvc.BodyDecoder;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class MultipartBodyDecoder implements BodyDecoder {
    private enum State {
        Initial,
        ReadingPartHeaders,
        ReadingPartBody
    }

    private static final int BODY_READ_LENGTH = 1024;

    private Request request;
    private byte[] boundaryBytes;
    private State state = State.Initial;
    private ByteArrayOutputStream currentHeader = new ByteArrayOutputStream();
    private Part currentPart = null;
    private String boundary;
    private int boundaryIndex = 0;
    private String contentDisposition;
    private String contentType;
    private int lastByte = '\0';
    private int skipBytes = 0;
    private boolean isFirstBoundary = true;

    @Override
    public void decode(Request request) throws Exception {
        this.request = request;
        final var contentLength = request.getContentLength();
        boundary = "--" + request.getBoundary();
        boundaryBytes = boundary.getBytes();
        final var len = contentLength > BODY_READ_LENGTH ? BODY_READ_LENGTH : contentLength;
        var readLength = 0;

        while (readLength < contentLength) {
            final var actualLen = Math.min(contentLength - readLength, len);
            final var data = request.readBody((int) actualLen).get();
            readLength += data.limit();

            while (data.hasRemaining()) {
                final var b = data.get();
                boundaryOrContent(b);
            }
        }
    }

    private void boundaryOrContent(int b) throws Exception {
        if (b == boundaryBytes[boundaryIndex]) {
            boundaryIndex++;

            if (boundaryIndex >= boundaryBytes.length) {
                if (state == State.ReadingPartBody) {
                    currentPart.done(request);
                }

                state = State.ReadingPartHeaders;
                boundaryIndex = 0;
                skipBytes = 2;

                if (isFirstBoundary) {
                    isFirstBoundary = false;
                    boundaryBytes = ("\r\n" + boundary).getBytes();
                }
            }
        } else {
            if (boundaryIndex > 0) {
                for (var i = 0; i < boundaryIndex; i++) {
                    processContentByte(boundaryBytes[i]);
                }

                boundaryIndex = 0;
                boundaryOrContent(b);
            } else {
                processContentByte(b);
            }
        }
    }

    private void processContentByte(int b) throws Exception {
        if (skipBytes > 0) {
            skipBytes--;
            return;
        }

        switch (state) {
            case ReadingPartHeaders:
                if (lastByte == '\r' && b == '\n') {
                    var header = new String(currentHeader.toByteArray(), StandardCharsets.UTF_8);
                    header = header.substring(0, header.length() - 1);

                    if (header.length() <= 0) {
                        currentPart = Part.factory(contentDisposition, contentType);
                        state = State.ReadingPartBody;
                    } else {
                        final var colon = header.indexOf(':');
                        final var headerName = header.substring(0, colon).trim();
                        final var headerValue = header.substring(colon + 1).trim();

                        switch (headerName) {
                            case "Content-Disposition":
                                contentDisposition = headerValue;
                                break;
                            case "Content-Type":
                                contentType = headerValue;
                                break;
                        }
                    }

                    currentHeader = new ByteArrayOutputStream();
                } else {
                    currentHeader.write(b);
                }

                lastByte = b;
                break;

            case ReadingPartBody:
                currentPart.parse(b);
                break;
        }
    }

    @Override
    public void close() throws Exception {
        if (currentPart != null) {
            currentPart.close();
        }
    }
}
