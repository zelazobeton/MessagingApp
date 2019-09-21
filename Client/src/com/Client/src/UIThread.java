package com.Client.src;

import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

public class UIThread implements Runnable {
    private PrintWriter clientWriter;
    private ArrayBlockingQueue<String> userOutputBuffer;
    private Scanner scanner;

    public UIThread(PrintWriter clientWriter, ArrayBlockingQueue<String> userOutputBuffer) {
        this.clientWriter = clientWriter;
        this.userOutputBuffer = userOutputBuffer;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        while(true){
            printInterface();
            String answer = scanner.next();
        }
    }

    private void printInterface(){
        System.out.println("Options: ");
        System.out.println("select - to user to communicate");
        System.out.println("exit - exits program");
    }

    private void printReceivedCall(String callerName){
        System.out.println("You have received call from: " + callerName);
        System.out.println("yes - to begin conversation");
        System.out.println("no - to reject conversation");
    }
}
