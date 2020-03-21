package org.example.webframework.lesson1;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerMT {
    public static void main(String[] args) throws IOException {
        final var threadPool = new ThreadPoolExecutor(8, 100, 20,
                TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        final var server = new ServerSocket(8080);

        while (true) {
            final var socket = server.accept();

            threadPool.submit(() -> {
                try (socket;
                     final var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     final var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                    do {
                        reader.readLine();
                    } while (reader.ready());

                    Thread.sleep(10);

                    writer.write("HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, world!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
