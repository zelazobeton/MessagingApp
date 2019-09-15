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

public class SocketManager {
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
            LOG.ERROR(ex.getMessage());
            closeSocket();
        }
    }

    private void verifyWaitingSocket(){
        LOG.DEBUG("verifyWaitingSocket");
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
            LOG.DEBUG("ServerSocket accepted");
            return socket;
        }
        catch (IOException ex) {
            LOG.ERROR(ex.getMessage());
            return null;
        }
    }

    private boolean openNewSocketForWaitingClient(){
        LOG.DEBUG("openNewSocketForWaitingClient");
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
            LOG.ERROR(ex.getMessage());
            return false;
        }
    }

    private void closeSocket(){
        try{
            this.serverSocket.close();
            LOG.DEBUG("Server socket closed");
        } catch (Exception ex){
            LOG.ERROR(ex.getMessage());
        }
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOG.WRN("Thread interrupted: " + ex.getMessage());
        }
    }
}
