package org.example.webframework.lesson1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerFullAsync {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT.%1$tL] [%4$-7s] %5$s %n");

        final var threadPool = new ThreadPoolExecutor(8, 100, 60,
                TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
        threadPool.submit(() -> {});

        final var group = AsynchronousChannelGroup.withThreadPool(threadPool);
        final var server = AsynchronousServerSocketChannel.open(group);
        server.bind(new InetSocketAddress(8080));

        server.accept(null, new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel socket, Object attachment) {
                server.accept(null, this);

                final var buffer = ByteBuffer.allocate(1024);

                socket.read(buffer, buffer, new CompletionHandler<>() {
                    @Override
                    public void completed(Integer length, ByteBuffer d) {
                        try {
                            if (length < 0) {
                                socket.close();
                                return;
                            }

                            d.clear();

                            Thread.sleep(10);

                            final var data = ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, world!".getBytes());

                            socket.write(data, data, new CompletionHandler<>() {
                                @Override
                                public void completed(Integer integer, ByteBuffer d) {
                                    try {
                                        if (!d.hasRemaining()) {
                                            socket.close();
                                            return;
                                        }

                                        socket.write(d, d, this);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void failed(Throwable throwable, ByteBuffer d) {
                                    throwable.printStackTrace();
                                }
                            });

                            socket.read(buffer, buffer, this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failed(Throwable throwable, ByteBuffer byteBuffer) {
                        if (throwable instanceof ClosedChannelException) {
                            return;
                        }

                        throwable.printStackTrace();
                    }
                });
            }

            @Override
            public void failed(Throwable throwable, Object attachment) {
                throwable.printStackTrace();
            }
        });
    }
}
