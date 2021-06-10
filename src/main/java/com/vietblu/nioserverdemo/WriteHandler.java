package com.vietblu.nioserverdemo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

public class WriteHandler implements CompletionHandler<Integer, Object> {
    private final ExecutorService worker;
    private final AsynchronousSocketChannel channel;
    private final Deque<StringBuilder> deque;
    private final ByteBuffer writeBuf;

    public WriteHandler(ExecutorService worker, AsynchronousSocketChannel channel,
                        Deque<StringBuilder> deque, ByteBuffer writeBuf) {
        this.worker = worker;
        this.channel = channel;
        this.deque = deque;
        this.writeBuf = writeBuf;
    }

    @Override
    public void completed(Integer bytesWritten, Object attachment) {
        if (bytesWritten <= 0 || !writeBuf.hasRemaining()) {
            deque.removeFirst();
            return;
        }
        // write not finished, continue writing this buffer
        worker.submit(() -> {
            channel.write(writeBuf, null, this);
        });
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
