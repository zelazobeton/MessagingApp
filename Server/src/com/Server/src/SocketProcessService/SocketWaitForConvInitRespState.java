package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.SMsgTypes;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public class SocketWaitForConvInitRespState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketWaitForConvInitRespState(SocketProcess socketProcess) {
        super(socketProcess);
//        super.socketProcess.startTimer(TimerType.WaitForConvAcceptTimer);
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                " handle: " + msgInParts[CC.MSG_ID] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[CC.MSG_ID]){
            case SMsgTypes.ClientMsg:
                super.socketProcess.handleClientMsgInWaitForConvInitRespState(msgInParts);
                break;
            case SMsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case SMsgTypes.IntConvInitRespMsg:
                super.socketProcess.handleIntConvInitRespMsg(msgInParts);
                break;
            case SMsgTypes.IntRouteFailInd:
                super.socketProcess.sendMsgToClient(SMsgTypes.ServerInfoMsg + "_" +
                                                    "Conversation start failed: " + msgInParts[1]);
                super.socketProcess.setState(new SocketLoggedIdleState(super.socketProcess));
                super.socketProcess.resetConvUser();
                break;
            case SMsgTypes.TimerExpired:
                if (TimerTypeName.valueOf(msgInParts[CC.TIMER_TYPE]) == TimerTypeName.NoResponseTimer){
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
