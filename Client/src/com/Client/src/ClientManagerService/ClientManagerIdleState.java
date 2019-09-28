package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientManagerIdleState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerIdleState(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        try {
            switch (msgFromServer[0]) {
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
                super.clientManager.sendMsgToServer(MsgTypes.ClientExitInd);
                super.clientManager.sleepWithExceptionHandle(500);
                System.exit(0);
            default:
                System.out.println("Incorrect input");
        }
        return;
    }
}
