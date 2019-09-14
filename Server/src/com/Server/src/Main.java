package com.Server.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final Integer CONNECTION_PORT = 5000;

    public static void main(String[] args) {

        Socket socket;
        try(ServerSocket serverSocket = new ServerSocket(CONNECTION_PORT)){
            socket = serverSocket.accept();
            try{
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                ConnectionPortHandler connectionPortHandler = new ConnectionPortHandler(input, output);
                LOG.DEBUG("ConnectionPortHandler built successfully");
                connectionPortHandler.scanForNewConnections();
            }
            catch (IOException ex){
                LOG.ERROR(ex.getMessage());
            }
            finally {
                try{
                    socket.close();
                    LOG.DEBUG("Server socket " + CONNECTION_PORT + " closed");
                } catch (IOException ex){
                    LOG.ERROR(ex.getMessage());
                }
            }
        }
        catch (IOException ex){
            LOG.ERROR(ex.getMessage());
        }
    }
}
