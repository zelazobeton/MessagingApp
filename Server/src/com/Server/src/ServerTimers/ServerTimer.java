package com.Server.src.ServerTimers;

import com.Server.src.LoggerSingleton;
import com.Server.src.Constants.MsgTypes;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class ServerTimer implements Runnable {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private final TimerTypeName TimerType;
    private final Integer TimerType_TIME;
    private ArrayBlockingQueue<String> messageQueue;
    private final Integer socketProcessId;
    private boolean IS_RESETABLE;

    public ServerTimer(TimerTypeData timerTypeData,
                         Integer socketProcessId,
                         ArrayBlockingQueue<String> messageQueue)
    {
        this.TimerType = timerTypeData.getTimerType();
        this.TimerType_TIME = timerTypeData.getTime();
        this.IS_RESETABLE = timerTypeData.isResetable();

        this.messageQueue = messageQueue;
        this.socketProcessId = socketProcessId;
    }

    @Override
    public void run() {
        LOGGER.fine(TimerType + " for socketProcessId: " + socketProcessId + " started");
        do {
            runTimer();
        } while(IS_RESETABLE);
    }

    private void runTimer(){
        try {
            Thread.sleep(TimerType_TIME);
            expireTimer();
        }
        catch (InterruptedException ex) {
            LOGGER.fine(TimerType + " for socketProcessId: " + socketProcessId + " reset");
        }
    }

    private void expireTimer(){
        try{
            LOGGER.fine(TimerType + " for socketProcessId: " + socketProcessId + " expired");
            messageQueue.put(MsgTypes.TimerExpired + "_" + TimerType);
            IS_RESETABLE = false;
        }
        catch (InterruptedException ex) {
            LOGGER.warning("Exception thrown while expireTimer: " +
                            TimerType +
                            " for socketProcessId: " +
                            socketProcessId + " : " + ex.toString());
        }
    }
}
