package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.MsgTypes;
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
                " handle: " + msgInParts[CC.MSG_ID] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[CC.MSG_ID]){
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
                if(TimerTypeName.valueOf(msgInParts[CC.TIMER_TYPE]) == TimerTypeName.NoResponseTimer){
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
