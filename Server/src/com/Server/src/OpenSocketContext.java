package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class OpenSocketContext {
    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;

    public OpenSocketContext(BufferedReader input, PrintWriter output, Socket socket) {
        this.input = input;
        this.output = output;
        this.socket = socket;
    }

    public void scanForNewConnections(){
        try{
            while(true) {
                String connectString = input.readLine();
                sleepWithExceptionHandle(1000);

                if (isConnectionReq(connectString)){
                    handleConnectionReq(connectString);
                }
                else {
                    LOG.WRN("Received wrong connect msg");
                }
            }
        }
        catch (IOException ex){
            LOG.ERROR(ex.getMessage());
        }
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOG.WRN("Thread interrupted: " + ex.getMessage());
        }
    }

    private boolean isConnectionReq(String msgString){
//        return (msgString != null && msgString.equals("connect_1"));
        return (msgString != null && Pattern.matches("connect_[0-9]", msgString));
    }

    private void handleConnectionReq(String msgString){
        Character randomAccessChar = msgString.charAt(8);
        Integer verificationInt = ThreadLocalRandom.current().nextInt(1, 10);
        output.println("OK_" + randomAccessChar + "_" + verificationInt);
        LOG.DEBUG(msgString);
    }
}
