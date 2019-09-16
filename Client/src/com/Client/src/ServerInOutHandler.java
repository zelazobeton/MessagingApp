package com.Client.src;

import java.io.*;
import java.util.logging.Logger;

public class ServerInOutHandler {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private BufferedReader serverReader;
    private PrintWriter clientWriter;

    public ServerInOutHandler(InputStream inputStream, OutputStream outputStream) {
        this.serverReader = new BufferedReader(new InputStreamReader(inputStream));
        this.clientWriter = new PrintWriter(outputStream, true);
    }

    public void sendStringToServer(String outputString){
        clientWriter.println(outputString);
    }

    public String getResponseFromServer() throws IOException {
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
