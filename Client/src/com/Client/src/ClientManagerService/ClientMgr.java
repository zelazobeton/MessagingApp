package com.Client.src.ClientManagerService;

import com.Client.src.*;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class ClientMgr {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private IClientMgrState currentState = null;
    private BufferedReader serverReader;
    public ArrayBlockingQueue<String> inputFromUserBuffer;
    public PrintWriter clientWriter;
    public StringBuilder stringBuilder;
    public Thread userInputThread;
    public Map<String, List<String>> interfaceMap;
    private Integer cycleCounter;

    public ClientMgr(InputStream inputStream,
                     OutputStream outputStream)
    {
        this.cycleCounter = 0;
        this.serverReader = new BufferedReader(new InputStreamReader(inputStream));
        this.stringBuilder = new StringBuilder();
        this.clientWriter = new PrintWriter(outputStream, true);
        this.interfaceMap = InterfaceMapBuilder.build();
        this.inputFromUserBuffer = new ArrayBlockingQueue<>(20);
        this.userInputThread = new Thread(new UserInputThread(inputFromUserBuffer));
        setState(new ClientMgrIdleState(this));
    }

    public void run(){
        runUserInputThread();
        while(true){
            tryHandleUserInput();
            tryHandleMsgFromServer();
            sendLiveConnInd();

            sleepWithExceptionHandle(500);
        }
    }

    private void sendLiveConnInd(){
        if((cycleCounter % 80) == 0){
            sendMsgToServer(MsgTypes.ClientLiveConnectionInd);
        }
        cycleCounter++;
    }

    public void prepareAndSendLoginRespMsg(){
        stringBuilder.append(MsgTypes.LoginRespMsg);
        appendCredentialsToStringBuilder();
        sendMsgToServer(stringBuilder.toString());
        stringBuilder.setLength(0);
    }

    public void prepareAndSendRegisterReqMsg(){
        stringBuilder.append(MsgTypes.RegisterReqMsg);
        appendCredentialsToStringBuilder();
        sendMsgToServer(stringBuilder.toString());
        stringBuilder.setLength(0);
    }

    private void appendCredentialsToStringBuilder(){
        String userInput;
        inputFromUserBuffer.clear();
        System.out.println("Enter username: ");
        while ((userInput = tryGetUserInput()) == null){
            sleepWithExceptionHandle(200);
        }
        stringBuilder.append(userInput);
        stringBuilder.append("_");

        System.out.println("Enter password: ");
        while ((userInput = tryGetUserInput()) == null){
            sleepWithExceptionHandle(200);
        }
        stringBuilder.append(userInput);
    }

    public void printInterface(String interfaceId){
        if(!interfaceMap.containsKey(interfaceId)){
            return;
        }
        List interfaceOptions = interfaceMap.get(interfaceId);
        for(Object option : interfaceOptions){
            System.out.println(option);
        }
    }

    public String[] tryGetServerMsg() {
        try{
            if(serverReader.ready()){
                return serverReader.readLine().split("_");
            }
            return null;
        }
        catch (IOException ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
            return null;
        }
    }

    protected String tryGetUserInput(){
        String userInput = null;
        try{
            if(!inputFromUserBuffer.isEmpty()){
                userInput = inputFromUserBuffer.take();
                return userInput;
            }
            return null;
        }
        catch (InterruptedException ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
            return null;
        }
    }

    public void runUserInputThread(){
        userInputThread.start();
    }

    public void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.warning("Thread interrupted");
        }
    }

    public void setState(IClientMgrState newState){
        currentState = newState;
    }

    public void sendMsgToServer(String msg){
        LOGGER.fine("Client send: " + msg +
                    " in state: " + currentState.getClass().getSimpleName());
        clientWriter.println(msg);
    }

    public void tryHandleUserInput(){
        String userInput;
        while ((userInput = tryGetUserInput()) != null){
            currentState.handleUserInput(userInput);
        }
    }

    public void tryHandleMsgFromServer(){
        String[] msgFromServer = tryGetServerMsg();
        if(msgFromServer != null){
            currentState.handleMsgFromServer(msgFromServer);
        }
    }

    public void startConversation(){
        stringBuilder.append(MsgTypes.ConversationReqMsg);
        String userInput;
        inputFromUserBuffer.clear();
        System.out.println("Who do you want to talk to?\nEnter username: ");
        while ((userInput = tryGetUserInput()) == null){
            sleepWithExceptionHandle(200);
        }
        stringBuilder.append(userInput);
        sendMsgToServer(stringBuilder.toString());
        stringBuilder.setLength(0);
    }

    public boolean handleConversationResp(String[] msgFromServer){
        if(msgFromServer[1] != "OK"){
            System.out.println("No such user or something went wrong");
            setState(new ClientMgrLoggedInState(this));
            return false;
        }
        setState(new ClientMgrConversationState(this));
        return true;
    }
}

