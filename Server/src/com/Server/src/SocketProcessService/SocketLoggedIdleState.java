package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import java.util.logging.Logger;

public class SocketLoggedIdleState extends SocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketLoggedIdleState(SocketProcess socketProcess) {
        super(socketProcess);
    }

    @Override
    public void run() {
        while(IS_RUNNING){
//            LOGGER.fine("Running SocketLoggedIdleState");

            String[] msgFromClient = super.socketProcess.getMsgFromClient();
            if(msgFromClient != null){
                handleMsgFromClient(msgFromClient);
            }

            super.socketProcess.sleepWithExceptionHandle(500);
        }
    }

    public void handleMsgFromClient(String[] msgFromClient){
        LOGGER.fine("SocketProcessId: " + super.socketProcess.getSocketProcessId() +
                    " received: " + msgFromClient[0] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgFromClient[0]){
            case MsgTypes.LogoutReqMsg:
                super.socketProcess.logoutUser();
                super.socketProcess.sendMsgToClient(MsgTypes.LogoutRespMsg);
                super.socketProcess.setState(new SocketNoUserState(super.socketProcess));
                break;
            case MsgTypes.ClientExitInd:
                IS_RUNNING = false;
                break;
            case MsgTypes.DeleteUserReqMsg:
                super.socketProcess.handleDeleteUserReq();
                break;
            default:
                break;
        }
    }
}
