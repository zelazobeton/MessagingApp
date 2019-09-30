package com.Server.src.ServerTimers;

public class TimerType {
    public static final TimerTypeData NoResponseTimer =
            new TimerTypeData(TimerTypeName.NoResponseTimer, 50000, true);
    public static final TimerTypeData WaitForConvAcceptTimer =
            new TimerTypeData(TimerTypeName.WaitForConvAcceptTimer, 5000, false);

}
