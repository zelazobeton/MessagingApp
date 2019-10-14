package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.MsgTypes;
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
                    " handle: " + msgInParts[CC.MSG_ID] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgInParts[CC.MSG_ID]){
            case MsgTypes.ClientMsg:
                super.socketProcess.handleClientMsgInLoggedInState(msgInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.handleIncomingConversationInLoggedInState(msgInParts);
                break;
            case MsgTypes.TimerExpired:
                if(TimerTypeName.valueOf(msgInParts[CC.TIMER_TYPE]) == TimerTypeName.NoResponseTimer){
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
