package com.Server.src.SocketProcessService;

public abstract class SocketProcessState {
    protected SocketProcess socketProcess;
    protected boolean IS_RUNNING = true;

    public SocketProcessState(SocketProcess socketProcess) {
        this.socketProcess = socketProcess;
    }

    public abstract void run();
}
