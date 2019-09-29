package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
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

    protected abstract void handleMsg(String[] msgFromClient);

    public void run() {
        while(IS_RUNNING){
            socketProcess.tryGetMsgFromClient();
            socketProcess.tryHandleNextMsgFromQueue();

            socketProcess.sleepWithExceptionHandle(500);
        }
    }

    protected void defaultMsgHandler(String[] msgFromQueue){
        switch (msgFromQueue[0]){
            case MsgTypes.ClientLiveConnectionInd:
                socketProcess.resetNoResponseTimer();
                break;
            case MsgTypes.NoResponseTimerExpired:
                socketProcess.logoutUser();
                IS_RUNNING = false;
                break;
            default:
                break;
        }
    }
}
