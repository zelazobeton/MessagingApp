package com.Client.src;

public class Main {
    private static final Integer CONNECTION_PORT = 5000;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        ClientManager clientManager = new ClientManager(HOST, CONNECTION_PORT);
        clientManager.run();
    }
}

