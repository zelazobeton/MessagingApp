package com.Server.src.SocketProcessService;

import com.Server.src.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class SocketProcess implements Runnable{
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private Socket clientSocket;
    private DbHandler dbHandler;
    private ArrayBlockingQueue<String> mainMsgQueue;
    private ArrayBlockingQueue<String> socketProcessMsgQueue;
    private Map<Integer, Integer> loggedUsersMap;

    private BufferedReader input;
    private PrintWriter output;
    private UserContext userContext;
    private final Integer socketProcessId;
    private ISocketProcessState currentState = null;
    private Thread noResponseTimer;
    public boolean IS_RUNNING;

    public SocketProcess(DbHandler dbHandler,
                         BufferedReader input,
                         PrintWriter output,
                         Socket clientSocket,
                         Integer socketProcessId,
                         Map<Integer, Integer> loggedUsersMap,
                         ArrayBlockingQueue<String> socketProcessMsgQueue,
                         ArrayBlockingQueue<String> mainMsgQueue)
    {
        this.dbHandler = dbHandler;
        this.input = input;
        this.output = output;
        this.clientSocket = clientSocket;
        this.userContext = null;
        this.socketProcessId = socketProcessId;
        this.loggedUsersMap = loggedUsersMap;
        this.socketProcessMsgQueue = socketProcessMsgQueue;
        this.mainMsgQueue = mainMsgQueue;
        this.noResponseTimer = new Thread(new NoResponseTimerThread(socketProcessId, socketProcessMsgQueue));
        this.IS_RUNNING = true;
        setState(new SocketNoUserState(this));
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
            sendMsgToMainServer(MsgTypes.IntSocketProcessExit + "_" + socketProcessId);
            IS_RUNNING = false;
        }
        catch (Exception ex){
            LOGGER.warning(ex.toString());
        }
    }

    private void sendMsgToMainServer(String msg) {
        try{
            mainMsgQueue.put(msg);
        }
        catch (InterruptedException ex){
            LOGGER.warning("SocketProcess: " + socketProcessId +
                           " error while send: " + msg +
                           " to mainMsgQueue");
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

    public UserContext getAndAuthenticateUserDataFromDb(String username, String pwd){
        synchronized (dbHandler){
            UserContext userContext = dbHandler.getUserContextForUsername(username);
            if(userContext != null && (new PasswordAuthentication()).authenticate(pwd.toCharArray(), userContext.getHash())){
                return userContext;
            }
            return null;
        }
    }

    public void handleLoginRespMsg(String[] msgInParts){
        if((userContext = getAndAuthenticateUserDataFromDb(msgInParts[1], msgInParts[2])) != null){
            LOGGER.fine("Credentials verified");
            if(addUserToLoggedUsersMap(userContext.getUserId())){
                LOGGER.fine("userId: " + userContext.getUserId() +
                        " username: " + userContext.getUsername() +
                        " hash: " + userContext.getHash());
                sendMsgToClient(MsgTypes.LoginSuccessInd);
                setState(new SocketLoggedIdleState(this));
                return;
            }
            userContext = null;
        }
        sendMsgToClient(MsgTypes.LoginFailInd);
    }

    private boolean addUserToLoggedUsersMap(int userId){
        synchronized (loggedUsersMap){
            if(loggedUsersMap.containsKey(userId)){
                LOGGER.fine("loggedUsersMap add userId: " + userId + " already logged in");
                return false;
            }
            loggedUsersMap.put(userId, socketProcessId);
            LOGGER.fine("loggedUsersMap add userId: " + userId + " success");
            return true;
        }
    }

    private boolean removeUserFromLoggedUsersMap(int userId){
        synchronized (loggedUsersMap){
            if(!loggedUsersMap.containsKey(userId)){
                LOGGER.fine("loggedUsersMap remove userId: " + userId + " not logged in");
                return false;
            }
            loggedUsersMap.remove(userId);
            LOGGER.fine("loggedUsersMap remove userId: " + userId + " success");
            return true;
        }
    }

    public void handleRegisterReqMsg(String[] msgInParts) {
        String hash = (new PasswordAuthentication()).hash(msgInParts[2].toCharArray());
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
        removeUserFromLoggedUsersMap(userContext.getUserId());
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
            currentState.handleMsgFromSocketProcessQueue(msgToHandle);
        }
    }

    public void resetNoResponseTimer(){
        noResponseTimer.interrupt();
    }

    public void handleConvInitReq(String[] msgFromClient){
        UserContext requestedUserContext = dbHandler.getUserContextForUsername(msgFromClient[1]);
        if(requestedUserContext == null){
            sendMsgToClient(MsgTypes.ConvInitFailInd + "_" + ConvInitStatus.UserNotExist);
            return;
        }
        else if(!loggedUsersMap.containsKey(requestedUserContext.getUserId())){
            sendMsgToClient(MsgTypes.ConvInitFailInd + "_" + ConvInitStatus.UserNotLogged);
            return;
        }
        sendMsgToMainServer(MsgTypes.IntConvInitReqMsg + "_" +
                            requestedUserContext.getUserId() + "_" +
                            this.userContext.getUserId() + "_" +
                            this.socketProcessId);
        setState(new SocketWaitForConvAcceptState(this));
    }

    public void handleIntConvInitRespMsg(String[] msgFromMainQueue){
        switch (msgFromMainQueue[5]){
            case BaseStatus.NotOK:
                sendMsgToClient(MsgTypes.ConvInitFailInd + "_" + msgFromMainQueue[6]);
                setState(new SocketLoggedIdleState(this));
                break;
            case BaseStatus.OK:
                sendMsgToClient(MsgTypes.ConvInitSuccessInd);
                setState(new SocketConversationState(this));
                break;
            default:
                break;
        }
    }

    public void ignoreIntConvInitReqMsg(String[] msgInParts){
        String ignoreReason;
        if(!isRequestedUserLogged(Integer.parseInt(msgInParts[1]))){
            ignoreReason = ConvInitStatus.UserNotLogged;
        }
        else{
            ignoreReason = ConvInitStatus.UserBusy;
        }
        String toSendIntConvInitRespMsg = IntMsgBuilder.buildIntConvInitResp(
                msgInParts[2], msgInParts[3], msgInParts[1], String.valueOf(this.socketProcessId), BaseStatus.NotOK, ignoreReason);
        sendMsgToMainServer(toSendIntConvInitRespMsg);
    }

    private boolean isRequestedUserLogged(Integer reqUserId){
        if (this.userContext == null){
            return false;
        }
        return this.userContext.getUserId() == reqUserId;
    }
}
