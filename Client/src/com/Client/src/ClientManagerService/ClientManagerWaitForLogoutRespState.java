package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientManagerWaitForLogoutRespState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerWaitForLogoutRespState(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[0] +
                    " in state " + this.getClass().getSimpleName());
        try {
            switch (msgFromServer[0]) {
                case MsgTypes.LogoutRespMsg:
                    System.out.println("You have successfully logged out");
                    super.clientManager.setState(new ClientManagerIdleState(super.clientManager));
                    break;
                default:
                    return;
            }
        }
        catch (IllegalArgumentException ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        return;
    }
}
