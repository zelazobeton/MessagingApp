package com.Client.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ClientManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private final Integer CONNECTION_PORT;
    private final String HOST;
    private Socket socket;
    private Integer connectionId;
    private IOutputInputOperator outputInputOperator;

    public ClientManager(String HOST, Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.HOST = HOST;
        this.socket = null;
        outputInputOperator = null;
    }

    public void run(){
        try{
            connectToSocket();
            prepareInOutOperator();
            verifyUser();
            LOGGER.fine("User connected and verified");
        }
        catch (IOException ex){
            LOGGER.warning(ex.getMessage());
            closeSocket(socket);
        }
    }

    private void prepareInOutOperator() throws IOException{
        outputInputOperator = new TextFileInOutOperator(socket.getInputStream(),
                                                        socket.getOutputStream());
    }

    private void verifyUser() throws IOException{
        while(true){
            outputInputOperator.getAndSendCredentials();
            String response = outputInputOperator.getVerificationResponse();
            if(response != null){
                if(checkVerificationResponse(response)){
                    return;
                }
                LOGGER.info("Wrong verification response or no response");
            }
            LOGGER.info("Verification failed, wait 1s");
            sleepWithExceptionHandle(1000);
        }
    }

    private boolean checkVerificationResponse(String response){
        if(Pattern.matches("OK_[0-9]", response)){
            connectionId = (int) response.charAt(3);
            return true;
        }
        return false;
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("Thread interrupted");
        }
    }

    private void connectToSocket(){
        while(true){
            try{
                socket = new Socket(HOST, CONNECTION_PORT);
                socket.setSoTimeout(5000);
                LOGGER.fine("Socket successfully created");
                return;
            }
            catch (IOException ex){
                LOGGER.warning(ex.getMessage());
                closeSocket(socket);
            }
        }
    }

    private void closeSocket(Socket socket){
        try{
            socket.close();
        } catch (Exception ex){
            LOGGER.warning("No socket to close");
        }
    }
}
