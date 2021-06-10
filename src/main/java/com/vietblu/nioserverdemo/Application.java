package com.vietblu.nioserverdemo;

public final class Application {
    public static void main(String[] args) {
        final NioServer nioServer = new NioServer();
        nioServer.run();
    }
}
