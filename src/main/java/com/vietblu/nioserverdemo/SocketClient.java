package com.vietblu.nioserverdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketClient {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOut = new PrintWriter(socket.getOutputStream());
    }

    public String exchange(String mess) throws IOException {
        System.out.println(socket.getLocalSocketAddress() + " sending " + mess);
        socketOut.println(mess);
        socketOut.flush();
        return socketIn.readLine();
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public List<String> exchange(String mess, int num) throws IOException {
        System.out.println(socket.getLocalSocketAddress() + " sending " + mess);
        socketOut.println(mess);
        socketOut.flush();
        List<String> res = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String line = socketIn.readLine();
            res.add(line);
        }
        return res;
    }
}
