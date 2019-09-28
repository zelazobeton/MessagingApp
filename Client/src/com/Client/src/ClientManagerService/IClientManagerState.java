package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public abstract class IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    protected ClientManager clientManager;
    private Integer cycleCounter;

    public IClientManagerState(ClientManager clientManager) {
        this.clientManager = clientManager;
        this.cycleCounter = 0;
        LOGGER.fine("ClientManager set to " + this.getClass().getSimpleName());
        clientManager.printInterface(this.getClass().getSimpleName());
    }

    public void run() {
        while(true){
            clientManager.tryHandleUserInput();
            clientManager.tryHandleMsgFromServer();
            handleCyclicalEvents();


            clientManager.sleepWithExceptionHandle(500);
        }
    }

    private void handleCyclicalEvents(){
        if((cycleCounter % 5) == 0){
            clientManager.sendMsgToServer(MsgTypes.ClientLiveConnectionInd);
        }
        cycleCounter++;
    }

    protected abstract void handleMsgFromServer(String[] msgFromServer);
    protected abstract void handleUserInput(String userInput);
}
