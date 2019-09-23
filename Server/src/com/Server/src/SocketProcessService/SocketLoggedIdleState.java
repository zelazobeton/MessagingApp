package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;

import java.util.logging.Logger;

public class SocketLoggedIdleState extends SocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketLoggedIdleState(SocketProcess socketProcess) {
        super(socketProcess);
        LOGGER.fine("SocketProcessId: " +
                super.socketProcess.getSocketProcessId() +
                " set to SocketLoggedIdleState");
    }

    @Override
    public void run() {
        while(IS_RUNNING){
            LOGGER.fine("Running SocketLoggedIdleState");

            String[] msgFromClient = super.socketProcess.getMsgFromClient();
            if(msgFromClient != null){
                handleMsgFromClient(msgFromClient);
            }

            super.socketProcess.sleepWithExceptionHandle(500);
        }
    }

    public void handleMsgFromClient(String[] msgFromClient){
        switch (MsgTypes.valueOf(msgFromClient[0])){
            case LogoutReqMsg:
                super.socketProcess.logoutUser();
                super.socketProcess.sendMsgToClient("LogoutRespMsg");
                super.socketProcess.setState(new SocketNoUserState(super.socketProcess));
                break;
            case DeleteUserReqMsg:
                // TODO
                break;
            default:
                break;
        }
    }
}
