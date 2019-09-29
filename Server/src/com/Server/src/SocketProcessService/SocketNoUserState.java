package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import java.util.logging.Logger;

public class SocketNoUserState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketNoUserState(SocketProcess socketProcess) {
        super(socketProcess);
    }

    @Override
    protected void handleMsg(String[] msgFromClient){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                    " handle: " + msgFromClient[0] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgFromClient[0]){
            case MsgTypes.LoginRespMsg:
                super.socketProcess.handleLoginRespMsg(msgFromClient);
                break;
            case MsgTypes.RegisterReqMsg:
                super.socketProcess.handleRegisterReqMsg(msgFromClient);
                break;
            case MsgTypes.ClientExitInd:
                socketProcess.finishSocketProcess();
                break;
            default:
                defaultMsgHandler(msgFromClient);
                break;
        }
    }
}
