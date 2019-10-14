package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.MsgTypes;
import com.Server.src.ServerTimers.TimerTypeName;

import java.util.logging.Logger;

public class SocketNoUserState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketNoUserState(SocketProcess socketProcess) {
        super(socketProcess);
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                    " handle: " + msgInParts[CC.MSG_ID] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgInParts[CC.MSG_ID]){
            case MsgTypes.LoginRespMsg:
                super.socketProcess.handleLoginRespMsg(msgInParts);
                break;
            case MsgTypes.RegisterReqMsg:
                super.socketProcess.handleRegisterReqMsg(msgInParts);
                break;
            case MsgTypes.ClientExitInd:
                super.socketProcess.finishSocketProcess();
                break;
            case MsgTypes.TimerExpired:
                if(TimerTypeName.valueOf(msgInParts[CC.TIMER_TYPE]) == TimerTypeName.NoResponseTimer){
                    socketProcess.finishSocketProcess();
                }
                break;
            default:
                defaultMsgHandler(msgInParts);
                break;
        }
    }
}
