package com.Server.src;

import com.Server.src.SocketProcessService.SocketProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

public class SocketManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private List<SocketProcess> socketProcesses;
    private Queue<SocketProcess> contextsToVerify;
    private ServerSocket serverSocket;
    private Integer CONNECTION_PORT;
    private Integer numOfSocketsCreated;
    private DbHandler dbHandler;

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        socketProcesses = new ArrayList<>();
        contextsToVerify = new ArrayDeque<>();
        serverSocket = null;
        numOfSocketsCreated = 0;
    }

    public void run() {
        try{
            openServerSocket();
            openDatabase();
            while(true){
                try {
                    openNewSocketForWaitingClient();
                    sleepWithExceptionHandle(1000);
                }
                catch (IOException ex){
                    LOGGER.warning(ex.toString());
                }
            }
        }
        catch (Exception ex){
            LOGGER.warning(ex.getMessage());
            closeSocket();
            dbHandler.closeConnection();
        }
    }

    private void openServerSocket() throws IOException{
        serverSocket = new ServerSocket(CONNECTION_PORT);
        serverSocket.setSoTimeout(5000);
    }

    private void openDatabase() throws Exception {
        this.dbHandler = new DbHandler();
        if(!dbHandler.open()){
            throw new Exception("Could not open database");
        }
    }



    private void openNewSocketForWaitingClient() throws IOException{
        LOGGER.fine("openNewSocketForWaitingClient");
        Socket socket = serverSocket.accept();
        LOGGER.fine("ServerSocket accepted");
        BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        SocketProcess socketProcess = new SocketProcess(dbHandler, input, output, socket, numOfSocketsCreated);
        numOfSocketsCreated++;
        socketProcesses.add(socketProcess);
        socketProcess.run();
//        contextsToVerify.add(socketContext);
    }

    private void closeSocket(){
        try{
            this.serverSocket.close();
            LOGGER.fine("Server socket closed");
        } catch (Exception ex){
            LOGGER.warning(ex.getMessage());
        }
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("Thread interrupted: " + ex.getMessage());
        }
    }
}
