package com.Server.src;

public class MsgTypes {
    //Client communication messages:
    public static final String LoginRespMsg = "LoginRespMsg";
    public static final String LoginSuccessInd = "LoginSuccessInd";
    public static final String LoginFailInd = "LoginFailInd";

    public static final String RegisterReqMsg = "RegisterReqMsg";
    public static final String RegisterSuccessInd = "RegisterSuccessInd";
    public static final String RegisterFailInd = "RegisterFailInd";

    public static final String LogoutReqMsg = "LogoutReqMsg";
    public static final String LogoutInd = "LogoutInd";
    public static final String ClientExitInd = "ClientExitInd";

    public static final String DeleteUserReqMsg = "DeleteUserReqMsg";
    public static final String DeleteUserFailInd = "DeleteUserFailInd";
    public static final String DeleteUserSuccessInd = "DeleteUserSuccessInd";

    public static final String ClientLiveConnectionInd = "ClientLiveConnectionInd";

    public static final String ConvInitReqMsg = "ConvInitReqMsg";
    public static final String ConvInitSuccessInd = "ConvInitSuccessInd";
    public static final String ConvInitFailInd = "ConvInitFailInd";

    //Internal server messages:
    public static final String IntNoResponseTimerExpired = "IntNoResponseTimerExpired";
    public static final String TimerExpired = "TimerExpired";

    public static final String IntSocketProcessExit = "IntSocketProcessExit";

    public static final String IntConvInitReqMsg = "IntConvInitReqMsg";
    public static final String IntConvInitRespMsg = "IntConvInitRespMsg";
    public static final String IntRouteFailInd = "IntRouteFailInd";
}
