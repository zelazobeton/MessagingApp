package com.Client.src.ClientManagerService;

import com.Client.src.CC;
import com.Client.src.CMsgTypes;
import com.Client.src.LoggerSingleton;
import java.util.logging.Logger;

public class ClientMgrUserCredentialsInputState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private boolean LOGIN_ENTERED = false;

    public ClientMgrUserCredentialsInputState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[CC.MSG_ID] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[CC.MSG_ID]) {
            case CMsgTypes.LoginSuccessInd:
                System.out.println("You are now logged in");
                clientMgr.setState(new ClientMgrLoggedInState(clientMgr));
                clientMgr.stringBuilder.setLength(0);
                break;
            case CMsgTypes.LoginFailInd:
                System.out.println("Login failed. Incorrect username or password\n");
                clientMgr.setState(new ClientMgrIdleState(clientMgr));
                break;
            case CMsgTypes.RegisterRespMsg:
                System.out.println(msgFromServer[CC.MSG_CONTENT]);
                clientMgr.setState(new ClientMgrIdleState(clientMgr));
                break;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        if (!LOGIN_ENTERED){
            clientMgr.handleUserLoginInput(userInput);
            LOGIN_ENTERED = true;
        }
        else{
            clientMgr.handleUserPwdInput(userInput);
        }
    }
}
