package com.example.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ConnectionPortHandler {
    private BufferedReader input;
    private PrintWriter output;

    public ConnectionPortHandler(BufferedReader input, PrintWriter output) throws IOException {
        this.input = input;
        this.output = output;
    }

    public void scanForNewConnections() throws IOException{
        while(true) {
            String connectString = input.readLine();
            sleepToPreventTooQuickResponse();

            if (connectString.equals("connect_1")) {
                output.println("OK_1");
                System.out.println(connectString);
                break;
            }
            else {
                System.out.println("Received wrong connect msg");
            }
        }

        while(true){
            String echoString = input.readLine();
            if(echoString.equals("exit")){
                break;
            }
            sleepToPreventTooQuickResponse();
            output.println("Echo: " + echoString);
        }
    }

    private void sleepToPreventTooQuickResponse(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            System.out.println("Thread interrupted");
        }
    }
}
