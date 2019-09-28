package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import java.util.logging.Logger;

public class ClientMgrConversationState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrConversationState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[0]) {
            default:
                return;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch(userInput){
            default:
                System.out.println("Incorrect input");
        }
        return;
    }
}
