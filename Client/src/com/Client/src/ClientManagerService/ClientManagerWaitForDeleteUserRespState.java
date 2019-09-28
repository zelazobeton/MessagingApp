package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientManagerWaitForDeleteUserRespState extends IClientManagerState{
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerWaitForDeleteUserRespState(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        try {
            switch (msgFromServer[0]) {
                case MsgTypes.DeleteUserSuccessInd:
                    super.clientManager.setState(new ClientManagerIdleState(super.clientManager));
                    break;
                case MsgTypes.DeleteUserFailInd:
                    System.out.println("Something went wrong");
                    super.clientManager.setState(new ClientManagerLoggedInState(super.clientManager));
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
            default:
                System.out.println("Incorrect input");
        }
    }
}
