package com.vietblu.nioserverdemo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    private final AsynchronousServerSocketChannel server;
    private final ExecutorService main;
    private final ExecutorService worker;
    private final Integer bufferSize = (Integer) System.getProperties()
                                                       .getOrDefault("buffer_size", 8);

    public AcceptHandler(AsynchronousServerSocketChannel server,
                         ExecutorService main,
                         ExecutorService worker) {
        this.server = server;
        this.main = main;
        this.worker = worker;
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        main.submit(() -> server.accept(null, this));

        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        final Deque<StringBuilder> deque = new ConcurrentLinkedDeque<>();
        worker.submit(() -> channel.read(buffer, null,
                                         new ReadHandler(worker, channel, buffer, deque)));
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
