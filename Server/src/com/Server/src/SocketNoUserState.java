package com.Server.src;

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
        while(true)
        {
            super.socketProcess.sendMsgToClient("LoginReqMsg");
            super.socketProcess.sleepWithExceptionHandle(200);
            String[] msgInParts = super.socketProcess.getMsgFromClient();
            if(msgInParts != null){
                if(MsgTypes.valueOf(msgInParts[0]) == MsgTypes.LoginRespMsg){
                    if(true == super.socketProcess.verifyUser(msgInParts)){
                        super.socketProcess.sendMsgToClient("LoginSuccessInd");
                        super.socketProcess.createServerUserContextFromLoginRespMsg(msgInParts);
                        super.socketProcess.setState(new SocketLoggedIdleState(super.socketProcess));
                    }
                }
            }
            super.socketProcess.sleepWithExceptionHandle(10000);
        }
    }
}
