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
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + socketProcess.getSocketProcessId() +
                    " handle: " + msgInParts[CC.MSG_ID] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgInParts[CC.MSG_ID]){
            case SMsgTypes.ClientMsg:
                socketProcess.handleClientMsgInWaitForConvInitRespState(msgInParts);
                break;
            case SMsgTypes.IntConvInitReqMsg:
                socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case SMsgTypes.IntConvInitRespMsg:
                socketProcess.handleIntConvInitRespMsg(msgInParts);
                break;
            case SMsgTypes.IntRouteFailInd:
                socketProcess.sendMsgToClient(
                        SMsgTypes.ServerInfoMsg + "_Conversation start failed: " + msgInParts[1]);
                socketProcess.resetConvUser();
                socketProcess.setState(new SocketLoggedIdleState(socketProcess));
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
