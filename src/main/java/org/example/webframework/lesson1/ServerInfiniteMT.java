package org.example.webframework.lesson1;

import java.io.*;
import java.net.ServerSocket;

public class ServerInfiniteMT {
    public static void main(String[] args) throws IOException {
        final var server = new ServerSocket(8080);

        while (true) {
            // 注意此处不再使用 try-with-resources
            final var socket = server.accept();

            // 创建线程
            final var thread = new Thread(() -> {
                // 原来的处理逻辑
                try (socket; // 保证 socket 在该线程结束时被关闭
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

            // 启动线程
            thread.start();
        }
    }
}
