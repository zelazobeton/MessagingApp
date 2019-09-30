package com.Server.src;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class NoResponseTimerThread implements Runnable {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private final Integer socketProcessId;
    private boolean TIMER_RUNNING;
    private ArrayBlockingQueue<String> messageQueue;

    public NoResponseTimerThread(Integer socketProcessId, ArrayBlockingQueue<String> messageQueue) {
        this.messageQueue = messageQueue;
        this.socketProcessId = socketProcessId;
        this.TIMER_RUNNING = true;
    }

    @Override
    public void run() {
        LOGGER.fine("NoResponseTimer thread for socketProcessId: " + socketProcessId + " started");
        while(TIMER_RUNNING){
            runTimer();
        }
    }

    private void runTimer(){
        try {
            Thread.sleep(50000);
            expireTimer();
        }
        catch (InterruptedException ex) {
            LOGGER.fine("NoResponseTimer for socketProcessId: " + socketProcessId + " reset");
        }
    }

    private void expireTimer(){
        try{
            LOGGER.fine("NoResponseTimer for socketProcessId: " + socketProcessId + " expired");
            messageQueue.put(MsgTypes.IntNoResponseTimerExpired);
            TIMER_RUNNING = false;
        }
        catch (InterruptedException ex) {
            LOGGER.warning("Exception thrown while NoResponseTimerExpired for socketProcessId: " +
                            socketProcessId + " : " + ex.toString());
            ex.printStackTrace();
        }
    }
}
