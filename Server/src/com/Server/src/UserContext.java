package com.Server.src;

import java.util.logging.Logger;

public class UserContext {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private int userId;
    private String username;
    private String hash;

    public UserContext(int userId, String username, String hash) {
        LOGGER.fine("UserContext created for username: " + username);
        this.userId = userId;
        this.username = username;
        this.hash = hash;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getHash() {
        return hash;
    }
}
