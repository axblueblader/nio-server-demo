package com.vietblu.nioserverdemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NioServerTest {
    static NioServer server;

    @BeforeAll
    static void setUp() {
        server = new NioServer();
        final Thread thread = new Thread(server);
        thread.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        server.shutdown();
    }

    SocketClient startClient() throws Exception {
        final SocketClient client = new SocketClient("localhost", 8989);
        client.connect();
        return client;
    }

    @Test
    void buf8_echo1Less8_success() throws Exception {
        final SocketClient client = startClient();
        final String abcde = client.exchange("abcde");
        client.disconnect();

        assertEquals("abcde", abcde);
    }

    @Test
    void buf8_echo4Less8_success() throws Exception {
        final SocketClient client = startClient();
        final List<String> abcd = client.exchange("a\nb\nc\nd", 4);
        client.disconnect();

        assertEquals("a", abcd.get(0));
        assertEquals("b", abcd.get(1));
        assertEquals("c", abcd.get(2));
        assertEquals("d", abcd.get(3));
    }

    @Test
    void buf8_echo4Long_success() throws Exception {
        final SocketClient client = startClient();
        final List<String> abcd = client.exchange("12345678\n987654321\nabc\nd", 4);
        client.disconnect();

        assertEquals("12345678", abcd.get(0));
        assertEquals("987654321", abcd.get(1));
        assertEquals("abc", abcd.get(2));
        assertEquals("d", abcd.get(3));
    }

    @Test
    void buf8_echo10Less8_success() throws Exception {
        final SocketClient client = startClient();
        for (int i = 0; i < 10; i++) {
            final String abcde = client.exchange("abcde" + i);
            assertEquals("abcde" + i, abcde);
        }
        client.disconnect();
    }

    @Test
    void buf8_echo1Equal9_success() throws Exception {
        final SocketClient client = startClient();
        final String res = client.exchange("12345678");
        client.disconnect();

        assertEquals("12345678", res);
    }

    @Test
    void buf8_echo1Equal16_success() throws Exception {
        final SocketClient client = startClient();
        // String with 15 chars
        final String mess16 = new String(new char[15]).replace("\0", "p");
        final String res = client.exchange(mess16);
        client.disconnect();

        assertEquals(mess16, res);
    }

    @Test
    void buf8_echo1Equal17_success() throws Exception {
        final SocketClient client = startClient();
        // String with 16 chars
        final String mess16 = new String(new char[16]).replace("\0", "p");
        final String res = client.exchange(mess16);
        client.disconnect();

        assertEquals(mess16, res);
    }

    @Test
    void buf8_echoThread3Task10000Equal17_success() throws Exception {
        // String with 16 chars, including \n
        final String mess16 = new String(new char[15]).replace("\0", "p");
        final List<Callable<Object>> tasks = new ArrayList<>();
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final int taskNum = 10000;
        for (int i = 0; i < taskNum; i++) {
            tasks.add(() -> {
                final SocketClient client = startClient();
                final String res = client.exchange(mess16);
                client.disconnect();
                return res;
            });
        }
        final List<Future<Object>> futures = executor.invokeAll(tasks, 3000, TimeUnit.SECONDS);
        assertFalse(futures.isEmpty());
        assertEquals(taskNum, futures.size());
        for (Future<Object> future : futures) {
            assertEquals(mess16, future.get().toString());
        }

        System.out.println(futures.size());
    }
}

