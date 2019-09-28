package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;

import java.util.logging.Logger;

public abstract class ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    protected SocketProcess socketProcess;
    protected boolean IS_RUNNING = true;

    public ISocketProcessState(SocketProcess socketProcess) {
        this.socketProcess = socketProcess;
        LOGGER.fine("SocketProcessId: " +
                socketProcess.getSocketProcessId() +
                " set to " + this.getClass().getSimpleName());
    }

    public abstract void run();
}
