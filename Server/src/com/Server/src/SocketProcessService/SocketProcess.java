package com.Server.src.SocketProcessService;

import com.Server.src.*;

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
    private ISocketProcessState currentState = null;
    private Integer socketProcessId;
    private DbHandler dbHandler;
    private PasswordAuthentication pwdAuth;

    public SocketProcess(DbHandler dbHandler, BufferedReader input, PrintWriter output, Socket socket, Integer socketProcessId) {
        this.dbHandler = dbHandler;
        this.input = input;
        this.output = output;
        this.socket = socket;
        this.userContext = null;
        this.socketProcessId = socketProcessId;
        this.pwdAuth = new PasswordAuthentication();
        LOGGER.fine("SocketProcessId: " + socketProcessId + " created");
    }

    public void run(){
        setState(new SocketNoUserState(this));
        LOGGER.fine("socketProcessId: " + socketProcessId + " exits");
    }

    public UserContext getUserDataFromDb(String username, String pwd){
        synchronized (dbHandler){
            UserContext userContext = dbHandler.getUserContextForUsername(username);
            if(userContext != null && pwdAuth.authenticate(pwd.toCharArray(), userContext.getHash())){
                return userContext;
            }
            return null;
        }
    }

    public void handleLoginRespMsg(String[] msgInParts){
        if((this.userContext = getUserDataFromDb(msgInParts[1], msgInParts[2])) != null){
            LOGGER.fine("userId: " + userContext.getUserId() +
                        " username: " + userContext.getUsername() +
                        " hash: " + userContext.getHash());
            LOGGER.fine("Connection verified");
            sendMsgToClient(MsgTypes.LoginSuccessInd);
            setState(new SocketLoggedIdleState(this));
        }
        else {
            sendMsgToClient(MsgTypes.LoginFailInd);
        }
    }

    public void handleRegisterReqMsg(String[] msgInParts) {
        String hash = pwdAuth.hash(msgInParts[2].toCharArray());
        if(dbHandler.insertUser(msgInParts[1], hash)){
            LOGGER.fine("Register for: " + msgInParts[1] + " hash: " + hash + " successed");
            sendMsgToClient(MsgTypes.RegisterSuccessInd);
        }
        else {
            LOGGER.fine("Register for: " + msgInParts[1] + " hash: " + hash + " failed");
            sendMsgToClient(MsgTypes.RegisterFailInd);
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
            ex.printStackTrace();
            return null;
        }
    }

    public void sendMsgToClient(String msg){
        LOGGER.fine("SocketProcessId: " + socketProcessId + " send msg: " + msg);
        output.println(msg);
    }

    public void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("SleepWithExceptionHandle interrupted: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public Integer getSocketProcessId() {
        return socketProcessId;
    }

    public void setState(ISocketProcessState newState){
        currentState = newState;
        currentState.run();
    }

    public void handleDeleteUserReq() {
        if(!dbHandler.deleteUser(userContext.getUsername())){
            sendMsgToClient(MsgTypes.DeleteUserFailInd);
            return;
        }
        logoutUser();
        sendMsgToClient(MsgTypes.DeleteUserSuccessInd);
        setState(new SocketNoUserState(this));
    }
}
