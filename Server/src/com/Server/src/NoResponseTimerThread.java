package com.Server.src;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class NoResponseTimerThread implements Runnable {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private boolean TIMER_RUNNING;
    private ArrayBlockingQueue<String> messageQueue;

    public NoResponseTimerThread(ArrayBlockingQueue<String> messageQueue) {
        this.messageQueue = messageQueue;
        this.TIMER_RUNNING = true;
    }

    @Override
    public void run() {
        LOGGER.fine("NoResponseTimer thread started");
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
            LOGGER.fine("NoResponseTimer reset");
        }
    }

    private void expireTimer(){
        try{
            LOGGER.fine("NoResponseTimer expired");
            messageQueue.put(MsgTypes.NoResponseTimerExpired);
            TIMER_RUNNING = false;
        }
        catch (InterruptedException ex) {
            LOGGER.warning("Exception thrown while NoResponseTimerExpired: " + ex.toString());
            ex.printStackTrace();
        }
    }
}
