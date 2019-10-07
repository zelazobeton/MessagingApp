package com.Server.src;

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
    private ServerSocket SERVER_SOCKET;

    private StringBuilder stringBuilder;
    private Map<Integer, Integer> loggedUsersMap;
    private Map<Integer, ArrayBlockingQueue<String>> messageQueuesMap;
    private Map<Integer, Thread> socketProcessThreadMap;
    private ArrayBlockingQueue<String> mainMsgQueue;

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.numOfClientConnectionsCreated = 0;
        this.SERVER_SOCKET = null;

        this.stringBuilder = new StringBuilder();
        this.socketProcessThreadMap = new HashMap<>();
        this.messageQueuesMap = new HashMap<>();
        this.loggedUsersMap = new HashMap<>();
        this.mainMsgQueue = new ArrayBlockingQueue<>(50);
    }

    public void run() {
        if(!(prepareServerSocket() && openDatabase())){
            LOGGER.fine("Socket manager finished run after preparation failure");
            return;
        }

        while(true){
            tryOpenNewClientConn();
            tryHandleNextMsgFromMainQueue();
            sleepWithExceptionHandle(1000);
        }
    }

    public void tryHandleNextMsgFromMainQueue(){
        String[] msgFromMainQueueInParts = tryGetNextMsgFromMainQueue();
        if(msgFromMainQueueInParts == null) {
            return;
        }
        LOGGER.fine("SocketManager handle: " + msgFromMainQueueInParts[0]);
        switch (msgFromMainQueueInParts[0]) {
            case MsgTypes.IntSocketProcessExit:
                handleIntSocketProcessExit(msgFromMainQueueInParts);
                break;
            case MsgTypes.IntConvInitReqMsg:
            case MsgTypes.IntCancelProcMsg:
                routeMsgByUserId(msgFromMainQueueInParts);
                break;
            case MsgTypes.IntConvInitRespMsg:
            case MsgTypes.IntConvFinishInd:
            case MsgTypes.IntConvUserMsg:
                routeMsgBySocketProcessId(msgFromMainQueueInParts);
                break;
            default:
                return;
        }
    }

    public String[] tryGetNextMsgFromMainQueue(){
        if(!mainMsgQueue.isEmpty()){
            try{
                return mainMsgQueue.take().split("_");
            }
            catch (InterruptedException ex){
                LOGGER.warning("Exception thrown while tryGetNextMsgFromMainQueue: " + ex.toString());
            }
        }
        return null;
    }

    private boolean prepareServerSocket() {
        try{
            SERVER_SOCKET = new ServerSocket(CONNECTION_PORT);
            SERVER_SOCKET.setSoTimeout(2000);
            return true;
        }
        catch (Exception ex){
            LOGGER.warning("Error while preparing SERVER_SOCKET: " + ex.toString());
            closeSocket();
            return false;
        }
    }

    private void closeSocket(){
        if(SERVER_SOCKET == null){
            return;
        }
        try{
            SERVER_SOCKET.close();
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
            /*LOGGING_CLEAR*/
//            LOGGER.fine("Try open new client connection");
            Socket clientSocket = SERVER_SOCKET.accept();
            createAndRunNewSocketProcess(clientSocket);
        }
        catch (IOException ex){
            /*LOGGING_CLEAR*/
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

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
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

    private void routeMsgByUserId(String[] recMsg){
        Integer toUserId = Integer.parseInt(recMsg[1]);
        Integer respUserSocketId = loggedUsersMap.get(toUserId);
        if(respUserSocketId == null){
            LOGGER.fine("Main queue routing fail: User not logged");
            SendIntRouteFailInd(Integer.parseInt(recMsg[3]), ConvInitStatus.UserNotLogged);
            return;
        }
        recMsg[2] = String.valueOf(respUserSocketId);
        routeMsgBySocketProcessId(recMsg);
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
        LOGGER.fine("Main queue error handling fail");
    }

    private void routeMsgBySocketProcessId(String[] recMsg){
        ArrayBlockingQueue<String> respUserSocketProcessMsgQueue =
                messageQueuesMap.get(Integer.parseInt(recMsg[2]));
        if(respUserSocketProcessMsgQueue == null){
            LOGGER.fine("Main queue fail routing msg: " + recMsg[0] +
                        " from socketId: " + recMsg[4] +
                        " to socketId: " + recMsg[2] +
                        " no such queue in messageQueuesMap");
            SendIntRouteFailInd(Integer.parseInt(recMsg[4]), ConvInitStatus.Unspecified);
            return;
        }
        try{
            respUserSocketProcessMsgQueue.put(joinMsgInParts(recMsg));
        }
        catch (InterruptedException ex){
            LOGGER.warning("Main queue exception while routing msg: " + recMsg[0] +
                            " from socketId: " + recMsg[4] +
                            " to socketId: " + recMsg[2] +
                            ex.toString());
            SendIntRouteFailInd(Integer.parseInt(recMsg[4]), ConvInitStatus.Unspecified);
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
