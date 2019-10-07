package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public class SocketConversationState extends ISocketProcessState{
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketConversationState(SocketProcess socketProcess) {
        super(socketProcess);
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                " handle: " + msgInParts[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[0]){
            case MsgTypes.ClientMsg:
                super.socketProcess.handleClientMsgInConversationState(msgInParts);
                break;
            case MsgTypes.IntConvUserMsg:
                super.socketProcess.handleMsgFromAnotherUser(msgInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case MsgTypes.IntConvFinishInd:
                super.socketProcess.handleConvFinish(msgInParts);
                super.socketProcess.setState(new SocketLoggedIdleState(super.socketProcess));
                break;
            case MsgTypes.TimerExpired:
                if(TimerTypeName.valueOf(msgInParts[1]) == TimerTypeName.NoResponseTimer){
                    socketProcess.sendConvFinishInd();
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
