package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;

import java.util.logging.Logger;

public class ClientManagerLoggedInState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerLoggedInState(ClientManager clientManager) {
        super(clientManager);
        LOGGER.fine("ClientManager set to: ClientManagerLoggedInState");
        super.clientManager.printInterface("ClientManagerLoggedInState");
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("handle server msg: " +
                    msgFromServer[0] +
                    " in state: " +
                    "ClientManagerLoggedInState");
        try {
            switch (MsgTypes.valueOf(msgFromServer[0])) {
                case ConversationReqMsg:
                    break;
                default:
                    return;
            }
        }
        catch (IllegalArgumentException ex){
            LOGGER.warning(ex.toString());
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch (userInput){
            case "logout":
                super.clientManager.clientWriter.println("LogoutReqMsg");
                super.clientManager.setState(new ClientManagerWaitForLogoutRespState(super.clientManager));
                break;
            case "exit":
                super.clientManager.clientWriter.println("LogoutReqMsg");
                super.clientManager.clientWriter.println("ClientExitInd");
                LOGGER.fine("User exits program");
                System.exit(0);
            default:
                System.out.println("Incorrect input");
        }
    }
}