package com.Client.src.ClientManagerService;

import com.Client.src.LoggerSingleton;
import com.Client.src.MsgTypes;
import java.util.logging.Logger;

public class ClientMgrWaitForConversationStartResp extends IClientMgrState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public ClientMgrWaitForConversationStartResp(ClientMgr clientMgr) {
        super(clientMgr);
    }

    @Override
    protected void handleMsgFromServer(String[] msgFromServer){
        LOGGER.fine("Client received: " + msgFromServer[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgFromServer[0]) {
            case MsgTypes.ConvInitSuccessInd:
                super.clientMgr.setState(new ClientMgrConversationState(super.clientMgr));
                break;
            case MsgTypes.ConvInitFailInd:
                super.clientMgr.handleConvInitFailure(msgFromServer);
                super.clientMgr.setState(new ClientMgrLoggedInState(super.clientMgr));
                break;
            default:
                super.clientMgr.defaultLoggedClientMsgHandler(msgFromServer);
        }
    }

    @Override
    protected void handleUserInput(String userInput) {
        switch(userInput){
            default:
                System.out.println("Incorrect input");
        }
    }
}
