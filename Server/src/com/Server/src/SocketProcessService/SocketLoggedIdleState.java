package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import java.util.logging.Logger;

public class SocketLoggedIdleState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketLoggedIdleState(SocketProcess socketProcess) {
        super(socketProcess);
    }

    @Override
    protected void handleMsg(String[] msgFromClient){
        LOGGER.fine("SocketProcessId: " + super.socketProcess.getSocketProcessId() +
                    " handle: " + msgFromClient[0] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgFromClient[0]){
            case MsgTypes.LogoutReqMsg:
                super.socketProcess.logoutUser();
                super.socketProcess.sendMsgToClient(MsgTypes.LogoutRespMsg);
                super.socketProcess.setState(new SocketNoUserState(super.socketProcess));
                break;
            case MsgTypes.ClientExitInd:
                super.socketProcess.logoutUser();
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
