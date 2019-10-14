package com.Server.src;

import com.Server.src.Constants.CC;
import com.Server.src.Constants.ConvInitStatus;
import com.Server.src.Constants.MsgTypes;
import com.Server.src.SocketProcessService.SocketProcess;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class SocketManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private Integer CONNECTION_PORT;
    private int numOfClientConnectionsCreated;
    private DbHandler dbHandler;
    private ServerSocket serverSocket;

    private StringBuilder stringBuilder;
    private Map<Integer, Integer> loggedUsersMap;
    private Map<Integer, ArrayBlockingQueue<String>> messageQueuesMap;
    private Map<Integer, Thread> socketProcessThreadMap;
    private ArrayBlockingQueue<String> mainMsgQueue;

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.numOfClientConnectionsCreated = 0;
        this.serverSocket = null;

        this.stringBuilder = new StringBuilder();
        this.socketProcessThreadMap = new HashMap<>();
        this.messageQueuesMap = new HashMap<>();
        this.loggedUsersMap = new HashMap<>();
        this.mainMsgQueue = new ArrayBlockingQueue<>(50);
    }

    void run() {
        if(!(prepareServerSocket() && openDatabase())){
            LOGGER.fine("Socket manager finished after preparation failure");
            return;
        }

        while(true){
            tryOpenNewClientConn();
            tryHandleNextMsgFromMainQueue();
            sleepWithExceptionHandle();
        }
    }

    private void tryHandleNextMsgFromMainQueue(){
        String[] mainQueueMsg = tryGetNextMsgFromMainQueue();
        if(mainQueueMsg == null) {
            return;
        }
        LOGGER.fine("SocketManager handle: " + mainQueueMsg[CC.MSG_ID]);
        switch (mainQueueMsg[CC.MSG_ID]) {
            case MsgTypes.IntSocketProcessExit:
                handleIntSocketProcessExit(mainQueueMsg);
                break;
            case MsgTypes.IntConvInitReqMsg:
            case MsgTypes.IntCancelProcMsg:
                routeMsgByUserId(mainQueueMsg);
                break;
            case MsgTypes.IntConvInitRespMsg:
            case MsgTypes.IntConvFinishInd:
            case MsgTypes.IntConvUserMsg:
                routeMsgBySocketProcessId(mainQueueMsg);
                break;
        }
    }

    private String[] tryGetNextMsgFromMainQueue(){
        if(!mainMsgQueue.isEmpty()){
            try{
                return mainMsgQueue.take().split("_");
            }
            catch (InterruptedException ex){
                LOGGER.warning("Exception thrown while getting msg from main queue: " + ex.toString());
            }
        }
        return null;
    }

    private boolean prepareServerSocket() {
        try{
            serverSocket = new ServerSocket(CONNECTION_PORT);
            serverSocket.setSoTimeout(2000);
            return true;
        }
        catch (Exception ex){
            LOGGER.warning("Error while preparing SERVER_SOCKET: " + ex.toString());
            closeSocket();
            return false;
        }
    }

    private void closeSocket(){
        if(serverSocket == null){
            return;
        }
        try{
            serverSocket.close();
        }
        catch (Exception ex){
            LOGGER.warning("Error while closing SERVER_SOCKET: " + ex.toString());
        }
    }

    private boolean openDatabase() {
        this.dbHandler = new DbHandler();
        return dbHandler.open();
    }

    private void tryOpenNewClientConn() {
        try{
            Socket clientSocket = serverSocket.accept();
            createAndRunNewSocketProcess(clientSocket);
        }
        catch (IOException ex){
//            LOGGER.warning("Error while opening new client connection: " + ex.getMessage());
        }
    }

    private void createAndRunNewSocketProcess(Socket clientSocket) throws IOException{
        BufferedReader inputBufferedReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter outputPrintWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        ArrayBlockingQueue<String> socketProcessMsgQueue = new ArrayBlockingQueue<>(30);
        int newSocketProcessId = numOfClientConnectionsCreated;
        Thread newSocketProcess = new Thread(new SocketProcess(dbHandler,
                                                               inputBufferedReader,
                                                               outputPrintWriter,
                                                               clientSocket,
                                                               newSocketProcessId,
                                                               loggedUsersMap,
                                                               socketProcessMsgQueue,
                                                               mainMsgQueue));

        socketProcessThreadMap.put(newSocketProcessId, newSocketProcess);
        messageQueuesMap.put(newSocketProcessId, socketProcessMsgQueue);
        numOfClientConnectionsCreated++;
        newSocketProcess.start();
    }

    private void sleepWithExceptionHandle(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            LOGGER.info("sleepWithExceptionHandle interrupted: " + ex.toString());
            ex.printStackTrace();
        }
    }

    private void handleIntSocketProcessExit(String[] msgFromMainQueueInParts){
        Integer socketProcessToFinishId = Integer.parseInt(msgFromMainQueueInParts[1]);
        Thread socketProcessThreadToFinish = socketProcessThreadMap.get(socketProcessToFinishId);
        if(socketProcessThreadToFinish == null){
            LOGGER.warning("Error while exiting SocketProcess: " +
                            socketProcessToFinishId +
                            " Thread not found in socketProcessThreadMap");
            return;
        }
        try{
            socketProcessThreadToFinish.join();
        }
        catch (InterruptedException ex){
            LOGGER.warning("Error while exiting SocketProcess: " +
                            socketProcessToFinishId +
                            " Could not join");
        }
        socketProcessThreadMap.remove(socketProcessToFinishId);
        messageQueuesMap.remove(socketProcessToFinishId);
        LOGGER.fine("SocketProcess: " + socketProcessToFinishId + " removed successfully");
    }

    private void routeMsgByUserId(String[] msg){
        Integer toUserId = Integer.parseInt(msg[CC.TO_USER_ID]);
        Integer toUserSocketId = loggedUsersMap.get(toUserId);
        if(toUserSocketId == null){
            LOGGER.fine("Main queue routing fail: Requested user not logged");
            SendIntRouteFailInd(Integer.parseInt(msg[CC.FROM_USER_ID]), ConvInitStatus.UserNotLogged);
            return;
        }
        msg[CC.TO_USER_SOCKET_ID] = String.valueOf(toUserSocketId);
        routeMsgBySocketProcessId(msg);
    }

    private void SendIntRouteFailInd(Integer toUserSocketId, String reason){
        String msg = MsgTypes.IntRouteFailInd + "_" + reason;
        ArrayBlockingQueue<String> toUserSocketProcessMsgQueue = messageQueuesMap.get(toUserSocketId);
        if(toUserSocketProcessMsgQueue != null){
            try{
                toUserSocketProcessMsgQueue.put(msg);
                return;
            }
            catch (InterruptedException ex){
                LOGGER.warning(ex.toString());
            }
        }
        LOGGER.warning("Main queue exception handling error");
    }

    private void routeMsgBySocketProcessId(String[] msg){
        ArrayBlockingQueue<String> toUserSocketProcessMsgQueue =
                messageQueuesMap.get(Integer.parseInt(msg[CC.TO_USER_SOCKET_ID]));
        if(toUserSocketProcessMsgQueue == null){
            LOGGER.fine("Error while routing msg: " + msg[CC.MSG_ID] +
                        " from socketId: " + msg[CC.FROM_USER_SOCKET_ID] +
                        " to socketId: " + msg[CC.TO_USER_SOCKET_ID] +
                        " no such queue in messageQueuesMap");
            SendIntRouteFailInd(Integer.parseInt(msg[CC.FROM_USER_SOCKET_ID]), ConvInitStatus.Unspecified);
            return;
        }
        try{
            toUserSocketProcessMsgQueue.put(joinMsgInParts(msg));
        }
        catch (InterruptedException ex){
            LOGGER.warning("Error while routing msg: " + msg[CC.MSG_ID] +
                            " from socketId: " + msg[CC.FROM_USER_SOCKET_ID] +
                            " to socketId: " + msg[CC.TO_USER_SOCKET_ID] +
                            ex.toString());
            SendIntRouteFailInd(Integer.parseInt(msg[CC.FROM_USER_SOCKET_ID]), ConvInitStatus.Unspecified);
        }
    }

    private String joinMsgInParts(String[] msgInParts){
        for(String element : msgInParts){
            stringBuilder.append(element);
            stringBuilder.append("_");
        }
        String joinedMsg = stringBuilder.toString();
        stringBuilder.setLength(0);
        return joinedMsg;
    }
}
