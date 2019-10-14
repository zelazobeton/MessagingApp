package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.SMsgTypes;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public class SocketWaitForClientConvAcceptState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketWaitForClientConvAcceptState(SocketProcess socketProcess) {
        super(socketProcess);
        socketProcess.sendMsgToClient(SMsgTypes.Interface + "_" +
                "Do you accept conversation request from:" +
                socketProcess.convUserUsername + "?" +
                "_yes (accept)_no (reject)");
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + socketProcess.getSocketProcessId() +
                " handle: " + msgInParts[CC.MSG_ID] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[CC.MSG_ID]){
            case SMsgTypes.ClientMsg:
                socketProcess.handleClientMsgInWaitForClientConvAcceptState(msgInParts);
                break;
            case SMsgTypes.IntConvInitReqMsg:
                socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case SMsgTypes.IntCancelProcMsg:
                socketProcess.sendMsgToClient(SMsgTypes.ServerInfoMsg + "_Conversation finished by another user");
                socketProcess.resetConvUser();
                socketProcess.setState(new SocketLoggedIdleState(socketProcess));
                break;
            case SMsgTypes.TimerExpired:
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
