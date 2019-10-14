package com.Server.src.SocketProcessService;

import com.Server.src.Constants.CC;
import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.SMsgTypes;
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
            case SMsgTypes.LoginReqMsg:
                super.socketProcess.handleLoginReqMsg(msgInParts);
                break;
            case SMsgTypes.RegisterReqMsg:
                super.socketProcess.handleRegisterReqMsg(msgInParts);
                break;
            case SMsgTypes.ClientExitInd:
                super.socketProcess.finishSocketProcess();
                break;
            case SMsgTypes.TimerExpired:
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
