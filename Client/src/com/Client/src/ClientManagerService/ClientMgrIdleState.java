package com.Client.src.ClientManagerService;

import com.Client.src.CC;
import com.Client.src.LoggerSingleton;
import com.Client.src.CMsgTypes;
import java.util.logging.Logger;

public class ClientMgrIdleState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrIdleState(ClientMgr clientMgr) {
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
                break;
            case CMsgTypes.ServerInfoMsg:
                System.out.println(msgFromServer[CC.MSG_CONTENT]);
                System.out.print('\n');
                break;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch (userInput) {
            case "login":
                clientMgr.handleLoginReq();
                break;
            case "register":
                clientMgr.handleRegisterReq();
                break;
            case "exit":
                clientMgr.sendMsgToServer(CMsgTypes.ClientExitInd);
                clientMgr.sleepWithExceptionHandle(500);
                System.exit(0);
            default:
                System.out.println("Incorrect command");
        }
    }
}
