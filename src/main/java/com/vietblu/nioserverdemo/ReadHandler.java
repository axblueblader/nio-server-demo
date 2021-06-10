package com.vietblu.nioserverdemo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Deque;
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
    private final Deque<StringBuilder> deque;

    public ReadHandler(ExecutorService worker,
                       AsynchronousSocketChannel channel,
                       ByteBuffer buffer,
                       Deque<StringBuilder> deque) {
        this.worker = worker;
        this.channel = channel;
        this.buffer = buffer;
        this.deque = deque;
    }

    @Override
    public void completed(Integer bytesRead, Object attachment) {
        if (bytesRead == -1) {
            return;
        }
        final String frame = new String(buffer.array(), buffer.arrayOffset(), buffer.position());
        try {
            System.out.println("Received: " + frame + " from " + channel.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (frame.contains(DELIM)) {
            final String[] split = PATTERN.split(frame);
            final StringBuilder last = deque.peekLast();
            int i = 0;
            if (last != null && !last.toString().endsWith(DELIM)) {
                // append the message part to the ending of last builder
                last.append(split[0]);
                i++;
            }
            // add the rest of messages (full or partial) to dequeue as separate builders
            for (; i < split.length; i++) {
                deque.addLast(new StringBuilder(split[i]));
            }
        } else { // only a partial message
            final StringBuilder last = deque.peekLast();
            if (last == null || last.toString().endsWith(DELIM)) {
                // add new message builder to end of dequeue
                final StringBuilder builder = new StringBuilder(frame);
                deque.addLast(builder);
            } else {
                // append partial message to last builder of dequeue
                last.append(frame);
            }
        }

        worker.submit(() -> {
            final StringBuilder message = deque.peekFirst();
            if (message != null && message.toString().endsWith(DELIM)) {
                final ByteBuffer writeBuf = ByteBuffer.wrap(message.toString().getBytes());
                // echo
                channel.write(writeBuf, null, new WriteHandler(worker, channel, deque, writeBuf));
            }
        });
        buffer.clear();
        channel.read(buffer,null,this);
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
