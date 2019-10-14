package com.Client.src.ClientManagerService;

import com.Client.src.CC;
import com.Client.src.LoggerSingleton;
import com.Client.src.CMsgTypes;
import java.util.logging.Logger;

public class ClientMgrLoggedInState extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrLoggedInState(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer) {
        LOGGER.fine("Client received: " + msgFromServer[CC.MSG_ID] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[CC.MSG_ID]) {
            case CMsgTypes.Interface:
                clientMgr.printServerMsg(msgFromServer);
                break;
            case CMsgTypes.LogoutInd:
                System.out.println(msgFromServer[CC.MSG_CONTENT]);
                clientMgr.setState(new ClientMgrIdleState(clientMgr));
                break;
            case CMsgTypes.ServerInfoMsg:
                System.out.print(msgFromServer[CC.MSG_CONTENT]);
                System.out.print('\n');
                break;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        clientMgr.sendMsgToServer(CMsgTypes.ClientMsg + "_" + userInput);
    }
}
