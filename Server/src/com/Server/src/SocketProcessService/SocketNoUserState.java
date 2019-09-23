package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;

import java.util.logging.Logger;

public class SocketNoUserState extends SocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketNoUserState(SocketProcess socketProcess) {
        super(socketProcess);
        LOGGER.fine("SocketProcessId: " +
                    super.socketProcess.getSocketProcessId() +
                    " set to SocketNoUserState");
    }

    @Override
    public void run() {
        int cycleCounter = 0;
        while(IS_RUNNING)
        {
            if((cycleCounter % 10) == 0){
                super.socketProcess.sendMsgToClient("LoginReqMsg");
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
        switch (MsgTypes.valueOf(msgFromClient[0])){
            case LoginRespMsg:
                super.socketProcess.handleLoginRespMsg(msgFromClient);
                break;
            case RegisterReqMsg:
                super.socketProcess.handleRegisterReqMsg(msgFromClient);
                break;
            case ClientExitInd:
                IS_RUNNING = false;
                break;
            default:
                break;
        }
    }
}
