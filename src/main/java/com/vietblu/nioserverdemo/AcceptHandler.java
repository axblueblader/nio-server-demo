package com.vietblu.nioserverdemo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    private final AsynchronousServerSocketChannel server;
    private final ExecutorService main;
    private final ExecutorService worker;
    private final Deque<StringBuilder> deque;

    public AcceptHandler(AsynchronousServerSocketChannel server,
                         ExecutorService main,
                         ExecutorService worker,
                         Deque<StringBuilder> deque) {
        this.server = server;
        this.main = main;
        this.worker = worker;
        this.deque = deque;
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        main.submit(() -> server.accept(null, this));

        ByteBuffer buffer = ByteBuffer.allocate(5);
        worker.submit(() -> channel.read(buffer, null,
                                         new ReadHandler(worker, channel, buffer, deque)));
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
