package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class SocketContext {
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
            String username = input.readLine();
            LOG.DEBUG(username);
            String pwd = input.readLine();
            LOG.DEBUG(pwd);
            connectionId = ThreadLocalRandom.current().nextInt(1, 10);
            output.println("OK_" + connectionId);
            LOG.DEBUG("Connection verified");
            return true;
        }
        catch (IOException ex){
            LOG.ERROR(ex.getMessage());
            return false;
        }
    }
}
