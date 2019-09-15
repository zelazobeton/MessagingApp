package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class OpenSocketContext {
    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;

    public OpenSocketContext(BufferedReader input, PrintWriter output, Socket socket) {
        this.input = input;
        this.output = output;
        this.socket = socket;
    }


}
