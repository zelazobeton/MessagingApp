package com.Client.src;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class UserContext {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private IUserInOutHandler userInOutHandler;
    private ServerInOutHandler serverInOutHandler;
    private Integer connectionId;

    public UserContext(IUserInOutHandler userInOutHandler, ServerInOutHandler serverInOutHandler) {
        this.userInOutHandler = userInOutHandler;
        this.serverInOutHandler = serverInOutHandler;
        this.connectionId = null;
    }

    public void getAndSendCredentials() {
        userInOutHandler.displayString("Enter username: ");
        String username = userInOutHandler.readString();
        userInOutHandler.displayString("Enter password: ");
        String pwd = userInOutHandler.readString();
        serverInOutHandler.sendStringToServer(username);
        serverInOutHandler.sendStringToServer(pwd);
    }

    void verifyUser() throws IOException {
        while(true){
            getAndSendCredentials();
            String response = serverInOutHandler.getResponseFromServer();
            if(response != null){
                if(checkVerificationResponse(response)){
                    return;
                }
                LOGGER.info("Wrong verification response or no response");
            }
            LOGGER.info("Verification failed, wait 1s");
            sleepWithExceptionHandle(1000);
        }
    }

    private boolean checkVerificationResponse(String response){
        if(Pattern.matches("OK_[0-9]", response)){
            connectionId = (int) response.charAt(3);
            return true;
        }
        return false;
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("Thread interrupted");
        }
    }
}
