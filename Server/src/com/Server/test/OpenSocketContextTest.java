package com.Server.test;

import com.Server.src.OpenSocketContext;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.*;

public class OpenSocketContextTest {
    private static OpenSocketContext sut;
    private static StringWriter outputStringWriter;
    private static String stringToRead;
    private static Socket socket;

    private static BufferedReader inputReader;
    private static PrintWriter printWriter;

    private void sutCreate(String stringToRead){
        inputReader = new BufferedReader(new StringReader(stringToRead));
        outputStringWriter = new StringWriter();
        printWriter = new PrintWriter(outputStringWriter, true);
        sut = new OpenSocketContext(inputReader, printWriter, socket);
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