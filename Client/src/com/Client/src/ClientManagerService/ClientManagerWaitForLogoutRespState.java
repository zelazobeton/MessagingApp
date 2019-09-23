package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientManagerWaitForLogoutRespState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerWaitForLogoutRespState(ClientManager clientManager) {
        super(clientManager);
        LOGGER.fine("ClientManager set to: ClientManagerWaitForLogoutRespState");
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("handle server msg: " +
                msgFromServer[0] +
                " in state: " +
                "ClientManagerWaitForLogoutRespState");
        try {
            switch (MsgTypes.valueOf(msgFromServer[0])) {
                case LogoutRespMsg:
                    System.out.println("You have successfully logged out");
                    super.clientManager.setState(new ClientManagerIdleState(super.clientManager));
                    break;
                default:
                    return;
            }
        }
        catch (IllegalArgumentException ex){
            LOGGER.warning(ex.toString());
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        return;
    }
}
