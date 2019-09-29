package com.Server.src;

public class MsgTypes {
    public static final String LoginReqMsg = "LoginReqMsg";
    public static final String LoginRespMsg = "LoginRespMsg";
    public static final String LoginSuccessInd = "LoginSuccessInd";
    public static final String LoginFailInd = "LoginFailInd";

    public static final String RegisterReqMsg = "RegisterReqMsg";
    public static final String RegisterSuccessInd = "RegisterSuccessInd";
    public static final String RegisterFailInd = "RegisterFailInd";

    public static final String LogoutReqMsg = "LogoutReqMsg";
    public static final String LogoutRespMsg = "LogoutRespMsg";
    public static final String ClientExitInd = "ClientExitInd";

    public static final String DeleteUserReqMsg = "DeleteUserReqMsg";
    public static final String DeleteUserFailInd = "DeleteUserFailInd";
    public static final String DeleteUserSuccessInd = "DeleteUserSuccessInd";

    public static final String NoResponseTimerExpired = "NoResponseTimerExpired";
    public static final String ClientLiveConnectionInd = "ClientLiveConnectionInd";

    public static final String ConversationReqMsg = "ConversationReqMsg";
}
