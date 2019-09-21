package com.Server.src;

public abstract class SocketProcessState {
    protected SocketProcess socketProcess;

    public SocketProcessState(SocketProcess socketProcess) {
        this.socketProcess = socketProcess;
    }

    public abstract void run();
}
