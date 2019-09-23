package com.Server.src.SocketProcessService;

import com.Server.src.DbHandler;
import com.Server.src.LoggerSingleton;
import com.Server.src.UserContext;

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
    private DbHandler dbHandler;

    public SocketProcess(DbHandler dbHandler, BufferedReader input, PrintWriter output, Socket socket, Integer socketProcessId) {
        this.dbHandler = dbHandler;
        this.input = input;
        this.output = output;
        this.socket = socket;
        this.userContext = null;
        this.socketProcessId = socketProcessId;
        LOGGER.fine("SocketProcessId: " + socketProcessId + " created");
    }

    public void run(){
        setState(new SocketNoUserState(this));
        LOGGER.fine("socketProcessId: " + socketProcessId + " exits");
    }

    public UserContext getUserDataFromDb(String username, String pwd){
        synchronized (dbHandler){
            return dbHandler.queryUserForUsernameAndPwd(username, pwd);
        }
    }

    public void handleLoginRespMsg(String[] msgInParts){
        if((this.userContext = getUserDataFromDb(msgInParts[1], msgInParts[2])) != null){
            LOGGER.fine("userId: " + userContext.getUserId() +
                        " username: " + userContext.getUsername() +
                        " pwd: " + userContext.getPwd());
            LOGGER.fine("Connection verified");
            sendMsgToClient("LoginSuccessInd");
            setState(new SocketLoggedIdleState(this));
        }
        else {
            sendMsgToClient("LoginFailInd");
        }
    }

    public void handleRegisterReqMsg(String[] msgInParts) {
        if(dbHandler.insertUser(msgInParts[1], msgInParts[2])){
            LOGGER.fine("Successfully created user: " + msgInParts[1] + " pwd: " + msgInParts[2]);
            sendMsgToClient("RegisterSuccessInd");
        }
        else {
            LOGGER.fine("Registration for: " + msgInParts[1] + " pwd: " + msgInParts[2] + " failed");
            sendMsgToClient("RegisterFailInd");
        }
    }

    public void logoutUser() {
        userContext = null;
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
}
