package com.Server.src;


public class Main {
    public static final Integer CONNECTION_PORT = 5000;

    public static void main(String[] args) {
        SocketManager socketManager = new SocketManager(CONNECTION_PORT);
        socketManager.run();
        LOG.DEBUG("End of process main");
    }
}
