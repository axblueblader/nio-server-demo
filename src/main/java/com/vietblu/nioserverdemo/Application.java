package com.vietblu.nioserverdemo;

public class Application {
    public static void main(String[] args) {
        final NioServer nioServer = new NioServer();
        nioServer.run();
    }
}
