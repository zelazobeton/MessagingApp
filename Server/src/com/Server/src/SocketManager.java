package com.Server.src;

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

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        socketProcesses = new ArrayList<>();
        contextsToVerify = new ArrayDeque<>();
        serverSocket = null;
        numOfSocketsCreated = 0;
    }

    public boolean run(){
        openServerSocket();
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

    private void openServerSocket(){
        try {
            serverSocket = new ServerSocket(CONNECTION_PORT);
            serverSocket.setSoTimeout(5000);
        }
        catch (IOException ex){
            LOGGER.warning(ex.getMessage());
            closeSocket();
        }
    }

    private void openNewSocketForWaitingClient() throws IOException{
        LOGGER.fine("openNewSocketForWaitingClient");
        Socket socket = serverSocket.accept();
        LOGGER.fine("ServerSocket accepted");
        BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        SocketProcess socketProcess = new SocketProcess(input, output, socket, numOfSocketsCreated);
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
