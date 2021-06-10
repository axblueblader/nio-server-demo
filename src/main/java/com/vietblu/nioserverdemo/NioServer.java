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
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private final ExecutorService main = Executors.newFixedThreadPool(1);
    private final Deque<StringBuilder> dequeue = new ConcurrentLinkedDeque<>();
    private AsynchronousServerSocketChannel server;

    @Override
    public void run() {
        final AsynchronousChannelGroup group;
        try {
            group = AsynchronousChannelGroup.withThreadPool(worker);
            server = AsynchronousServerSocketChannel.open(group);
            final int port = 8989;
            server.bind(new InetSocketAddress(port));

            main.submit(() -> server.accept(null, new AcceptHandler(server, main, worker, dequeue)));

            System.out.println("Server started at: " + port);
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        main.shutdown();
        worker.shutdown();
    };
}
