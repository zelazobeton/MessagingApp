package com.Client.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class Client {
    private BufferedReader serverReader;
    private PrintWriter clientWriter;
    private Integer connectionId;

    public Client(PrintWriter clientWriter, BufferedReader serverReader) {
        this.serverReader = serverReader;
        this.clientWriter = clientWriter;
    }

    public void echo(){
        Scanner scanner = new Scanner(System.in);
        String echoString;
        String response;
        do {
            System.out.println("Enter string to be echoed");
            echoString = scanner.nextLine();
            clientWriter.println(echoString);
            if(!echoString.equals("exit")){
                try{
                    response = serverReader.readLine();
                    System.out.println(response);
                }
                catch (IOException ex){
                    System.out.println("Msg from server cannot be read: " + ex.getMessage());
                }
            }
        } while(!echoString.equals("exit"));
    }

    public boolean verifyUser(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            getAndSendCredentials(scanner);
            String response = getResponse();
            if(response != null){
                Integer connectionId = getConnectionId(response);
                if(connectionId != null){
                    this.connectionId = connectionId;
                    return true;
                }
            }
            LOG.DEBUG("Verification failed, wait 1s");
            sleepWithExceptionHandle(1000);
        }
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOG.WRN("Thread interrupted");
        }
    }

    private Integer getConnectionId(String response){
        if(Pattern.matches("OK_[0-9]", response)){
            return (int) response.charAt(3);
        }
        return null;
    }

    private void getAndSendCredentials(Scanner scanner){
        LOG.DEBUG("Enter username: ");
        String username = scanner.nextLine();
        LOG.DEBUG("Enter password: ");
        String pwd = scanner.nextLine();
        clientWriter.println(username);
        clientWriter.println(pwd);
    }

    private String getResponse(){
        String response = null;
        for(int idx = 0; idx < 5; idx++){
            try{
                response = serverReader.readLine();
                if(!response.equals(null) && !response.equals("")){
                    LOG.DEBUG("Not empty verification response received: " + response);
                    return response;
                }
            }
            catch (IOException ex){
                LOG.DEBUG("Verification response exception " + ex.getMessage());
            }
            sleepWithExceptionHandle(200);
        }
        return null;
    }

    public Integer getConnectionId() {
        return connectionId;
    }
}
