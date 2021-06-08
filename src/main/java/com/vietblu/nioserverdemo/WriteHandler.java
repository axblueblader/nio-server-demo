package com.vietblu.nioserverdemo;

import java.nio.channels.CompletionHandler;

public class WriteHandler implements CompletionHandler<Integer,Object> {
    @Override
    public void completed(Integer bytesWritten, Object attachment) {

    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
