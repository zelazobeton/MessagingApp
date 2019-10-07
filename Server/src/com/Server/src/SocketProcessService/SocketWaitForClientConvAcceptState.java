package com.Server.src.SocketProcessService;

import com.Server.src.ConvInitStatus;
import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import com.Server.src.ServerTimers.TimerType;
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
                " handle: " + msgInParts[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[0]){
            case MsgTypes.ClientMsg:
                super.socketProcess.handleClientMsgInWaitForClientConvAcceptState(msgInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
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
