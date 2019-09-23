package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;

import java.util.logging.Logger;

public class ClientManagerWaitForRegisterRespState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerWaitForRegisterRespState(ClientManager clientManager) {
        super(clientManager);
        LOGGER.fine("ClientManager set to: ClientManagerWaitForRegisterRespState");
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("handle server msg: " +
                msgFromServer[0] +
                " in state: " +
                "ClientManagerWaitForRegisterRespState");
        try {
            switch (MsgTypes.valueOf(msgFromServer[0])) {
                case RegisterSuccessInd:
                    System.out.println("You have registered correctly. You can now log in.");
                case RegisterFailInd:
                    System.out.println("User exists or something else went wrong");
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
