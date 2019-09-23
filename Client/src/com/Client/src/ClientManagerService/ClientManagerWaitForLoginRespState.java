package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;

import java.util.logging.Logger;

public class ClientManagerWaitForLoginRespState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerWaitForLoginRespState(ClientManager clientManager) {
        super(clientManager);
        LOGGER.fine("ClientManager set to: ClientManagerWaitForLoginRespState");
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("handle server msg: " +
                msgFromServer[0] +
                " in state: " +
                "ClientManagerWaitForLoginRespState");
        try {
            switch (MsgTypes.valueOf(msgFromServer[0])) {
                case LoginSuccessInd:
                    super.clientManager.setState(new ClientManagerLoggedInState(super.clientManager));
                    break;
                case LoginFailInd:
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
