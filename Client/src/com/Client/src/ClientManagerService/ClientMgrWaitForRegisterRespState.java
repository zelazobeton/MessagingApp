package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientMgrWaitForRegisterRespState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrWaitForRegisterRespState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[0]) {
            case MsgTypes.RegisterSuccessInd:
                System.out.println("You have registered correctly. You can now log in.");
                super.clientMgr.setState(new ClientMgrIdleState(super.clientMgr));
                break;
            case MsgTypes.RegisterFailInd:
                System.out.println("User exists or something else went wrong");
                super.clientMgr.setState(new ClientMgrIdleState(super.clientMgr));
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch (userInput){
            default:
                System.out.println("Incorrect input");
        }
    }

}
