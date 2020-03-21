package org.example.webframework.lesson8;

import org.example.webframework.lesson8.file.StaticFileHandler;
import org.example.webframework.lesson8.mvc.MvcHandler;
import org.example.webframework.lesson8.session.SessionHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private final List<Handler> handlers = new ArrayList<>();

    private void run() throws IOException {
        final var workerThreadPool = new ThreadPoolExecutor(100, 100, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        workerThreadPool.allowCoreThreadTimeOut(true);

        final var eventThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        eventThreadPool.submit(() -> {});

        final var group = AsynchronousChannelGroup.withThreadPool(eventThreadPool);
        final var server = AsynchronousServerSocketChannel.open(group);
        server.bind(new InetSocketAddress(8080));

        server.accept(new HttpConnection(workerThreadPool, handlers), new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel socket, HttpConnection conn) {
                server.accept(new HttpConnection(workerThreadPool, handlers), this);

                workerThreadPool.submit(() -> conn.beginReadRequest(socket));
            }

            @Override
            public void failed(Throwable throwable, HttpConnection attachment) {
                throwable.printStackTrace();
            }
        });
    }

    public Server addHandler(Handler handler) {
        handlers.add(handler);
        return this;
    }

    public static void main(String[] args) throws IOException {
        new Server()
                .addHandler(new SessionHandler())
                .addHandler(new MvcHandler()
                        .registerController(HelloController.class))
                .addHandler(new StaticFileHandler(Paths.get("static")))
                .addHandler(new DefaultHandler())
                .run();
    }
}
