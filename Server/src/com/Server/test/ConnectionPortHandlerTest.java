package com.Server.test;

import com.Server.src.ConnectionPortHandler;
import org.junit.After;

import java.io.*;

import static org.junit.Assert.*;

public class ConnectionPortHandlerTest {
    private static ConnectionPortHandler sut;
    private static StringWriter outputStringWriter;
    private static String stringToRead;

    private static BufferedReader inputReader;
    private static PrintWriter printWriter;

    private void sutCreate(String stringToRead){
        inputReader = new BufferedReader(new StringReader(stringToRead));
        outputStringWriter = new StringWriter();
        printWriter = new PrintWriter(outputStringWriter, true);
        sut = new ConnectionPortHandler(inputReader, printWriter);
    }

    @org.junit.Test
    public void shouldWriteOkMsgToBufferAfterCorrectConnectInput() {
        stringToRead = "connect_1";
        sutCreate(stringToRead);

        sut.scanForNewConnections();

        String expectedString = String.format("OK_1%n");
        assertEquals(expectedString, outputStringWriter.toString());
    }

    @org.junit.Test
    public void shouldIgnoreIncorrectConnectInput() {
        stringToRead = String.format("connection%n" + "lala%n" + "connect_1");
        sutCreate(stringToRead);

        sut.scanForNewConnections();
        String expectedString = String.format("OK_1%n");
        assertEquals(expectedString, outputStringWriter.toString());
    }
}