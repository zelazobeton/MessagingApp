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
    public void run() {
        int cycleCounter = 0;
        while(IS_RUNNING)
        {
            if((cycleCounter % 10) == 0){
                super.socketProcess.sendMsgToClient(MsgTypes.LoginReqMsg);
            }

            String[] msgFromClient = super.socketProcess.getMsgFromClient();
            if(msgFromClient != null){
                handleMsgFromClient(msgFromClient);
            }

            super.socketProcess.sleepWithExceptionHandle(500);
            cycleCounter++;
        }
    }

    public void handleMsgFromClient(String[] msgFromClient){
        LOGGER.fine("SocketProcessId: " + super.socketProcess.getSocketProcessId() +
                    " received: " + msgFromClient[0] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgFromClient[0]){
            case MsgTypes.LoginRespMsg:
                super.socketProcess.handleLoginRespMsg(msgFromClient);
                break;
            case MsgTypes.RegisterReqMsg:
                super.socketProcess.handleRegisterReqMsg(msgFromClient);
                break;
            case MsgTypes.ClientExitInd:
                IS_RUNNING = false;
                break;
            default:
                break;
        }
    }
}
