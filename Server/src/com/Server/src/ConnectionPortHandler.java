package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ConnectionPortHandler {
    private BufferedReader input;
    private PrintWriter output;

    public ConnectionPortHandler(BufferedReader input, PrintWriter output) {
        this.input = input;
        this.output = output;
    }

    public void scanForNewConnections(){
        try{
            while(true) {
                String connectString = input.readLine();
                sleepWithExceptionHandle();

                if (connectString != null && connectString.equals("connect_1")) {
                    output.println("OK_1");
                    LOG.DEBUG(connectString);
                    break;
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

    private void sleepWithExceptionHandle(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            LOG.WRN("Thread interrupted: " + ex.getMessage());
        }
    }
}
