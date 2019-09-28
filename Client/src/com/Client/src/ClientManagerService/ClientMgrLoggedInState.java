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
            case MsgTypes.ConversationReqMsg:
                break;
            default:
                return;
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch (userInput){
            case "start":
                super.clientMgr.startConversation();
                super.clientMgr.setState(new ClientMgrWaitForConversationStartResp(super.clientMgr));
                break;
            case "logout":
                super.clientMgr.sendMsgToServer(MsgTypes.LogoutReqMsg);
                super.clientMgr.setState(new ClientMgrWaitForLogoutRespState(super.clientMgr));
                break;
            case "delete":
                super.clientMgr.sendMsgToServer(MsgTypes.DeleteUserReqMsg);
                super.clientMgr.setState(new ClientMgrWaitForDeleteUserRespState(super.clientMgr));
                break;
            case "exit":
                super.clientMgr.sendMsgToServer(MsgTypes.ClientExitInd);
                System.exit(0);
            default:
                System.out.println("Incorrect input");
        }
    }
}
