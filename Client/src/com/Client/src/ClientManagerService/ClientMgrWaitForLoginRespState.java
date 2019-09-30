package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientMgrWaitForLoginRespState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrWaitForLoginRespState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[0]) {
            case MsgTypes.LoginSuccessInd:
                System.out.println("You are now logged in");
                super.clientMgr.setState(new ClientMgrLoggedInState(super.clientMgr));
                break;
            case MsgTypes.LoginFailInd:
                System.out.println("Incorrect username or password");
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
