package com.Server.src.Constants;

public class SMsgTypes {
    //Client communication messages:
    public static final String ClientMsg = "ClientMsg";
    public static final String Interface = "Interface";
    public static final String ServerInfoMsg = "ServerInfoMsg";

    public static final String LoginReqMsg = "LoginReqMsg";
    public static final String LoginSuccessInd = "LoginSuccessInd";
    public static final String LoginFailInd = "LoginFailInd";

    public static final String RegisterReqMsg = "RegisterReqMsg";
    public static final String RegisterRespMsg = "RegisterRespMsg";

    public static final String LogoutInd = "LogoutInd";
    public static final String ClientExitInd = "ClientExitInd";
    public static final String ClientLiveConnectionInd = "ClientLiveConnectionInd";

    //Internal server messages:
    public static final String TimerExpired = "TimerExpired";

    public static final String IntSocketProcessExit = "IntSocketProcessExit";

    public static final String IntConvInitReqMsg = "IntConvInitReqMsg";
    public static final String IntConvInitRespMsg = "IntConvInitRespMsg";
    public static final String IntConvFinishInd = "IntConvFinishInd";
    public static final String IntConvUserMsg = "IntConvUserMsg";

    public static final String IntRouteFailInd = "IntRouteFailInd";

    public static final String IntCancelProcMsg = "IntCancelProc";
}
