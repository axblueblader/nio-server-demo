package com.vietblu.nioserverdemo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

public class ReadHandler implements CompletionHandler<Integer, Object> {
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
        System.out.println("Received: " + frame);
        if (frame.contains("\r\n")) {
            String[] split = frame.split("\r\n");
            StringBuilder last = deque.peekLast();
            int i = 0;
            if (last != null && !last.toString().endsWith("\r\n")) {
                // append the message part to the ending of last builder
                last.append(split[0]).append("\r\n");
                i++;
            }
            // add the rest of full message to dequeue as separate builders
            for (; i < split.length - 1; i++) {
                deque.addLast(new StringBuilder(split[i] + "\r\n"));
            }

            // the last element is a partial message so it doesn't end with \r\n
            deque.addLast(new StringBuilder(split[split.length-1]));
        } else { // only a partial message
            StringBuilder last = deque.peekLast();
            if (last == null || last.toString().endsWith("\r\n")) {
                // add new message builder to end of dequeue
                StringBuilder builder = new StringBuilder(frame);
                deque.addLast(builder);
            } else {
                // append partial message to last builder of dequeue
                last.append(frame);
            }
        }

        worker.submit(()-> {
            StringBuilder first = deque.peekFirst();
            if (first != null && first.toString().endsWith("\r\n")) {
                ByteBuffer message = ByteBuffer.wrap(first.toString().getBytes());
                // echo
                channel.write(message,null,null);
            }
        });
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
