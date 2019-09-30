package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import java.util.logging.Logger;

public abstract class ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    protected SocketProcess socketProcess;

    public ISocketProcessState(SocketProcess socketProcess) {
        this.socketProcess = socketProcess;
        LOGGER.fine("SocketProcess: " +
                socketProcess.getSocketProcessId() +
                " set to " + this.getClass().getSimpleName());
    }

    protected abstract void handleMsgFromSocketProcessQueue(String[] msgFromClient);

    protected void defaultMsgHandler(String[] msgFromQueue){
        switch (msgFromQueue[0]){
            case MsgTypes.ClientLiveConnectionInd:
                socketProcess.resetNoResponseTimer();
                break;
            case MsgTypes.NoResponseTimerExpired:
                socketProcess.logoutUser();
                socketProcess.IS_RUNNING = false;
                break;
            default:
                break;
        }
    }
}
