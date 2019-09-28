package com.Client.src.ClientManagerService;

import com.Client.src.*;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class ClientManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private IClientManagerState currentState = null;
    private BufferedReader serverReader;
    public ArrayBlockingQueue<String> inputFromUserBuffer;
    public PrintWriter clientWriter;
    public StringBuilder stringBuilder;
    public Thread userInputThread;
    public Map<String, List<String>> interfaceMap;

    public ClientManager(InputStream inputStream,
                         OutputStream outputStream)
    {
        this.serverReader = new BufferedReader(new InputStreamReader(inputStream));
        this.stringBuilder = new StringBuilder();
        this.clientWriter = new PrintWriter(outputStream, true);
        this.interfaceMap = InterfaceMapBuilder.build();
        this.inputFromUserBuffer = new ArrayBlockingQueue<>(20);
        this.userInputThread = new Thread(new UserInputThread(inputFromUserBuffer));
    }

    public void run(){
        runUserInputThread();
        setState(new ClientManagerIdleState(this));
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

    public void handleConversationReq(String[] msgFromServer){
        return;
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

    public void setState(IClientManagerState newState){
        currentState = newState;
        currentState.run();
    }

    public void sendMsgToServer(String msg){
        LOGGER.fine("Client send: " + msg +
                    " in state: " + currentState.getClass().getSimpleName());
        clientWriter.println(msg);
    }
}

