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
        switch(userInput){
            case "login":
                super.clientManager.prepareAndSendLoginRespMsg();
                super.clientManager.setState(new ClientManagerWaitForLoginRespState(super.clientManager));
                break;
            case "register":
                super.clientManager.prepareAndSendRegisterReqMsg();
                super.clientManager.setState(new ClientManagerWaitForRegisterRespState(super.clientManager));
                break;
            case "exit":
                LOGGER.fine("User exits program");
                super.clientManager.clientWriter.println("ClientExitInd");
                super.clientManager.sleepWithExceptionHandle(500);
                System.exit(0);
            default:
                System.out.println("Incorrect input");
        }
        return;
    }
}
