package com.Client.src;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class UserInputThread implements Runnable {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private ArrayBlockingQueue<String> inputFromUserBuffer;
    private Scanner scanner;

    public UserInputThread(ArrayBlockingQueue<String> inputFromUserBuffer) {
        this.inputFromUserBuffer = inputFromUserBuffer;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        LOGGER.fine("User input thread started");
        while(true){
            String userInput = scanner.next();
            queueInput(userInput);
        }
    }

    private void queueInput(String userInput){
        synchronized (inputFromUserBuffer){
            try {
                inputFromUserBuffer.put(userInput);
            }
            catch (InterruptedException ex) {
                LOGGER.warning(ex.toString());
                ex.printStackTrace();
            }
        }
    }
}
