package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;

import java.util.logging.Logger;

public class ClientManagerLoginState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerLoginState(ClientManager clientManager) {
        super(clientManager);
        LOGGER.fine("ClientManager set to: ClientManagerLoginState");
        super.clientManager.runUserInputThread();
        super.clientManager.printInterface("ClientManagerLoginState");
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("handle server msg: " +
                msgFromServer[0] +
                " in state: " +
                "ClientManagerLoginState");
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
        clientManager.stringBuilder.append("LoginRespMsg_");
        clientManager.stringBuilder.append(userInput);
        clientManager.stringBuilder.append("_");

        while ((userInput = clientManager.tryGetUserInput()) != null) {}
        System.out.println("Enter password: ");

        while ((userInput = clientManager.tryGetUserInput()) == null) {}

        clientManager.stringBuilder.append(userInput);
        clientManager.clientWriter.println(clientManager.stringBuilder.toString());
        clientManager.stringBuilder.setLength(0);
        super.clientManager.setState(new ClientManagerWaitForLoginRespState(super.clientManager));
    }
}
