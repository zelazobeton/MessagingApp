package com.Server.src;

import java.util.logging.Logger;

public class Main {
    public static final Integer CONNECTION_PORT = 5000;
    private static Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public static void main(String[] args) {
        SocketManager socketManager = new SocketManager(CONNECTION_PORT);
        socketManager.run();
        LOGGER.fine("End of process main");
    }
}
