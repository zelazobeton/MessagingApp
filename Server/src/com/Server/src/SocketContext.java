package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class SocketContext {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;
    private Integer connectionId;

    public SocketContext(BufferedReader input, PrintWriter output, Socket socket) {
        this.input = input;
        this.output = output;
        this.socket = socket;
        this.connectionId = null;
    }

    public boolean verifyConnection(){
        try{
            if(!input.ready()){
                return false;
            }
            String username = input.readLine();
            LOGGER.fine(username);
            String pwd = input.readLine();
            LOGGER.fine(pwd);
            connectionId = ThreadLocalRandom.current().nextInt(1, 10);
            output.println("OK_" + connectionId);
            LOGGER.fine("Connection verified");
            return true;
        }
        catch (IOException ex){
            LOGGER.warning(ex.getMessage());
            return false;
        }
    }
}
