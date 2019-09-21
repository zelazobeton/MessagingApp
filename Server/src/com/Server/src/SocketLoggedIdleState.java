package com.Server.src;

import java.util.logging.Logger;

public class SocketLoggedIdleState extends SocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketLoggedIdleState(SocketProcess socketProcess) {
        super(socketProcess);
        LOGGER.fine("SocketProcessId: " +
                super.socketProcess.getSocketProcessId() +
                " set to SocketNoUserState");
    }

    @Override
    public void run() {
        while(true){
            LOGGER.fine("Running SocketLoggedIdleState");
            super.socketProcess.sleepWithExceptionHandle(2000);
        }
    }
}
