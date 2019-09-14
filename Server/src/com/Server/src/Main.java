package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Integer CONNECTION_PORT = 5000;

    public static void main(String[] args) {
        List<OpenSocketContext> openSocketContexts = new ArrayList<>();
        for(int idx = 0; idx < 5; idx++){
            openNewSocketIfWaiting(openSocketContexts);
        }
        LOG.DEBUG("End of process");
    }


    static void openNewSocketIfWaiting(List<OpenSocketContext> openSocketContexts){
        Socket socket = null;
        try(ServerSocket serverSocket = new ServerSocket(CONNECTION_PORT)){
            LOG.DEBUG("ServerSocket created successfully");
            serverSocket.setSoTimeout(15000);
            socket = serverSocket.accept();
            LOG.DEBUG("ServerSocket accepted");
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            OpenSocketContext openSocketContext = new OpenSocketContext(input, output, socket);
            openSocketContexts.add(openSocketContext);
        }
        catch (IOException ex){
            LOG.ERROR(ex.getMessage());
            try{
                if(socket != null){
                    socket.close();
                    LOG.DEBUG("Server socket " + CONNECTION_PORT + " closed");
                }
            } catch (IOException socketCloseException){
                LOG.ERROR(socketCloseException.getMessage());
            }
        }
    }
}
