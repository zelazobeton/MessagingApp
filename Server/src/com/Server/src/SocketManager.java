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
    private List<SocketContext> socketContexts;
    private Queue<SocketContext> contextsToVerify;
    private ServerSocket serverSocket;
    private Integer CONNECTION_PORT;

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        socketContexts = new ArrayList<>();
        contextsToVerify = new ArrayDeque<>();
        serverSocket = null;
    }

    public boolean run(){
        openServerSocket();
        while(true){
            openNewSocketForWaitingClient();
            verifyWaitingSocket();
            sleepWithExceptionHandle(1000);
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

    private void verifyWaitingSocket(){
        LOGGER.fine("verifyWaitingSocket");
        SocketContext socketContext = contextsToVerify.poll();
        if(socketContext != null){
            if(socketContext.verifyConnection()){
                return;
            }
            else{
                contextsToVerify.add(socketContext);
            }
        }
    }

    private Socket connectSocket(){
        try{
            Socket socket = serverSocket.accept();
            LOGGER.fine("ServerSocket accepted");
            return socket;
        }
        catch (IOException ex) {
            LOGGER.warning(ex.getMessage());
            return null;
        }
    }

    private boolean openNewSocketForWaitingClient(){
        LOGGER.fine("openNewSocketForWaitingClient");
        Socket socket = connectSocket();
        if(socket == null){
            return false;
        }
        try{
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            SocketContext socketContext = new SocketContext(input, output, socket);
            socketContexts.add(socketContext);
            contextsToVerify.add(socketContext);
            return true;
        }
        catch (IOException ex){
            LOGGER.warning(ex.getMessage());
            return false;
        }
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
