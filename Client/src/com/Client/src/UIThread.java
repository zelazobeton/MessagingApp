package com.Client.src;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class UIThread implements Runnable {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private PrintWriter clientWriter;
    private ArrayBlockingQueue<String> userOutputBuffer;
    private Scanner scanner;
    private StringBuilder stringBuilder;

    public UIThread(PrintWriter clientWriter, ArrayBlockingQueue<String> userOutputBuffer) {
        this.clientWriter = clientWriter;
        this.userOutputBuffer = userOutputBuffer;
        this.scanner = new Scanner(System.in);
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public void run() {
        while(true){
            if (!userOutputBuffer.isEmpty()){
                try{
                    String msg = userOutputBuffer.take();
                    switch (msg){
                        case "LoginReqMsg":
                            getAndSendCredentials();
                            break;
                        case "LoginSuccessInd":
                            LOGGER.fine("LoginSuccessInd received from server");
                    }
                }
                catch (InterruptedException ex){
                    LOGGER.fine(ex.toString());
                }
            }
            sleepWithExceptionHandle(500);
//            printInterface();
//            String answer = scanner.next();
        }
    }

    private void printInterface(){
        System.out.println("Options: ");
        System.out.println("1 - selects user to communicate");
        System.out.println("0 - exits program");
    }

    private void printReceivedCall(String callerName){
        System.out.println("You have received call from: " + callerName);
        System.out.println("yes - to begin conversation");
        System.out.println("no - to reject conversation");
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("Thread interrupted");
        }
    }

    public void getAndSendCredentials() {
        stringBuilder.append("LoginRespMsg_");
        System.out.println("Enter username: ");
        stringBuilder.append(scanner.next());
        stringBuilder.append("_");
        System.out.println("Enter password: ");
        stringBuilder.append(scanner.next());
        clientWriter.println(stringBuilder.toString());
        stringBuilder.setLength(0);
    }
}
