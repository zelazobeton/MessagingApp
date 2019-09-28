package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import java.util.logging.Logger;

public abstract class IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    protected ClientManager clientManager;

    public IClientManagerState(ClientManager clientManager) {
        this.clientManager = clientManager;
        LOGGER.fine("ClientManager set to " + this.getClass().getSimpleName());
        clientManager.printInterface(this.getClass().getSimpleName());
    }

    public void run() {
        String userInput;
        while(true){
            while ((userInput = clientManager.tryGetUserInput()) != null){
                handleUserInput(userInput);
            }

            String[] msgFromServer = clientManager.tryGetServerMsg();
            if(msgFromServer != null){
                handleMsgFromServer(msgFromServer);
            }

            clientManager.sleepWithExceptionHandle(500);
        }
    }

    protected abstract void handleMsgFromServer(String[] msgFromServer);
    protected abstract void handleUserInput(String userInput);
}
