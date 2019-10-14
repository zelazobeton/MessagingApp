package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientMgrLoggedInState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrLoggedInState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[0]) {
            case MsgTypes.Interface:
                super.clientMgr.printUserInterface(msgFromServer);
                break;
            case MsgTypes.LogoutInd:
                System.out.println(msgFromServer[1]);
                super.clientMgr.setState(new ClientMgrIdleState(super.clientMgr));
                break;
            case MsgTypes.ServerInfoMsg:
                System.out.print(msgFromServer[1]);
                break;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        super.clientMgr.sendMsgToServer(MsgTypes.ClientMsg + "_" + userInput);
    }
}
