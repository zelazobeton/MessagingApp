package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientManagerWaitForLoginRespState extends IClientManagerState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientManagerWaitForLoginRespState(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        try {
            switch (msgFromServer[0]) {
                case MsgTypes.LoginSuccessInd:
                    System.out.println("You are now logged in");
                    super.clientManager.setState(new ClientManagerLoggedInState(super.clientManager));
                    break;
                case MsgTypes.LoginFailInd:
                    System.out.println("Incorrect username or password");
                    super.clientManager.setState(new ClientManagerIdleState(super.clientManager));
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
        return;
    }
}
