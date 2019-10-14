package com.Server.src.SocketProcessService;

import com.Server.src.*;
import com.Server.src.Constants.*;
import com.Server.src.ServerTimers.ServerTimer;
import com.Server.src.ServerTimers.TimerTypeData;
import com.Server.src.ServerTimers.TimerTypeName;
import com.Server.src.ServerTimers.TimerType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class SocketProcess implements Runnable{
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private Socket clientSocket;
    private final DbHandler dbHandler;
    private ArrayBlockingQueue<String> mainMsgQueue;
    private ArrayBlockingQueue<String> socketProcessMsgQueue;
    private final Map<Integer, Integer> loggedUsersMap;
    private Map<TimerTypeName, Thread> runningTimersMap;

    private BufferedReader inputBuffer;
    private PrintWriter outputWriter;
    private UserContext userContext;
    private final Integer socketProcessId;
    private ISocketProcessState currentState = null;
    private Map<String, String> interfaceMap;
    private boolean IS_RUNNING;

    private String convUserId;
    private String convUserSocketId;
    String convUserUsername;

    public SocketProcess(DbHandler dbHandler,
                         BufferedReader inputBuffer,
                         PrintWriter outputWriter,
                         Socket clientSocket,
                         Integer socketProcessId,
                         Map<Integer, Integer> loggedUsersMap,
                         ArrayBlockingQueue<String> socketProcessMsgQueue,
                         ArrayBlockingQueue<String> mainMsgQueue)
    {
        this.dbHandler = dbHandler;
        this.inputBuffer = inputBuffer;
        this.outputWriter = outputWriter;
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

    private void startTimer(TimerTypeData timerTypeData){
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

    void resetTimer(TimerTypeName timerType){
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
            sleepWithExceptionHandle();
            tryEnqueueMsgFromClient();
            tryHandleNextMsgFromSocketProcessQueue();
        }
        LOGGER.fine("SocketProcess: " + socketProcessId + " finished running");
    }

    void finishSocketProcess(){
        LOGGER.fine("SocketProcess: " + socketProcessId + " exits");
        try{
            closeSocket();
            sendMsgToMainServer(SMsgTypes.IntSocketProcessExit + "_" + socketProcessId);
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

    private UserContext getAndAuthenticateUserDataFromDb(String username, String pwd){
        synchronized (dbHandler){
            UserContext userContext = dbHandler.getUserContextForUsername(username);
            if(userContext != null && (new PasswordAuthentication()).authenticate(pwd.toCharArray(), userContext.getHash())){
                return userContext;
            }
            return null;
        }
    }

    void handleLoginReqMsg(String[] msgInParts){
        if(isCorrectLength(msgInParts, SMsgSizes.LoginReqLength) &&
           (userContext = getAndAuthenticateUserDataFromDb(msgInParts[CC.TO_USER_ID],
                                                           msgInParts[CC.TO_USER_SOCKET_ID])) != null)
        {
            LOGGER.fine("Credentials verified");
            if(addUserToLoggedUsersMap(userContext.getUserId())){
                LOGGER.fine("userId: " + userContext.getUserId() +
                        " username: " + userContext.getUsername() +
                        " hash: " + userContext.getHash());
                sendMsgToClient(SMsgTypes.LoginSuccessInd);
                setState(new SocketLoggedIdleState(this));
                return;
            }
            userContext = null;
        }
        sendMsgToClient(SMsgTypes.LoginFailInd);
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

    private void removeUserFromLoggedUsersMap(int userId){
        synchronized (loggedUsersMap){
            if(!loggedUsersMap.containsKey(userId)){
                LOGGER.fine("loggedUsersMap remove userId: " + userId + " not logged in");
                return;
            }
            loggedUsersMap.remove(userId);
            LOGGER.fine("loggedUsersMap remove userId: " + userId + " success");
        }
    }

    void handleRegisterReqMsg(String[] msgInParts) {
        if(!isCorrectLength(msgInParts, SMsgSizes.RegisterReqLength)){
            sendMsgToClient(SMsgTypes.RegisterRespMsg + "_Register failed. No password provided");
            return;
        }
        String hash = (new PasswordAuthentication()).hash(msgInParts[CC.TO_USER_SOCKET_ID].toCharArray());
        if(dbHandler.insertUser(msgInParts[CC.TO_USER_ID], hash)){
            LOGGER.fine("Register for: " + msgInParts[CC.TO_USER_ID] + " hash: " + hash + " successed");
            sendMsgToClient(SMsgTypes.RegisterRespMsg + "_You have registered correctly. You can now log in.");
        }
        else {
            LOGGER.fine("Register for: " + msgInParts[CC.TO_USER_ID] + " hash: " + hash + " failed");
            sendMsgToClient(SMsgTypes.RegisterRespMsg + "_Register failed. User exists or something else went wrong");
        }
    }

    void logoutUser() {
        removeUserFromLoggedUsersMap(userContext.getUserId());
        userContext = null;
        resetConvUser();
    }

    private void tryEnqueueMsgFromClient(){
        try{
            if(inputBuffer.ready()){
                socketProcessMsgQueue.put(inputBuffer.readLine());
            }
        }
        catch (IOException | InterruptedException ex){
            LOGGER.warning("SocketProcess: " + socketProcessId +
                           " error while enqueuing msg from client");
            ex.printStackTrace();
        }
    }

    void sendMsgToClient(String msg){
        LOGGER.fine("SocketProcess: " + socketProcessId + " send msg: " + msg);
        outputWriter.println(msg);
    }

    private void sleepWithExceptionHandle(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            LOGGER.info("SleepWithExceptionHandle interrupted: " + ex.toString());
            ex.printStackTrace();
        }
    }

    Integer getSocketProcessId() {
        return socketProcessId;
    }

    void setState(ISocketProcessState newState){
        currentState = newState;
    }

    private void handleDeleteUserReq() {
        if(!dbHandler.deleteUser(userContext.getUsername())){
            sendMsgToClient(SMsgTypes.ServerInfoMsg +
                            "_" +
                            "User not deleted, something went wrong");
            return;
        }
        logoutUser();
        sendMsgToClient(SMsgTypes.LogoutInd + "_" + "User successfully deleted");
        setState(new SocketNoUserState(this));
    }

    private String[] tryGetNextMsgFromSocketProcessQueue(){
        if(!socketProcessMsgQueue.isEmpty()){
            try{
                return socketProcessMsgQueue.take().split("_");
            }
            catch (InterruptedException ex){
                LOGGER.warning("Exception thrown while getting msg from socket process queue: " +
                                ex.toString());
            }
        }
        return null;
    }

    private void tryHandleNextMsgFromSocketProcessQueue(){
        String[] msgToHandle = tryGetNextMsgFromSocketProcessQueue();
        if(msgToHandle != null) {
            currentState.handleMsgFromSocketProcessQueue(msgToHandle);
        }
    }

    private void handleConvInitReq(String[] msgFromClient){
        if(!isCorrectLength(msgFromClient, SMsgSizes.ConvInitCmdMsgLength)){
            sendMsgToClient(SMsgTypes.ServerInfoMsg + "_Conversation start failed: " + ConvInitStatus.Unspecified);
            return;
        }
        UserContext reqUserContext = dbHandler.getUserContextForUsername(msgFromClient[CC.TO_USER_USERNAME]);
        if(reqUserContext == null ||
           reqUserContext.getUserId() == userContext.getUserId() ||
           !loggedUsersMap.containsKey(reqUserContext.getUserId()) )
        {
            sendMsgToClient(SMsgTypes.ServerInfoMsg + "_Conversation start failed: " + ConvInitStatus.UserNotExist);
            return;
        }

        convUserId = String.valueOf(reqUserContext.getUserId());
        convUserUsername = reqUserContext.getUsername();
        sendMsgToMainServer(IntMsgBuilder.buildIntConvInitReqMsg(reqUserContext.getUserId(),
                                                                 userContext.getUserId(),
                                                                 socketProcessId,
                                                                 userContext.getUsername()));
        setState(new SocketWaitForConvInitRespState(this));
    }

    void handleIntConvInitRespMsg(String[] msg){
        switch (msg[CC.STATUS]){
            case BaseStatus.NotOK:
                sendMsgToClient(SMsgTypes.ServerInfoMsg + "_" +
                                "Conversation start failed: " + msg[CC.REASON]);
                setState(new SocketLoggedIdleState(this));
                resetConvUser();
                break;
            case BaseStatus.OK:
                convUserId = msg[CC.FROM_USER_ID];
                convUserSocketId = msg[CC.FROM_USER_SOCKET_ID];
                sendMsgToClient(SMsgTypes.ServerInfoMsg + "_" +
                        "Conversation started with: " + convUserUsername);
                setState(new SocketConversationState(this));
                break;
            default:
                break;
        }
    }

    void ignoreIntConvInitReqMsg(String[] msg){
        sendMsgToMainServer(IntMsgBuilder.buildIntConvInitResp(
                                msg[CC.FROM_USER_ID],
                                msg[CC.FROM_USER_SOCKET_ID],
                                msg[CC.TO_USER_ID],
                                socketProcessId,
                                BaseStatus.NotOK,
                                ConvInitStatus.UserBusy));
    }

    private boolean isRequestedUserLogged(Integer reqUserId){
        if (this.userContext == null){
            return false;
        }
        return this.userContext.getUserId() == reqUserId;
    }

    void sendConvFinishInd(){
        String toSendIntConvFinishInd = IntMsgBuilder.buildIntConvFinishInd(
                convUserId, convUserSocketId, userContext.getUserId(), socketProcessId);
        LOGGER.fine(toSendIntConvFinishInd);
        sendMsgToMainServer(toSendIntConvFinishInd);
    }

    void handleConvFinish(String[] convFinishMsg){
        if (convFinishMsg[CC.FROM_USER_ID].equals(convUserId)){
            resetConvUser();
            sendMsgToClient(SMsgTypes.ServerInfoMsg + "_" + "Conversation finished by another user");
        }
        LOGGER.fine("SocketProcess: " + getSocketProcessId() +
                " received IntConvFinishInd with wrong userId: " + convFinishMsg[CC.FROM_USER_ID]);
    }

    void resetConvUser(){
        convUserId = null;
        convUserSocketId = null;
        convUserUsername = null;
    }

    private boolean isCorrectLength(String[] msg, Integer correctLength){
        return msg.length >= correctLength;
    }

    void handleClientMsgInConversationState(String[] convUserMsg){
        if(!isCorrectLength(convUserMsg, SMsgSizes.UserMsgLength)){
            return;
        }
        if(convUserMsg[CC.CLIENT_CMD].equals(SClientCmdTypes.ConvFinishCmd))
        {
            sendConvFinishInd();
            resetConvUser();
            setState(new SocketLoggedIdleState(this));
            return;
        }
        sendMsgToMainServer(IntMsgBuilder.buildIntConvUserMsg(convUserId,
                                                              convUserSocketId,
                                                              userContext.getUserId(),
                                                              socketProcessId,
                                                              convUserMsg[CC.CLIENT_CMD]));
    }

    void handleClientMsgInLoggedInState(String[] msg){
        if(!isCorrectLength(msg, SMsgSizes.UserMsgLength)){
            return;
        }
        String[] clientMsg = msg[CC.CLIENT_CMD].split(":");
        switch (clientMsg[CC.MSG_ID]){
            case SClientCmdTypes.DeleteUserCmd:
                handleDeleteUserReq();
                break;
            case SClientCmdTypes.LogoutCmd:
                logoutUser();
                sendMsgToClient(SMsgTypes.LogoutInd + "_You have successfully logged out");
                setState(new SocketNoUserState(this));
                break;
            case SClientCmdTypes.ConvInitCmd:
                handleConvInitReq(clientMsg);
                break;
        }
    }

    void handleMsgFromAnotherUser(String[] msgInParts){
        if(Integer.parseInt(msgInParts[CC.TO_USER_ID]) != userContext.getUserId()){
            LOGGER.warning("SocketProcess: " + getSocketProcessId() +
                            " user logged: " + userContext.getUsername() +
                            " received msg directed to: " + msgInParts[CC.TO_USER_ID]);
            return;
        }
        sendMsgToClient(SMsgTypes.ServerInfoMsg + "_[" + convUserUsername + "]: " + msgInParts[CC.INT_MSG_CONT]);
    }

    void handleIncomingConversationInLoggedInState(String[] msgInParts){
        if(!isRequestedUserLogged(Integer.parseInt(msgInParts[CC.TO_USER_ID]))){
            sendMsgToMainServer(IntMsgBuilder.buildIntConvInitResp(msgInParts[CC.FROM_USER_ID],
                                                                    msgInParts[CC.FROM_USER_SOCKET_ID],
                                                                    msgInParts[CC.TO_USER_ID],
                                                                    socketProcessId,
                                                                    BaseStatus.NotOK,
                                                                    ConvInitStatus.UserNotLogged));
            return;
        }
        convUserId = msgInParts[CC.FROM_USER_ID];
        convUserUsername = msgInParts[CC.FROM_USER_USERNAME];
        convUserSocketId = msgInParts[CC.FROM_USER_SOCKET_ID];
        setState(new SocketWaitForClientConvAcceptState(this));
    }

    void handleClientMsgInWaitForClientConvAcceptState(String[] clientMsg){
        if(!isCorrectLength(clientMsg, SMsgSizes.UserMsgLength)){
            return;
        }
        String toSendIntConvInitRespMsg;
        switch (clientMsg[CC.CLIENT_CMD]){
            case SClientCmdTypes.YesCmd:
                toSendIntConvInitRespMsg = IntMsgBuilder.buildIntConvInitResp(
                        convUserId, convUserSocketId, userContext.getUserId(), socketProcessId, BaseStatus.OK);
                sendMsgToMainServer(toSendIntConvInitRespMsg);
                setState(new SocketConversationState(this));
                break;
            case SClientCmdTypes.NoCmd:
                toSendIntConvInitRespMsg = IntMsgBuilder.buildIntConvInitResp(
                        convUserId, convUserSocketId, String.valueOf(userContext.getUserId()), socketProcessId, BaseStatus.NotOK, ConvInitStatus.UserRefused);
                sendMsgToMainServer(toSendIntConvInitRespMsg);
                resetConvUser();
                setState(new SocketLoggedIdleState(this));
                break;
        }
    }

    void handleClientMsgInWaitForConvInitRespState(String[] clientMsg){
        if(!isCorrectLength(clientMsg, SMsgSizes.UserMsgLength)){
            return;
        }

        switch (clientMsg[CC.CLIENT_CMD]){
            case SClientCmdTypes.CancelCmd:
                sendMsgToMainServer(IntMsgBuilder.buildCancelProcedureMsg(
                                        convUserId, userContext.getUserId(), socketProcessId));
                resetConvUser();
                setState(new SocketLoggedIdleState(this));
                break;
        }
    }

    void sendStateUserInterfaceToClient(String interfaceId){
        if(!interfaceMap.containsKey(interfaceId)){
            return;
        }
        String userInterfaceString = interfaceMap.get(interfaceId);
        sendMsgToClient(SMsgTypes.Interface + "_" + userInterfaceString);
    }
}
