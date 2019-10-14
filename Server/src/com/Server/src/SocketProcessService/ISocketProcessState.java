package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.MsgTypes;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public abstract class ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    protected SocketProcess socketProcess;

    public ISocketProcessState(SocketProcess socketProcess) {
        this.socketProcess = socketProcess;
        LOGGER.fine("SocketProcess: " +
                socketProcess.getSocketProcessId() +
                " set to " + this.getClass().getSimpleName());
        this.socketProcess.sendStateUserInterfaceToClient(this.getClass().getSimpleName());
    }

    protected abstract void handleMsgFromSocketProcessQueue(String[] msgFromClient);

    protected void defaultMsgHandler(String[] msgFromQueue){
        switch (msgFromQueue[CC.MSG_ID]){
            case MsgTypes.ClientLiveConnectionInd:
                socketProcess.resetTimer(TimerTypeName.NoResponseTimer);
                break;
            default:
                LOGGER.fine("SocketProcess: " + socketProcess.getSocketProcessId() +
                        " ignored: " + msgFromQueue[CC.MSG_ID] +
                        " in state " + this.getClass().getSimpleName());
                break;
        }
    }
}
