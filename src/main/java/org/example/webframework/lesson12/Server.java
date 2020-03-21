package org.example.webframework.lesson12;

import org.example.webframework.lesson12.cors.CorsHandler;
import org.example.webframework.lesson12.file.StaticFileHandler;
import org.example.webframework.lesson12.filter.FilterHandler;
import org.example.webframework.lesson12.ioc.Container;
import org.example.webframework.lesson12.ioc.LifecycleAware;
import org.example.webframework.lesson12.mvc.MvcHandler;
import org.example.webframework.lesson12.session.SessionHandler;

import javax.inject.Named;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Named
public class Server implements LifecycleAware {
    private final List<Class<? extends Handler>> handlerClasses = new ArrayList<>();
    private final List<Handler> handlers = new ArrayList<>();
    private final Container container;

    public Server(Container container) {
        this.container = container;
    }

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

    public Server addHandler(Class<? extends Handler> handlerClass) {
        handlerClasses.add(handlerClass);
        return this;
    }

    @Override
    public void started() {
        try {
            addHandler(SessionHandler.class);
            addHandler(FilterHandler.class);
            addHandler(CorsHandler.class);
            addHandler(MvcHandler.class);
            addHandler(StaticFileHandler.class);
            addHandler(DefaultHandler.class);

            initHandlers();

            run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initHandlers() throws Exception {
        for (final var handlerClass : handlerClasses) {
            final var handler = (Handler) container.get(handlerClass);
            handlers.add(handler);
        }
    }
}
