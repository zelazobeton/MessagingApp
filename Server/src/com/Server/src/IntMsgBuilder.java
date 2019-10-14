package com.Server.src;

import com.Server.src.Constants.MsgTypes;

public class IntMsgBuilder {
    public static String buildIntConvInitResp(String toUserId,
                                              String toUserSocketId,
                                              String fromUserId,
                                              Integer fromUserSocketId,
                                              String status,
                                              String reason){
        return MsgTypes.IntConvInitRespMsg + "_" +
                toUserId + "_" + toUserSocketId + "_" +
                fromUserId + "_" + fromUserSocketId + "_" +
                status + "_" + reason;
    }

    public static String buildIntConvInitResp(String toUserId,
                                              String toUserSocketId,
                                              Integer fromUserId,
                                              Integer fromUserSocketId,
                                              String status){
        return MsgTypes.IntConvInitRespMsg + "_" +
                toUserId + "_" + toUserSocketId + "_" +
                fromUserId + "_" + fromUserSocketId + "_" +
                status;
    }

    public static String buildIntConvFinishInd(String toUserId,
                                               String toUserSocketId,
                                               Integer fromUserId,
                                               Integer fromUserSocketId){
        return MsgTypes.IntConvFinishInd + "_" +
                toUserId + "_" + toUserSocketId + "_" +
                fromUserId + "_" + fromUserSocketId;
    }

    public static String buildIntConvUserMsg(String toUserId,
                                             String toUserSocketId,
                                             Integer fromUserId,
                                             Integer fromUserSocketId,
                                             String msgContent){
        return MsgTypes.IntConvUserMsg + "_" +
                toUserId + "_" + toUserSocketId + "_" +
                fromUserId + "_" + fromUserSocketId + "_" +
                msgContent;
    }

    public static String buildIntConvInitReqMsg(final Integer toUserId,
                                                final Integer fromUserId,
                                                final Integer fromUserSocketId){
        return MsgTypes.IntConvInitReqMsg + "_" +
                toUserId + "_" +
                "NULL" + "_" +
                fromUserId + "_" +
                fromUserSocketId;
    }

    public static String buildCancelProcedureMsg(final String toUserId,
                                                 final Integer fromUserId,
                                                 final Integer fromUserSocketId){
        return MsgTypes.IntCancelProcMsg + "_" +
                toUserId + "_" +
                "NULL" + "_" +
                fromUserId + "_" +
                fromUserSocketId;
    }

}
