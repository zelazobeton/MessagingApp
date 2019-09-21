package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class SocketProcess {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;
    private UserContext userContext;
    private SocketProcessState currentState = null;
    private Integer socketProcessId;

    public SocketProcess(BufferedReader input, PrintWriter output, Socket socket, Integer socketProcessId) {
        this.input = input;
        this.output = output;
        this.socket = socket;
        this.userContext = null;
        this.socketProcessId = socketProcessId;
        LOGGER.fine("SocketProcessId: " + socketProcessId + " created");
    }

    public void run(){
        setState(new SocketNoUserState(this));
    }

    public boolean verifyUser(String[] LoginRespMsgInParts){
        String username = LoginRespMsgInParts[1];
        String pwd = LoginRespMsgInParts[2];
        LOGGER.fine("username: " + username);
        LOGGER.fine("pwd: " + pwd);
        LOGGER.fine("Connection verified");
        return true;
    }

    public String[] getMsgFromClient(){
        try{
            if(!input.ready()){
                return null;
            }
            else {
                String[] msgInParts = input.readLine().split("_");
                return msgInParts;
            }
        }
        catch (IOException ex){
            LOGGER.warning(ex.toString());
            return null;
        }
    }

    public void sendMsgToClient(String msg){
        output.println(msg);
    }

    public void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("Thread interrupted: " + ex.getMessage());
        }
    }

    public Integer getSocketProcessId() {
        return socketProcessId;
    }

    public void setState(SocketProcessState newState){
        currentState = newState;
        currentState.run();
    }

    public void createServerUserContextFromLoginRespMsg(String[] LoginRespMsgInParts){
        userContext = new UserContext(999, getSocketProcessId());
    }
}
