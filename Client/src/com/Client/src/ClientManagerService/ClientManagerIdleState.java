package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;

import java.util.logging.Logger;

public class ClientManagerIdleState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerIdleState(ClientManager clientManager) {
        super(clientManager);
        super.clientManager.printInterface("ClientManagerIdleState");
        LOGGER.fine("ClientManager set to: ClientManagerIdleState");
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("handle server msg: " +
                    msgFromServer[0] +
                    " in state: " +
                    "ClientManagerIdleState");
        try {
            switch (MsgTypes.valueOf(msgFromServer[0])) {
                case LoginReqMsg:
                    super.clientManager.setState(new ClientManagerLoginState(super.clientManager));
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
