package com.Client.src;

import java.io.IOException;

public interface IOutputInputOperator {
    public void getAndSendCredentials();
    public String getVerificationResponse() throws IOException;
}
