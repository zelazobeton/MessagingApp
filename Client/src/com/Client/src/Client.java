package com.Client.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

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
//        connectionId = ThreadLocalRandom.current().nextInt(0, 1000);
        connectionId = 1;
        while(true){
            try{
                clientWriter.println("connect_" + connectionId);
                String response = serverReader.readLine();
                LOG.DEBUG("Received connection response: " + response);

                if(response.equals("OK_" + connectionId)){
//                    LOG.DEBUG("");
                    return true;
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
}
