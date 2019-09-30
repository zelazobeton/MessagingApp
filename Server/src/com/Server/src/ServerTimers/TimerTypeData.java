package com.Server.src.ServerTimers;

public class TimerTypeData {
    private final TimerTypeName timerType;
    private final Integer time;
    private final boolean isResetable;

    public TimerTypeData(TimerTypeName timerType, Integer time, boolean isResetable) {
        this.timerType = timerType;
        this.time = time;
        this.isResetable = isResetable;
    }

    public TimerTypeName getTimerType() {
        return timerType;
    }

    public Integer getTime() {
        return time;
    }

    public boolean isResetable() {
        return isResetable;
    }
}
