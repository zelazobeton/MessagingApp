package com.Server.src.SocketProcessService;

import com.Server.src.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class SocketProcess implements Runnable{
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private ArrayBlockingQueue<String> socketProcessMsgQueue;
    private ArrayBlockingQueue<String> mainMsgQueue;

    private BufferedReader input;
    private PrintWriter output;
    private Socket clientSocket;
    private UserContext userContext;
    private ISocketProcessState currentState = null;
    private Integer socketProcessId;
    private DbHandler dbHandler;
    private PasswordAuthentication pwdAuth;
    private Thread noResponseTimer;
    public boolean IS_RUNNING = true;

    public SocketProcess(DbHandler dbHandler,
                         BufferedReader input,
                         PrintWriter output,
                         Socket clientSocket,
                         Integer socketProcessId,
                         ArrayBlockingQueue<String> socketProcessMsgQueue,
                         ArrayBlockingQueue<String> mainMsgQueue)
    {
        this.dbHandler = dbHandler;
        this.input = input;
        this.output = output;
        this.clientSocket = clientSocket;
        this.userContext = null;
        this.socketProcessId = socketProcessId;
        this.pwdAuth = new PasswordAuthentication();
        this.socketProcessMsgQueue = socketProcessMsgQueue;
        this.mainMsgQueue = mainMsgQueue;
        this.noResponseTimer = new Thread(new NoResponseTimerThread(socketProcessMsgQueue));
        setState(new SocketNoUserState(this));

        LOGGER.fine("SocketProcess: " + socketProcessId + " created");
    }

    @Override
    public void run(){
        noResponseTimer.start();
        while(IS_RUNNING){
            sleepWithExceptionHandle(500);
            tryEnqueueMsgFromClient();
            tryHandleNextMsgFromSocketProcessQueue();
        }
        LOGGER.fine("SocketProcess: " + socketProcessId + " finished running");
    }

    public void finishSocketProcess(){
        LOGGER.fine("SocketProcess: " + socketProcessId + " exits");
        try{
            closeSocket();
            sendSocketProcessExitToSocketMgr();
            IS_RUNNING = false;
        }
        catch (Exception ex){
            LOGGER.warning(ex.toString());
        }
    }

    private void sendSocketProcessExitToSocketMgr() throws Exception {
        try{
            mainMsgQueue.put(MsgTypes.SocketProcessExit + "_" + this.socketProcessId);
            LOGGER.fine("SocketProcessExit put into mainMsgQueue");
        }
        catch (InterruptedException ex){
            LOGGER.warning("SocketProcess: " + socketProcessId + " Error while sending SocketProcessExit");
            throw new Exception("SocketProcess: " + socketProcessId + "Finish socketProcess failed");
        }
    }

    private void closeSocket() throws Exception{
        try{
            clientSocket.close();
        }
        catch (IOException ex){
            LOGGER.warning("SocketProcess: " + socketProcessId + " Error while closing socket");
            throw new Exception("SocketProcess: " + socketProcessId + "Finish socketProcess failed");
        }
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


    public void tryEnqueueMsgFromClient(){
        try{
            if(input.ready()){
                socketProcessMsgQueue.put(input.readLine());
            }
        }
        catch (IOException | InterruptedException ex){
            ex.printStackTrace();
        }
    }

    public void sendMsgToClient(String msg){
        LOGGER.fine("SocketProcess: " + socketProcessId + " send msg: " + msg);
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

    public String[] tryGetNextMsgFromSocketProcessQueue(){
        if(!socketProcessMsgQueue.isEmpty()){
            try{
                return socketProcessMsgQueue.take().split("_");
            }
            catch (InterruptedException ex){
                LOGGER.warning("Exception thrown while tryGetNextMsgFromSocketProcessQueue: " +
                                ex.toString());
            }
        }
        return null;
    }

    public void tryHandleNextMsgFromSocketProcessQueue(){
        String[] msgToHandle = tryGetNextMsgFromSocketProcessQueue();
        if(msgToHandle != null) {
            currentState.handleMsg(msgToHandle);
        }
    }

    public void resetNoResponseTimer(){
        noResponseTimer.interrupt();
    }

    public void handleConversationReq(String[] msgFromClient){
        LOGGER.fine("handleConversationReq");
//        checkIfUserExist
//        checkIfUserOnlineAndGetsocketId
//        putIntoMainQueue msg + socketId
    }
}
