package com.Server.src;

public class IntMsgBuilder {
    public static String buildIntConvInitResp(String toUserId,
                                              String toUserSocketId,
                                              String fromUserId,
                                              String fromUserSocketId,
                                              String status,
                                              String reason){
        return MsgTypes.IntConvInitRespMsg + "_" +
                toUserId + "_" + toUserSocketId + "_" +
                fromUserId + "_" + fromUserSocketId + "_" +
                status + "_" + reason;
    }

    public static String buildIntConvInitResp(String toUserId,
                                              String toUserSocketId,
                                              String fromUserId,
                                              String fromUserSocketId,
                                              String status){
        return MsgTypes.IntConvInitRespMsg + "_" +
                toUserId + "_" + toUserSocketId + "_" +
                fromUserId + "_" + fromUserSocketId + "_" +
                status;
    }

    public static String buildIntConvInitResp(String toUserId,
                                              String toUserSocketId,
                                              String status,
                                              String reason){
        return MsgTypes.IntConvInitRespMsg + "_" +
                toUserId + "_" + toUserSocketId + "_" +
                "NULL" + "_" + "NULL" + "_" +
                status + "_" + reason;
    }
}
