package com.Client.src;


import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class SendReceiveLoopMgr {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private ArrayBlockingQueue<String> userOutputBuffer;
    private BufferedReader serverReader;
    private StringBuilder stringBuilder;

    public SendReceiveLoopMgr(InputStream inputStream, ArrayBlockingQueue<String> userOutputBuffer) {
        this.serverReader = new BufferedReader(new InputStreamReader(inputStream));
        this.stringBuilder = new StringBuilder();
        this.userOutputBuffer = userOutputBuffer;
    }

    public void run(){
        while(true){
            try{
                tryHandleServerMsg();
//                sendUserInput();
                sleepWithExceptionHandle(500);
            }
            catch (Exception ex){
                LOGGER.warning("Exception in main loop: " + ex.toString());
            }
        }
    }

    private boolean tryHandleServerMsg() throws IOException {
        if(serverReader.ready()){
            return handle(serverReader.readLine());
        }
        return false;
    }

    private boolean handle(String msg){
        return true;
    }

    public static void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            System.out.println("Thread interrupted");
        }
    }
}

