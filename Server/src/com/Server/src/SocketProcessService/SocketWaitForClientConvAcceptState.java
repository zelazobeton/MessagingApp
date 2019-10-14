package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.MsgTypes;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public class SocketWaitForClientConvAcceptState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketWaitForClientConvAcceptState(SocketProcess socketProcess) {
        super(socketProcess);
        super.socketProcess.sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                "Do you accept conversation request from:" + super.socketProcess.convUserId + "?");
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                " handle: " + msgInParts[CC.MSG_ID] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[CC.MSG_ID]){
            case MsgTypes.ClientMsg:
                super.socketProcess.handleClientMsgInWaitForClientConvAcceptState(msgInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
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
