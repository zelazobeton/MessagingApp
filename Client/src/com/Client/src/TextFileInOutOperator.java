package com.Client.src;

import java.io.*;
import java.util.Scanner;
import java.util.logging.Logger;

public class TextFileInOutOperator implements IOutputInputOperator {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private BufferedReader serverReader;
    private PrintWriter clientWriter;
    private Scanner scanner;

    public TextFileInOutOperator(InputStream inputStream, OutputStream outputStream) {
        this.serverReader = new BufferedReader(new InputStreamReader(inputStream));
        this.clientWriter = new PrintWriter(outputStream, true);
        scanner = new Scanner(System.in);
    }

    @Override
    public void getAndSendCredentials() {
        LOGGER.fine("Enter username: ");
        String username = scanner.nextLine();
        LOGGER.fine("Enter password: ");
        String pwd = scanner.nextLine();
        clientWriter.println(username);
        clientWriter.println(pwd);
    }

    @Override
    public String getVerificationResponse() throws IOException{
        String response;
        for(int idx = 0; idx < 3; idx++){
            LOGGER.fine("Iteration " + idx + " to getVerificationResponse");
            response = serverReader.readLine();
            if (response != null && !response.equals("")) {
                return response;
            }
            sleepWithExceptionHandle(200);
        }
        return null;
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("Thread interrupted");
        }
    }
}
