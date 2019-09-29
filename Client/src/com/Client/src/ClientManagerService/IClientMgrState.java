package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import java.util.logging.Logger;

public abstract class IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    protected ClientMgr clientMgr;
    private Integer cycleCounter;

    public IClientMgrState(ClientMgr clientMgr) {
        this.clientMgr = clientMgr;
        this.cycleCounter = 0;
        LOGGER.fine("ClientManager set to " + this.getClass().getSimpleName());
        clientMgr.printInterface(this.getClass().getSimpleName());
    }

    protected abstract void handleMsgFromServer(String[] msgFromServer);
    protected abstract void handleUserInput(String userInput);
}
