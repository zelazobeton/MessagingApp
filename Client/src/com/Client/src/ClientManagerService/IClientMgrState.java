package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
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

    public void run() {
        while(true){
            clientMgr.tryHandleUserInput();
            clientMgr.tryHandleMsgFromServer();
            handleCyclicalEvents();

            clientMgr.sleepWithExceptionHandle(500);
        }
    }

    private void handleCyclicalEvents(){
        if((cycleCounter % 80) == 0){
            clientMgr.sendMsgToServer(MsgTypes.ClientLiveConnectionInd);
        }
        cycleCounter++;
    }

    protected abstract void handleMsgFromServer(String[] msgFromServer);
    protected abstract void handleUserInput(String userInput);
}
