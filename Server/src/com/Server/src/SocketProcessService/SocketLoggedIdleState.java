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
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                    " handle: " + msgInParts[0] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgInParts[0]){
            case MsgTypes.ConvInitReqMsg:
                super.socketProcess.handleConvInitReq(msgInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case MsgTypes.LogoutReqMsg:
                super.socketProcess.logoutUser();
                super.socketProcess.sendMsgToClient(MsgTypes.LogoutInd);
                super.socketProcess.setState(new SocketNoUserState(super.socketProcess));
                break;
            case MsgTypes.ClientExitInd:
                super.socketProcess.logoutUser();
                socketProcess.finishSocketProcess();
                break;
            case MsgTypes.DeleteUserReqMsg:
                super.socketProcess.handleDeleteUserReq();
                break;
            default:
                defaultMsgHandler(msgInParts);
                break;
        }
    }
}
