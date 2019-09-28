package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientManagerLoggedInState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerLoggedInState(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        try {
            switch (msgFromServer[0]) {
                case MsgTypes.ConversationReqMsg:
                    break;
                default:
                    return;
            }
        }
        catch (IllegalArgumentException ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch (userInput){
            case "logout":
                super.clientManager.sendMsgToServer(MsgTypes.LogoutReqMsg);
                super.clientManager.setState(new ClientManagerWaitForLogoutRespState(super.clientManager));
                break;
            case "delete":
                super.clientManager.sendMsgToServer(MsgTypes.DeleteUserReqMsg);
                super.clientManager.setState(new ClientManagerWaitForDeleteUserRespState(super.clientManager));
                break;
            case "exit":
                super.clientManager.sendMsgToServer(MsgTypes.ClientExitInd);
                System.exit(0);
            default:
                System.out.println("Incorrect input");
        }
    }
}
