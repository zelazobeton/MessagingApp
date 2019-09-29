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
    private ServerSocket SERVER_SOCKET;
    private int numOfSocketsCreated;
    private DbHandler dbHandler;

    private Map<Integer, Integer> loggedUsersIdsMap;
    private Map<Integer, ArrayBlockingQueue<String>> messageQueuesMap;
    private Map<Integer, Thread> socketProcessThreadMap;
    private ArrayBlockingQueue<String> mainMsgQueue;

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.SERVER_SOCKET = null;
        this.numOfSocketsCreated = 0;

        this.socketProcessThreadMap = new HashMap<>();
        this.messageQueuesMap = new HashMap<>();
        this.loggedUsersIdsMap = new HashMap<>();
        this.mainMsgQueue = new ArrayBlockingQueue<>(50);
    }

    public void run() {
        if(!(prepareServerSocket() && openDatabase())) {
            LOGGER.fine("Socket manager finished run after preparation failure");
            return;
        }

        while(true){
            try {
                tryOpenNewSocketForClient();
                sleepWithExceptionHandle(1000);
            }
            catch (IOException ex){
                LOGGER.warning(ex.toString());
            }
        }
    }

    private boolean prepareServerSocket(){
        try{
            openServerSocket();
            return true;
        }
        catch (Exception ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
            closeSocket();
            dbHandler.closeConnection();
            return false;
        }
    }

    private void openServerSocket() throws IOException{
        SERVER_SOCKET = new ServerSocket(CONNECTION_PORT);
        SERVER_SOCKET.setSoTimeout(5000);
    }

    private boolean openDatabase() {
        this.dbHandler = new DbHandler();
        return dbHandler.open();
    }

    private void tryOpenNewSocketForClient() throws IOException{
        LOGGER.fine("Try open new socket for client");
        Socket socket = SERVER_SOCKET.accept();
        LOGGER.fine("New socket accepted");
        BufferedReader inputBufferedReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter outputPrintWriter = new PrintWriter(socket.getOutputStream(), true);

        ArrayBlockingQueue<String> socketProcessMsgQueue = new ArrayBlockingQueue<>(30);
        Thread newSocketProcess = new Thread(new SocketProcess(dbHandler,
                                                               inputBufferedReader,
                                                               outputPrintWriter,
                                                               socket,
                                                               numOfSocketsCreated,
                                                               socketProcessMsgQueue,
                                                               mainMsgQueue));

        socketProcessThreadMap.put(numOfSocketsCreated, newSocketProcess);
        messageQueuesMap.put(numOfSocketsCreated, socketProcessMsgQueue);
        mainMsgQueue = new ArrayBlockingQueue<>(50);
        numOfSocketsCreated++;
        newSocketProcess.run();
    }

    private void closeSocket(){
        try{
            this.SERVER_SOCKET.close();
            LOGGER.fine("SERVER_SOCKET closed");
        } catch (Exception ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
        }
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("sleepWithExceptionHandle interrupted: " + ex.toString());
            ex.printStackTrace();
        }
    }
}
