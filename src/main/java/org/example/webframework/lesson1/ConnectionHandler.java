package org.example.webframework.lesson1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ConnectionHandler {
    private Selector selector;
    private SocketChannel socket;
    private Queue<ByteBuffer> dataToWrite = new LinkedBlockingQueue<>();
    private static Logger log = Logger.getAnonymousLogger();
    private int id;
    private static AtomicInteger counter = new AtomicInteger(0);

    public ConnectionHandler(Selector selector, SocketChannel socket) {
        this.selector = selector;
        this.socket = socket;
        this.id = counter.getAndIncrement();
    }

    public int getId() {
        return id;
    }

    public void receivedData(ByteBuffer buffer) throws InterruptedException, IOException {
        //log.info("Begin handle connection " + id);

        buffer.clear();

        handle();

        //log.info("Begin write connection " + id);

        while (!dataToWrite.isEmpty()) {
            var buf = dataToWrite.peek();
            var writeLength = 0;

            while (buf.hasRemaining()) {
                writeLength = socket.write(buf);

                if (writeLength <= 0) {
                    //log.info("Register write connection " + id);
                    registerForWrite();
                    return;
                }
            }

            dataToWrite.poll();
        }

        //log.info("End write connection " + id);
    }

    private void registerForWrite() throws ClosedChannelException {
        socket.register(selector, SelectionKey.OP_WRITE, this);
        selector.wakeup();
    }

    private void handle() throws InterruptedException {
        Thread.sleep(10);

        dataToWrite.offer(ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Length: 13\r\nConnection: keep-alive\r\n\r\nHello, world!".getBytes()));
    }

    public ByteBuffer pullDataToSend() {
        return dataToWrite.peek();
    }

    public void currentBufferSent() {
        dataToWrite.poll();
    }

    public boolean hasDataToSend() {
        return !dataToWrite.isEmpty();
    }

    public boolean isKeepAlive() {
        return true;
    }
}
