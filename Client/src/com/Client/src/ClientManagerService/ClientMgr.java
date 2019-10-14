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
    private ArrayBlockingQueue<String> inputFromUserBuffer;
    private PrintWriter clientWriter;
    StringBuilder stringBuilder;
    private Thread userInputThread;
    private Map<String, List<String>> interfaceMap;
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
            sendClientLiveConnInd();

            sleepWithExceptionHandle(500);
        }
    }

    private void sendClientLiveConnInd(){
        if((cycleCounter % 80) == 0){
            sendMsgToServer(CMsgTypes.ClientLiveConnectionInd);
        }
        cycleCounter++;
    }

    void handleLoginReq(){
        stringBuilder.append(CMsgTypes.LoginReqMsg);
        setState(new ClientMgrUserCredentialsInputState(this));
        inputFromUserBuffer.clear();
    }

    void handleRegisterReq(){
        stringBuilder.append(CMsgTypes.RegisterReqMsg);
        setState(new ClientMgrUserCredentialsInputState(this));
        inputFromUserBuffer.clear();
    }

    void printInterface(String interfaceId){
        if(!interfaceMap.containsKey(interfaceId)){
            return;
        }
        List interfaceOptions = interfaceMap.get(interfaceId);
        for(Object option : interfaceOptions){
            System.out.println(option);
        }
    }

    private String[] tryGetServerMsg() {
        try{
            if(serverReader.ready()){
                return serverReader.readLine().split("_");
            }
        }
        catch (IOException ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
        }
        return null;
    }

    private String tryGetUserInput(){
        try{
            if(!inputFromUserBuffer.isEmpty()){
                return inputFromUserBuffer.take();
            }
        }
        catch (InterruptedException ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
        }
        return null;
    }

    private void runUserInputThread(){
        userInputThread.start();
    }

    void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.warning("Thread interrupted while sleeping");
        }
    }

    void setState(IClientMgrState newState){
        currentState = newState;
    }

    void sendMsgToServer(String msg){
        LOGGER.fine("Client send: " + msg +
                    " in state: " + currentState.getClass().getSimpleName());
        clientWriter.println(msg);
    }

    private void tryHandleUserInput(){
        String userInput;
        while ((userInput = tryGetUserInput()) != null){
            currentState.handleUserInput(userInput);
        }
    }

    private void tryHandleMsgFromServer(){
        String[] msgFromServer = tryGetServerMsg();
        if(msgFromServer != null){
            currentState.handleMsgFromServer(msgFromServer);
        }
    }

    void printServerMsg(String[] msgFromServer){
        for(int idx = 1; idx < msgFromServer.length; idx++){
            System.out.println(msgFromServer[idx]);
        }
    }

    void handleUserLoginInput(String userInput){
        stringBuilder.append("_").append(userInput);
        inputFromUserBuffer.clear();
        System.out.println("Enter password: ");
    }

    void handleUserPwdInput(String userInput){
        stringBuilder.append("_").append(userInput);
        sendMsgToServer(stringBuilder.toString());
        stringBuilder.setLength(0);
    }
}
