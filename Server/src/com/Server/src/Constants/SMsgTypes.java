package com.Server.src.Constants;

public class SMsgTypes {
    //Client communication messages:
    public static final String ClientMsg = "ClientMsg";
    public static final String Interface = "Interface";
    public static final String ServerInfoMsg = "ServerInfoMsg";

    public static final String LoginReqMsg = "LoginReqMsg";
    public static final String LoginSuccessInd = "LoginSuccessInd";
    public static final String LoginFailInd = "LoginFailInd";;
    public static final Integer LoginReqLength = 3;
    public static final Integer UserMsgLength = 2;

    public static final String RegisterReqMsg = "RegisterReqMsg";
    public static final String RegisterRespMsg = "RegisterRespMsg";;
    public static final Integer RegisterReqLength = 3;

    public static final String LogoutCmd = "logout";
    public static final String ExitCmd = "exit";
    public static final String DeleteUserCmd = "delete";
    public static final String ConvInitCmd = "start";
    public static final String ConvFinishCmd = "finish";

    public static final String LogoutInd = "LogoutInd";
    public static final String ClientExitInd = "ClientExitInd";
    public static final String ClientLiveConnectionInd = "ClientLiveConnectionInd";

    //Internal server messages:
    public static final String TimerExpired = "TimerExpired";

    public static final String IntSocketProcessExit = "IntSocketProcessExit";

    public static final String IntConvInitReqMsg = "IntConvInitReqMsg";
    public static final String IntConvInitRespMsg = "IntConvInitRespMsg";
    public static final String IntRouteFailInd = "IntRouteFailInd";

    public static final String IntConvFinishInd = "IntConvFinishInd";
    public static final String IntConvUserMsg = "IntConvUserMsg";

    public static final String IntCancelProcMsg = "IntCancelProc";
}
