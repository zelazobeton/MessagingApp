package com.Server.src.SocketProcessService;

import com.Server.src.*;
import com.Server.src.ServerTimers.ServerTimer;
import com.Server.src.ServerTimers.TimerTypeData;
import com.Server.src.ServerTimers.TimerTypeName;
import com.Server.src.ServerTimers.TimerType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
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
    private Map<TimerTypeName, Thread> runningTimersMap;

    private BufferedReader input;
    private PrintWriter output;
    private UserContext userContext;
    private final Integer socketProcessId;
    private ISocketProcessState currentState = null;
    private Map<String, String> interfaceMap;
    public boolean IS_RUNNING;

    public String convUserId;
    public String convUserSocketId;

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
        this.runningTimersMap = new HashMap<>();
        this.IS_RUNNING = true;
        this.interfaceMap = InterfaceMapBuilder.build();
        resetConvUser();
    }

    public void startTimer(TimerTypeData timerTypeData){
        Thread newTimer = new Thread(new ServerTimer(
                timerTypeData, this.socketProcessId, this.socketProcessMsgQueue));
        runningTimersMap.put(timerTypeData.getTimerType(), newTimer);
        newTimer.start();
    }

    public void stopTimer(TimerTypeName timerType){
        Thread timerToStopThread = runningTimersMap.get(timerType);
        if(timerToStopThread != null){
            timerToStopThread.interrupt();
            try{
                timerToStopThread.join();
            }
            catch (InterruptedException ex){
                LOGGER.warning("Exception thrown while stopTimer: " + ex.toString());
            }
            runningTimersMap.remove(timerType);
            LOGGER.fine(timerType + " for socketProcessId: " + this.socketProcessId + " stop");
        }
    }

    public void resetTimer(TimerTypeName timerType){
        Thread timerToStopThread = runningTimersMap.get(timerType);
        if(timerToStopThread != null){
            timerToStopThread.interrupt();
        }
    }


    @Override
    public void run(){
        setState(new SocketNoUserState(this));
        startTimer(TimerType.NoResponseTimer);
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
        sendMsgToClient(MsgTypes.ServerInfoMsg + "_" + "Login failed. Incorrect username or password");
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
            sendMsgToClient(MsgTypes.ServerInfoMsg + "_You have registered correctly. You can now log in.");
        }
        else {
            LOGGER.fine("Register for: " + msgInParts[1] + " hash: " + hash + " failed");
            sendMsgToClient(MsgTypes.ServerInfoMsg + "_Register failed. User exists or something else went wrong");
        }
    }

    public void logoutUser() {
        removeUserFromLoggedUsersMap(userContext.getUserId());
        userContext = null;
        resetConvUser();
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
            sendMsgToClient(MsgTypes.ServerInfoMsg +
                            "_" +
                            "User not deleted, something went wrong");
            return;
        }
        logoutUser();
        sendMsgToClient(MsgTypes.LogoutInd + "_" + "User successfully deleted");
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

    public void handleConvInitReq(String[] msgFromClient){
        if(msgFromClient.length < 2){
            sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                    "Conversation start failed: " + ConvInitStatus.Unspecified);
            return;
        }
        UserContext reqUserContext = dbHandler.getUserContextForUsername(msgFromClient[2]);
        if(reqUserContext == null || reqUserContext.getUserId() == userContext.getUserId()){
            sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                            "Conversation start failed: " + ConvInitStatus.UserNotExist);
            return;
        }
        else if(!loggedUsersMap.containsKey(reqUserContext.getUserId())){
            sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                            "Conversation start failed: " + ConvInitStatus.UserNotLogged);
            return;
        }
        convUserId = String.valueOf(reqUserContext.getUserId());
        sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                "Waiting for response from user");
        sendMsgToMainServer(IntMsgBuilder.buildIntConvInitReqMsg(reqUserContext.getUserId(),
                                                                 userContext.getUserId(),
                                                                 socketProcessId));
        setState(new SocketWaitForConvInitRespState(this));
    }

    public void handleIntConvInitRespMsg(String[] msg){
        switch (msg[5]){
            case BaseStatus.NotOK:
                sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                                "Conversation start failed: " + msg[6]);
                setState(new SocketLoggedIdleState(this));
                resetConvUser();
                break;
            case BaseStatus.OK:
                convUserId = msg[3];
                convUserSocketId = msg[4];
                sendMsgToClient(MsgTypes.ServerInfoMsg + "_" +
                        "Conversation started with: " + msg[3]);
                setState(new SocketConversationState(this));
                break;
            default:
                break;
        }
    }

    public void ignoreIntConvInitReqMsg(String[] msg){
        String ignoreReason;
        if(!isRequestedUserLogged(Integer.parseInt(msg[1]))){
            ignoreReason = ConvInitStatus.UserNotLogged;
        }
        else{
            ignoreReason = ConvInitStatus.UserBusy;
        }
        String toSendIntConvInitRespMsg = IntMsgBuilder.buildIntConvInitResp(
                msg[3], msg[4], msg[1], socketProcessId, BaseStatus.NotOK, ignoreReason);
        sendMsgToMainServer(toSendIntConvInitRespMsg);
    }

    private boolean isRequestedUserLogged(Integer reqUserId){
        if (this.userContext == null){
            return false;
        }
        return this.userContext.getUserId() == reqUserId;
    }

    public void sendConvFinishInd(){
        String toSendIntConvFinishInd = IntMsgBuilder.buildIntConvFinishInd(
                convUserId, convUserSocketId, userContext.getUserId(), socketProcessId);
        LOGGER.fine(toSendIntConvFinishInd);
        sendMsgToMainServer(toSendIntConvFinishInd);
        resetConvUser();
    }

    public void handleConvFinish(String[] convFinishMsg){
        if (convFinishMsg[3].equals(convUserId)){
            resetConvUser();
            sendMsgToClient(MsgTypes.ServerInfoMsg + "_" + "conversation finished");
        }
        LOGGER.fine("SocketProcess: " + getSocketProcessId() +
                " received IntConvFinishInd with wrong userId: " + convFinishMsg[3]);
    }

    public void resetConvUser(){
        convUserId = null;
        convUserSocketId = null;
    }

    private boolean isCorrectLength(String[] msg){
        if(msg.length == 1){
            return false;
        }
        LOGGER.fine("SocketProcess: " + getSocketProcessId() +
                " handle: " + msg[1] +
                " in state " + currentState.getClass().getSimpleName());
        return true;
    }

    public void handleClientMsgInConversationState(String[] convUserMsg){
        if(!isCorrectLength(convUserMsg)){
            return;
        }
        switch (convUserMsg[1]){
            case MsgTypes.ConvFinishCmd:
                sendConvFinishInd();
                setState(new SocketLoggedIdleState(this));
                break;
            case MsgTypes.ExitCmd:
                sendConvFinishInd();
                logoutUser();
                finishSocketProcess();
                break;
            default:
                sendMsgToMainServer(IntMsgBuilder.buildIntConvUserMsg(
                        convUserId, convUserSocketId, userContext.getUserId(), socketProcessId, convUserMsg[1]));
        }
    }

    public void handleClientMsgInLoggedInState(String[] clientMsg){
        if(!isCorrectLength(clientMsg)){
            return;
        }

        switch (clientMsg[1]){
            case MsgTypes.ExitCmd:
                logoutUser();
                finishSocketProcess();
                break;
            case MsgTypes.DeleteUserCmd:
                handleDeleteUserReq();
                break;
            case MsgTypes.LogoutCmd:
                logoutUser();
                sendMsgToClient(MsgTypes.LogoutInd + "_You have successfully logged out");
                setState(new SocketNoUserState(this));
                break;
            case MsgTypes.ConvInitCmd:
                handleConvInitReq(clientMsg);
                break;
        }
    }

    public void handleMsgFromAnotherUser(String[] msgInParts){
        if(Integer.parseInt(msgInParts[1]) != userContext.getUserId()){
            LOGGER.warning("SocketProcess: " + getSocketProcessId() +
                            " user logged: " + userContext.getUsername() +
                            " received msg directed to: " + msgInParts[1]);
            return;
        }
        sendMsgToClient(MsgTypes.ServerInfoMsg + "_" + msgInParts[1] + ": " + msgInParts[5]);
    }

    public void handleIncomingConversationInLoggedInState(String[] msgInParts){
        if(!isRequestedUserLogged(Integer.parseInt(msgInParts[1]))){
            String toSendIntConvInitRespMsg = IntMsgBuilder.buildIntConvInitResp(
                    msgInParts[3], msgInParts[4], msgInParts[1], socketProcessId, BaseStatus.NotOK, ConvInitStatus.UserNotLogged);
            sendMsgToMainServer(toSendIntConvInitRespMsg);
            return;
        }
        convUserId = msgInParts[3];
        convUserSocketId = msgInParts[4];
        setState(new SocketWaitForClientConvAcceptState(this));
    }

    public void handleClientMsgInWaitForClientConvAcceptState(String[] clientMsg){
        if(!isCorrectLength(clientMsg)){
            return;
        }
        String toSendIntConvInitRespMsg;
        switch (clientMsg[1]){
            case "yes":
                toSendIntConvInitRespMsg = IntMsgBuilder.buildIntConvInitResp(
                        convUserId, convUserSocketId, userContext.getUserId(), socketProcessId, BaseStatus.OK);
                sendMsgToMainServer(toSendIntConvInitRespMsg);
                setState(new SocketConversationState(this));
                break;
            case "no":
                toSendIntConvInitRespMsg = IntMsgBuilder.buildIntConvInitResp(
                        convUserId, convUserSocketId, String.valueOf(userContext.getUserId()), socketProcessId, BaseStatus.NotOK, ConvInitStatus.UserRefused);
                sendMsgToMainServer(toSendIntConvInitRespMsg);
                setState(new SocketLoggedIdleState(this));
                break;
        }
    }

    public void handleClientMsgInWaitForConvInitRespState(String[] clientMsg){
        if(!isCorrectLength(clientMsg)){
            return;
        }

        switch (clientMsg[1]){
            case "cancel":
                String msg = IntMsgBuilder.buildCancelProcedureMsg(
                        convUserId, userContext.getUserId(), socketProcessId);
                sendMsgToMainServer(msg);
                setState(new SocketLoggedIdleState(this));
                break;
        }
    }



    public void sendStateUserInterfaceToClient(String interfaceId){
        if(!interfaceMap.containsKey(interfaceId)){
            return;
        }
        String userInterfaceString = interfaceMap.get(interfaceId);
        sendMsgToClient(MsgTypes.Interface + "_" + userInterfaceString);
    }
}
