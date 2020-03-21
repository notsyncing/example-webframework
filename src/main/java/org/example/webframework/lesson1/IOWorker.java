package org.example.webframework.lesson1;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

public class IOWorker implements Runnable {
    private ThreadPoolExecutor workerThreadPool;
    private Selector selector;

    public IOWorker(ThreadPoolExecutor workerThreadPool) {
        this.workerThreadPool = workerThreadPool;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();

            while (true) {
                selector.select();

                var keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    var key = keyIterator.next();

                    if (key.isReadable()) {
                        var channel = (SocketChannel) key.channel();
                        var handler = (ConnectionHandler) key.attachment();

                        //log.info("Begin read connection " + handler.getId());

                        var readLength = 0;

                        do {
                            var buffer = ByteBuffer.allocate(1024);
                            readLength = channel.read(buffer);

                            if (readLength > 0) {
                                workerThreadPool.submit(() -> {
                                    try {
                                        handler.receivedData(buffer);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });

                                //log.info("Submit read connection " + handler.getId());
                            } else if (readLength < 0) {
                                channel.close();
                            }
                        } while (readLength > 0);
                    } else if (key.isWritable()) {
                        var channel = (SocketChannel) key.channel();
                        var handler = (ConnectionHandler) key.attachment();

                        //log.info("Begin async write connection " + handler.getId());

                        var data = handler.pullDataToSend();

                        if (data != null) {
                            channel.write(data);

                            if (!data.hasRemaining()) {
                                handler.currentBufferSent();
                            }
                        }

                        if (!handler.hasDataToSend()) {
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);

                            if (!handler.isKeepAlive()) {
                                channel.close();
                            }
                        }

                        //log.info("End async write connection " + handler.getId());
                    }

                    keyIterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(SocketChannel socket) throws ClosedChannelException {
        socket.register(selector, SelectionKey.OP_READ, new ConnectionHandler(selector, socket));
        selector.wakeup();
    }
}
