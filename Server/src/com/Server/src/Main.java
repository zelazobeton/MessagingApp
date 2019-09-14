package com.example.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final Integer CONNECTION_PORT = 5000;

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(CONNECTION_PORT)){
            Socket socket = serverSocket.accept();
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            try{
                ConnectionPortHandler connectionPortHandler = new ConnectionPortHandler(input, output);
                connectionPortHandler.scanForNewConnections();
            }
            catch (IOException ex){
                System.out.println(ex.getMessage());
            }
            finally {
                try{
                    socket.close();
                } catch (IOException ex){
                    System.out.println(ex.getMessage());
                }
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
