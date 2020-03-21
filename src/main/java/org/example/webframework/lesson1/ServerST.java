package org.example.webframework.lesson1;

import java.io.*;
import java.net.ServerSocket;

public class ServerST {
    public static void main(String[] args) throws IOException {
        final var server = new ServerSocket(8080);

        while (true) {
            try (final var socket = server.accept();
                 final var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 final var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                do {
                    reader.readLine();
                } while (reader.ready());

                writer.write("HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, world!");
            }
        }
    }
}