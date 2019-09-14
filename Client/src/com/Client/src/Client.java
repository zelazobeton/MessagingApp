package com.Client.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class Client {
    private BufferedReader serverReader;
    private PrintWriter clientWriter;

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

    public boolean connect(){
        Integer connectionId;
        connectionId = ThreadLocalRandom.current().nextInt(1, 10);
//        connectionId = 1;
        while(true){
            try{
                clientWriter.println("connect_" + connectionId);

                for(int idx = 0; idx < 10; idx++){
                    String response = serverReader.readLine();
                    LOG.DEBUG("Received connection response: " + response);
                    if(isResponseToConnectionReq(response)){
                        LOG.DEBUG("Received resp with verificationInt");
                        return true;
                    }
                    sleepWithExceptionHandle(100);
                }
                sleepWithExceptionHandle(200);
            }
            catch (IOException ex){
                LOG.ERROR(ex.getMessage());
            }
        }
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOG.WRN("Thread interrupted");
        }
    }

    private boolean isResponseToConnectionReq(String response){
        return (response != null && Pattern.matches("OK_[0-9]_[0-9]", response));
    }
}
