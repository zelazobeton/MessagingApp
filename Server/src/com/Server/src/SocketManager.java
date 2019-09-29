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

    private Map<Integer, Integer> loggedUsersIdsMap;
    private Map<Integer, ArrayBlockingQueue<String>> messageQueuesMap;
    private Map<Integer, Thread> socketProcessThreadMap;
    private ArrayBlockingQueue<String> mainMsgQueue;

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.numOfClientConnectionsCreated = 0;
        this.SERVER_SOCKET = null;

        this.socketProcessThreadMap = new HashMap<>();
        this.messageQueuesMap = new HashMap<>();
        this.loggedUsersIdsMap = new HashMap<>();
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
        String[] msgFromMainQueue = tryGetNextMsgFromMainQueue();
        if(msgFromMainQueue == null) {
            return;
        }
        LOGGER.fine("SocketManager handle: " + msgFromMainQueue[0]);
        switch (msgFromMainQueue[0]) {
            case MsgTypes.SocketProcessExit:
                handleSocketProcessExit(msgFromMainQueue);
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
                                                               socketProcessMsgQueue,
                                                               mainMsgQueue));


        socketProcessThreadMap.put(newSocketProcessId, newSocketProcess);
        messageQueuesMap.put(newSocketProcessId, socketProcessMsgQueue);
        numOfClientConnectionsCreated++;
        newSocketProcess.start();
        LOGGER.fine("SocketProcess: " + newSocketProcessId + " created successfully");
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("sleepWithExceptionHandle interrupted: " + ex.toString());
            ex.printStackTrace();
        }
    }

    private void handleSocketProcessExit(String[] msgFromMainQueue){
        Integer socketProcessToFinishId = Integer.parseInt(msgFromMainQueue[1]);
        Thread socketProcessThreadToFinish = socketProcessThreadMap.get(socketProcessToFinishId);
        try{
            socketProcessThreadToFinish.join();
        }
        catch (InterruptedException ex){
            LOGGER.warning("Error while exiting SocketProcess: " +
                            socketProcessToFinishId +
                            "Could not join");
        }
        socketProcessThreadMap.remove(socketProcessToFinishId);
        messageQueuesMap.remove(socketProcessToFinishId);
        LOGGER.fine("SocketProcess: " + socketProcessToFinishId + " removed successfully");
    }
}
