package com.Server.src.SocketProcessService;

import com.Server.src.ConvInitStatus;
import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import com.Server.src.ServerTimers.TimerType;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public class SocketWaitForConvAcceptState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketWaitForConvAcceptState(SocketProcess socketProcess) {
        super(socketProcess);
        super.socketProcess.startTimer(TimerType.WaitForConvAcceptTimer);
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                " handle: " + msgInParts[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[0]){
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case MsgTypes.IntConvInitRespMsg:
                super.socketProcess.stopTimer(TimerTypeName.WaitForConvAcceptTimer);
                super.socketProcess.handleIntConvInitRespMsg(msgInParts);
                break;
            case MsgTypes.IntRouteFailInd:
                super.socketProcess.stopTimer(TimerTypeName.WaitForConvAcceptTimer);
                super.socketProcess.sendMsgToClient(MsgTypes.ConvInitFailInd + "_" + msgInParts[1]);
                break;
            case MsgTypes.TimerExpired:
                if (TimerTypeName.valueOf(msgInParts[1]) == TimerTypeName.WaitForConvAcceptTimer){
                        super.socketProcess.stopTimer(TimerTypeName.WaitForConvAcceptTimer);
                        super.socketProcess.setState(new SocketLoggedIdleState(super.socketProcess));
                        super.socketProcess.sendMsgToClient(MsgTypes.ConvInitFailInd + "_" + ConvInitStatus.TimerExpired);
                }
            default:
                defaultMsgHandler(msgInParts);
                break;
        }
    }
}
