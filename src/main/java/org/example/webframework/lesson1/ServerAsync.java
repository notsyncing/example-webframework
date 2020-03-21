package org.example.webframework.lesson1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ServerAsync {
    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT.%1$tL] [%4$-7s] %5$s %n");

        final var threadPool = new ThreadPoolExecutor(8, 100, 60,
                TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        final var selector = Selector.open();
        final var serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverChannel.bind(new InetSocketAddress(8080));

        var log = Logger.getAnonymousLogger();

        while (true) {
            if (selector.select() <= 0) {
                continue;
            }

            final var keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                final var key = keyIterator.next();

                if (!key.isValid()) {
                    keyIterator.remove();
                    continue;
                }

                if (key.isAcceptable()) {
                    final var channel = (ServerSocketChannel) key.channel();
                    final var clientChannel = channel.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    final var channel = (SocketChannel) key.channel();

                    var readLength = 0;

                    final var buffer = ByteBuffer.allocate(1024);

                    try {
                        readLength = channel.read(buffer);
                    } catch (ClosedChannelException e) {
                        keyIterator.remove();
                        continue;
                    }

                    if (readLength > 0) {
                        threadPool.submit(() -> {
                            try {
                                Thread.sleep(10);

                                final var data = ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, world!".getBytes());
                                channel.write(data);

                                if (data.hasRemaining()) {
                                    channel.register(selector, SelectionKey.OP_WRITE, data);
                                    selector.wakeup();
                                } else {
                                    channel.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (readLength < 0) {
                        channel.close();
                    }
                } else if (key.isWritable()) {
                    final var channel = (SocketChannel) key.channel();
                    final var data = (ByteBuffer) key.attachment();

                    if (data != null) {
                        channel.write(data);

                        if (!data.hasRemaining()) {
                            key.attach(null);
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            channel.close();
                        }
                    }
                }

                keyIterator.remove();
            }
        }
    }
}
