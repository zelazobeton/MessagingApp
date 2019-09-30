package com.Server.src.SocketProcessService;

import com.Server.src.LoggerSingleton;
import com.Server.src.MsgTypes;
import java.util.logging.Logger;

public class SocketWaitForConvAcceptState extends ISocketProcessState {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;

    public SocketWaitForConvAcceptState(SocketProcess socketProcess) {
        super(socketProcess);
    }

    @Override
    protected void handleMsgFromSocketProcessQueue(String[] msgInParts){
        LOGGER.fine("SocketProcess: " + super.socketProcess.getSocketProcessId() +
                " handle: " + msgInParts[0] +
                " in state " + this.getClass().getSimpleName());
        switch (msgInParts[0]){
            case MsgTypes.IntConvInitReqMsg:
                super.socketProcess.ignoreIntConvInitReqMsg(msgInParts);
                break;
            case MsgTypes.IntConvInitRespMsg:
                super.socketProcess.handleIntConvInitRespMsg(msgInParts);
                break;
            case MsgTypes.IntRouteFailInd:
                super.socketProcess.sendMsgToClient(MsgTypes.ConvInitFailInd + "_" + msgInParts[1]);
                break;
            default:
                defaultMsgHandler(msgInParts);
                break;
        }
    }
}
