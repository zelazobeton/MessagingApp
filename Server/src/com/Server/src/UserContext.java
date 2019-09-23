package com.Server.src;

import java.util.logging.Logger;

public class UserContext {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private int userId;
    private String username;
    private String pwd;

    public UserContext(int userId, String username, String pwd) {
        LOGGER.fine("UserContext created for username: " + username);
        this.userId = userId;
        this.username = username;
        this.pwd = pwd;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPwd() {
        return pwd;
    }
}
