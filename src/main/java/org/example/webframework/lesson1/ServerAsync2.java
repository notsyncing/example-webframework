package org.example.webframework.lesson1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerAsync2 {
    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT.%1$tL] [%4$-7s] %5$s %n");

        var coreCount = 4;

        final var ioThreadPool = Executors.newFixedThreadPool(coreCount);
        final var ioWorkers = new ArrayList<IOWorker>();
        var counter = 0;

        final var workerThreadPool = new ThreadPoolExecutor(100, 100, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        workerThreadPool.allowCoreThreadTimeOut(true);

        for (var i = 0; i < coreCount; i++) {
            var ioWorker = new IOWorker(workerThreadPool);
            ioWorkers.add(ioWorker);
            ioThreadPool.submit(ioWorker);
        }

        var selector = Selector.open();
        var serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        var server = serverChannel.socket();
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(8080));

        //var log = Logger.getAnonymousLogger();

        while (true) {
            selector.select();
            var keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                var key = keyIterator.next();

                if (key.isAcceptable()) {
                    var channel = (ServerSocketChannel) key.channel();
                    var socket = channel.accept();
                    socket.configureBlocking(false);

                    var ioWorker = ioWorkers.get(counter);
                    ioWorker.register(socket);

                    counter++;

                    if (counter >= coreCount) {
                        counter = 0;
                    }

                    //key.cancel();
                    //log.info("Accepted connection " + handler.getId());
                }

                keyIterator.remove();
            }
        }
    }
}
