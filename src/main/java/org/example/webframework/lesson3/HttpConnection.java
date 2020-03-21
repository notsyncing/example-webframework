package org.example.webframework.lesson3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpConnection {
    private enum State {
        WaitingForRequest,
        ReadingRequestLine,
        ReadingHeaders
    }

    private static final int MAX_HEADER_LENGTH = 8192;

    private State state = State.WaitingForRequest;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private Context context;
    private int headerReadLength = 0;
    private AsynchronousSocketChannel socket;
    private final ThreadPoolExecutor threadPool;
    private final List<Handler> handlers;
    private long bodyReadLength = 0;

    private StringBuilder headerLine = new StringBuilder();

    public HttpConnection(ThreadPoolExecutor threadPool, List<Handler> handlers) {
        this.threadPool = threadPool;
        this.handlers = handlers;
    }

    public String getHttpVersion() {
        return context.getRequest().getVersion();
    }

    public void beginReadRequest(AsynchronousSocketChannel socket) {
        state = State.WaitingForRequest;
        context = new Context(new Request(this), new Response(this));
        this.socket = socket;
        buffer.clear();

        socket.read(buffer, this, new CompletionHandler<>() {
            @Override
            public void completed(Integer length, HttpConnection connection) {
                if (length < 0) {
                    close();
                    return;
                }

                threadPool.submit(() -> {
                    buffer.flip();

                    if (!parseRequest()) {
                        socket.read(buffer, connection, this);
                    } else {
                        process();
                    }
                });
            }

            @Override
            public void failed(Throwable throwable, HttpConnection connection) {
                throwable.printStackTrace();
                close();
            }
        });
    }

    private boolean readLine(StringBuilder target, ByteBuffer buf) {
        var lastChar = 0;

        if (target.length() > 0) {
            lastChar = target.charAt(target.length() - 1);
        }

        while (buf.hasRemaining()) {
            if (headerReadLength >= MAX_HEADER_LENGTH) {
                throw new RuntimeException("Request header too large!");
            }

            final var b = (char) buf.get();
            headerReadLength++;

            if (lastChar == '\r' && b == '\n') {
                target.deleteCharAt(target.length() - 1);
                return true;
            }

            lastChar = b;

            target.append(b);
        }

        return false;
    }

    private void parseRequestLine(String requestLine) {
        final var request = context.getRequest();
        final var requestLineTokens = requestLine.split(" ");
        final var method = HttpMethod.valueOf(requestLineTokens[0]);
        request.setMethod(method);
        request.setPathAndQuery(requestLineTokens[1]);
        request.setVersion(requestLineTokens[2]);
    }

    private void parseHeader(String header) {
        final var splitter = header.indexOf(':');
        final var fieldName = header.substring(0, splitter).trim();
        final var fieldValue = header.substring(splitter + 1).trim();
        context.getRequest().addHeader(fieldName, fieldValue);
    }

    private boolean parseRequest() {
        while (buffer.hasRemaining()) {
            switch (state) {
                case WaitingForRequest:
                    state = State.ReadingRequestLine;
                    break;
                case ReadingRequestLine:
                    if (readLine(headerLine, buffer)) {
                        parseRequestLine(headerLine.toString());
                        headerLine.setLength(0);
                        state = State.ReadingHeaders;
                    }

                    break;
                case ReadingHeaders:
                    if (readLine(headerLine, buffer)) {
                        final var header = headerLine.toString();

                        if (header.isEmpty()) {
                            headerReadLength = 0;
                            return true;
                        } else {
                            parseHeader(header);
                            headerLine.setLength(0);
                        }
                    }

                    break;
            }
        }

        buffer.clear();
        return false;
    }

    private void process() {
        new HandlerChain(handlers).nextHandler(context);
    }

    public void write(ByteBuffer data) {
        try {
            writeAsync(data).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Void> writeAsync(ByteBuffer data) {
        final var cf = new CompletableFuture<Void>();

        socket.write(data, data, new CompletionHandler<>() {
            @Override
            public void completed(Integer length, ByteBuffer d) {
                if (d.hasRemaining()) {
                    socket.write(d, d, this);
                } else {
                    cf.complete(null);
                }
            }

            @Override
            public void failed(Throwable throwable, ByteBuffer d) {
                cf.completeExceptionally(throwable);
            }
        });

        return cf;
    }

    public void onRequestDone() {
        if (context.getRequest().isKeepAliveConnection()) {
            beginReadRequest(socket);
        } else {
            close();
        }
    }

    private void close() {
        if (socket.isOpen()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CompletableFuture<ByteBuffer> readBody(long expectedLength) {
        bodyReadLength = 0;

        final var fullBuffer = ByteBuffer.allocate((int) expectedLength);
        fullBuffer.put(buffer.array(), buffer.position(),
                buffer.remaining() > expectedLength ? (int) expectedLength : buffer.remaining());
        buffer.position(buffer.position() + fullBuffer.position());

        if (fullBuffer.position() >= expectedLength) {
            fullBuffer.flip();
            return CompletableFuture.completedFuture(fullBuffer);
        } else {
            bodyReadLength += fullBuffer.position();
        }

        final var cf = new CompletableFuture<ByteBuffer>();

        socket.read(fullBuffer, this, new CompletionHandler<>() {
            @Override
            public void completed(Integer length, HttpConnection connection) {
                if (length < 0) {
                    close();
                    cf.completeExceptionally(new ClosedChannelException());
                    return;
                }

                bodyReadLength += length;

                if (bodyReadLength < expectedLength) {
                    socket.read(fullBuffer, connection, this);
                } else {
                    fullBuffer.flip();
                    cf.complete(fullBuffer);
                }
            }

            @Override
            public void failed(Throwable throwable, HttpConnection connection) {
                cf.completeExceptionally(throwable);
                close();
            }
        });

        return cf;
    }
}
