package com.Server.src;

import java.util.logging.Logger;

public class UserContext {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private Integer userId;

    public UserContext(Integer userId, Integer socketProcessId) {
        this.userId = userId;
        LOGGER.fine("SocketProcessId: " +
                socketProcessId +
                " userContext created for user: " +
                userId);
    }
}
