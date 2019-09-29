package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientMgrWaitForLogoutRespState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrWaitForLogoutRespState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[0] +
                    " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[0]) {
            case MsgTypes.LogoutRespMsg:
                System.out.println("You have successfully logged out");
                super.clientMgr.setState(new ClientMgrIdleState(super.clientMgr));
                break;
            default:
                return;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        return;
    }
}