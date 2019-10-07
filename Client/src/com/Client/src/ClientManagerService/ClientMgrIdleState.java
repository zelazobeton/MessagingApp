package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientMgrIdleState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrIdleState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[0]) {
            case MsgTypes.LoginSuccessInd:
                System.out.println("You are now logged in");
                super.clientMgr.setState(new ClientMgrLoggedInState(super.clientMgr));
                break;
            case MsgTypes.ServerInfoMsg:
                System.out.println(msgFromServer[1]);
                break;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch (userInput) {
            case "login":
                super.clientMgr.prepareAndSendLoginRespMsg();
                break;
            case "register":
                super.clientMgr.prepareAndSendRegisterReqMsg();
                break;
            case "exit":
                super.clientMgr.sendMsgToServer(MsgTypes.ClientExitInd);
                super.clientMgr.sleepWithExceptionHandle(500);
                System.exit(0);
            default:
                System.out.println("Incorrect command");
        }
    }
}
