package com.example.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static final Integer CONNECTION_PORT = 5000;
    public static final String HOST = "localhost";

    public static void main(String[] args) {
        try(Socket socket = new Socket(HOST, CONNECTION_PORT)){
            socket.setSoTimeout(5000); //will disconnect after 5s

            BufferedReader serverReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter clientWriter = new PrintWriter(socket.getOutputStream(), true);

            Client client = new Client(clientWriter, serverReader);

            if(!client.connect()){
                System.out.println("Could not connect to server");
                return;
            }
            client.echo();

        }catch (IOException ex){
            System.out.println("Client error" + ex.getMessage());
        }
    }
}

