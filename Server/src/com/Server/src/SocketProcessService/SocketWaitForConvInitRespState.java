package com.Server.src.SocketProcessService;

import com.Server.src.ConvInitStatus;
import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import com.Server.src.ServerTimers.TimerType;
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
                " handle: " + msgInParts[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[0]){
            case MsgTypes.ClientMsg:
                super.socketProcess.handleClientMsgInWaitForConvInitRespState(msgInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case MsgTypes.IntConvInitRespMsg:
//                super.socketProcess.stopTimer(TimerTypeName.WaitForConvAcceptTimer);
                super.socketProcess.handleIntConvInitRespMsg(msgInParts);
                break;
            case MsgTypes.IntRouteFailInd:
//                super.socketProcess.stopTimer(TimerTypeName.WaitForConvAcceptTimer);
                super.socketProcess.sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                                                    "Conversation start failed: " + msgInParts[1]);
                super.socketProcess.setState(new SocketLoggedIdleState(super.socketProcess));
                super.socketProcess.resetConvUser();
                break;
            case MsgTypes.TimerExpired:
//                if (TimerTypeName.valueOf(msgInParts[1]) == TimerTypeName.WaitForConvAcceptTimer){
//                        super.socketProcess.stopTimer(TimerTypeName.WaitForConvAcceptTimer);
//                        super.socketProcess.setState(new SocketLoggedIdleState(super.socketProcess));
//                        super.socketProcess.sendConvFinishInd();
//                        super.socketProcess.sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
//                                                            "Conversation start failed: " + ConvInitStatus.TimerExpired);
//                        super.socketProcess.resetConvUser();
//                }
                if (TimerTypeName.valueOf(msgInParts[1]) == TimerTypeName.NoResponseTimer){
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
