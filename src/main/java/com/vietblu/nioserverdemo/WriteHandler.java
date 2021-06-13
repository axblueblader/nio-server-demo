package com.vietblu.nioserverdemo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class WriteHandler implements CompletionHandler<Integer, Object> {
    private final ExecutorService worker;
    private final AsynchronousSocketChannel channel;
    private final ByteBuffer writeBuf;
    private final Queue<String> writeQueue;

    public WriteHandler(ExecutorService worker, AsynchronousSocketChannel channel,
                        ByteBuffer writeBuf, Queue<String> writeQueue) {
        this.worker = worker;
        this.channel = channel;
        this.writeBuf = writeBuf;
        this.writeQueue = writeQueue;
    }

    @Override
    public void completed(Integer bytesWritten, Object attachment) {
        if (bytesWritten > 0 && writeBuf.hasRemaining()) {// write not finished, continue writing this buffer
            channel.write(writeBuf, null, this);
        } else {
            // Continue to write from the queue
            String message = writeQueue.peek();
            if (message != null) {
                writeQueue.remove();
                ByteBuffer writeBuf = ByteBuffer.wrap(message.getBytes());
                channel.write(writeBuf, null, new WriteHandler(worker, channel, writeBuf, writeQueue));
            }
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        System.out.println(exc.getMessage());
    }
}
