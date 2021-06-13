package com.vietblu.nioserverdemo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class ReadHandler implements CompletionHandler<Integer, Object> {
    private static final String DELIM = "\n";
    // split keeping delimiter at the end
    private static final Pattern PATTERN = Pattern.compile(
            "(?<=" + DELIM + ')');
    private final ExecutorService worker;
    private final AsynchronousSocketChannel channel;
    private final ByteBuffer buffer;
    private StringBuffer messBuf;
    private final Queue<String> writeQueue;

    public ReadHandler(ExecutorService worker,
                       AsynchronousSocketChannel channel,
                       ByteBuffer buffer,
                       StringBuffer messBuf, Queue<String> writeQueue) {
        this.worker = worker;
        this.channel = channel;
        this.buffer = buffer;
        this.messBuf = messBuf;
        this.writeQueue = writeQueue;
    }

    @Override
    public void completed(Integer bytesRead, Object attachment) {
        if (bytesRead == -1) {
            return;
        }
        final String frame = new String(buffer.array(), buffer.arrayOffset(), buffer.position());
        try {
            System.out.println("From " + channel.getRemoteAddress().toString() + ":" + frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int startIdx = 0;
        int endIdx;
        while (frame.indexOf(DELIM, startIdx) != -1) {
            endIdx = frame.indexOf(DELIM, startIdx) + 1;
            messBuf.append(frame, startIdx, endIdx);
            writeQueue.add(messBuf.toString());
            this.messBuf = new StringBuffer();
            startIdx = endIdx;
        }
        messBuf.append(frame, startIdx, frame.length());

        buffer.clear();
        channel.read(buffer, null, this);

        if (!writeQueue.isEmpty()) {
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
