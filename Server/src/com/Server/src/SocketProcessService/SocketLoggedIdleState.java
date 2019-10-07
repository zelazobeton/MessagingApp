package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public class SocketLoggedIdleState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketLoggedIdleState(SocketProcess socketProcess) {
        super(socketProcess);
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                    " handle: " + msgInParts[0] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgInParts[0]){
            case MsgTypes.ClientMsg:
                super.socketProcess.handleClientMsgInLoggedInState(msgInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.handleIncomingConversationInLoggedInState(msgInParts);
                break;
            case MsgTypes.TimerExpired:
                if(TimerTypeName.valueOf(msgInParts[1]) == TimerTypeName.NoResponseTimer){
                    socketProcess.logoutUser();
                    socketProcess.finishSocketProcess();
                }
                break;
            default:
                defaultMsgHandler(msgInParts);
                break;
        }
    }
}
