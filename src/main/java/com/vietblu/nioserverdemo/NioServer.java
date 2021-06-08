package com.vietblu.nioserverdemo;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioServer implements Runnable {
    private AsynchronousServerSocketChannel server;
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private final ExecutorService main = Executors.newFixedThreadPool(1);
    private final Deque<StringBuilder> dequeue = new ConcurrentLinkedDeque<>();

    @Override
    public void run() {
        final AsynchronousChannelGroup group;
        try {
            group = AsynchronousChannelGroup.withThreadPool(worker);
            server = AsynchronousServerSocketChannel.open(group);
            server.bind(new InetSocketAddress(8989));

            main.submit(() -> server.accept(null,
                                            new AcceptHandler(server, main, worker, dequeue)));

            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
