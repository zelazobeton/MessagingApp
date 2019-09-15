package com.Client.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ClientManager {
    private final Integer CONNECTION_PORT;
    private final String HOST;
    private Socket socket;
    private BufferedReader serverReader;
    private PrintWriter clientWriter;
    private Integer connectionId;
    Scanner scanner;

    public ClientManager(String HOST, Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.HOST = HOST;
        this.socket = null;
        scanner = new Scanner(System.in);
    }

    public void run(){
        try{
            connectToSocket();
            prepareReaderAndWriter();
            verifyUser();
            LOG.DEBUG("User connected and verified");
        }
        catch (IOException ex){
            LOG.ERROR(ex.getMessage());
            closeSocket(socket);
        }
    }

    private void prepareReaderAndWriter() throws IOException{
        this.serverReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        this.clientWriter = new PrintWriter(socket.getOutputStream(), true);
    }

    private void verifyUser() throws IOException{
        while(true){
            getAndSendCredentials();
            String response = getVerificationResponse();
            if(response != null){
                if(checkVerificationResponse(response)){
                    return;
                }
                LOG.WRN("Wrong verification response or no response");
            }
            LOG.WRN("Verification failed, wait 1s");
            sleepWithExceptionHandle(1000);
        }
    }

    private void getAndSendCredentials(){
        LOG.DEBUG("Enter username: ");
        String username = scanner.nextLine();
        LOG.DEBUG("Enter password: ");
        String pwd = scanner.nextLine();
        clientWriter.println(username);
        clientWriter.println(pwd);
    }

    private String getVerificationResponse() throws IOException{
        String response;
        for(int idx = 0; idx < 3; idx++){
            LOG.DEBUG("Iteration " + idx + " to getVerificationResponse");
            response = serverReader.readLine();
            if(response != null && !response.equals("")){
                return response;
            }
            sleepWithExceptionHandle(200);
        }
        return null;
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
            LOG.WRN("Thread interrupted");
        }
    }

    private void connectToSocket(){
        while(true){
            try{
                socket = new Socket(HOST, CONNECTION_PORT);
                socket.setSoTimeout(5000);
                LOG.DEBUG("Socket successfully created");
                return;
            }
            catch (IOException ex){
                LOG.ERROR(ex.getMessage());
                closeSocket(socket);
            }
        }
    }

    private void closeSocket(Socket socket){
        try{
            socket.close();
        } catch (Exception ex){
            LOG.ERROR("No socket to close");
        }
    }
}
