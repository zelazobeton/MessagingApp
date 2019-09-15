package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
            serverSocket.setSoTimeout(5000);
            socket = serverSocket.accept();
            LOG.DEBUG("ServerSocket accepted");

            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            if(verifyConnection(input)){
                OpenSocketContext openSocketContext = new OpenSocketContext(input, output, socket);
                openSocketContexts.add(openSocketContext);
                sleepWithExceptionHandle(200);
                Integer verificationInt = ThreadLocalRandom.current().nextInt(1, 10);
                output.println("OK_" + verificationInt);
            }
            else{
                throw new IOException("Connection not verified");
            }
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

    static void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOG.WRN("Thread interrupted: " + ex.getMessage());
        }
    }

    static boolean verifyConnection(BufferedReader input){
        try{
            String username = input.readLine();
            LOG.DEBUG(username);
            String pwd = input.readLine();
            LOG.DEBUG(pwd);
            LOG.DEBUG("Connection verified");
            return true;
        }
        catch (IOException ex){
            LOG.ERROR(ex.getMessage());
            return false;
        }
    }
}
