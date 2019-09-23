package com.Server.src;

public class UserData {
    private int userId;
    private String username;
    private String pwd;

    public UserData(int userId, String username, String pwd) {
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
