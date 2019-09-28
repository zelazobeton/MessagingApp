package com.Server.src;

import com.Server.src.SocketProcessService.SocketProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SocketManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private Integer CONNECTION_PORT;
    private List<SocketProcess> socketProcesses;
    private ServerSocket SERVER_SOCKET;
    private Integer numOfSocketsCreated;
    private DbHandler dbHandler;

    public SocketManager(Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.SERVER_SOCKET = null;
        this.socketProcesses = new ArrayList<>();
        this.numOfSocketsCreated = 0;
    }

    public void run() {
        if(!(prepareServerSocket() && openDatabase())) {
            LOGGER.fine("Socket manager finished run after preparation failure");
            return;
        }

        while(true){
            try {
                openNewSocketForWaitingClient();
                sleepWithExceptionHandle(1000);
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    private boolean prepareServerSocket(){
        try{
            openServerSocket();
            return true;
        }
        catch (Exception ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
            closeSocket();
            dbHandler.closeConnection();
            return false;
        }
    }

    private void openServerSocket() throws IOException{
        SERVER_SOCKET = new ServerSocket(CONNECTION_PORT);
        SERVER_SOCKET.setSoTimeout(5000);
    }

    private boolean openDatabase() {
        this.dbHandler = new DbHandler();
        return dbHandler.open();
    }

    private void openNewSocketForWaitingClient() throws IOException{
        LOGGER.fine("Try open new socket for client");
        Socket socket = SERVER_SOCKET.accept();
        LOGGER.fine("New socket accepted");
        BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        SocketProcess socketProcess = new SocketProcess(dbHandler, input, output, socket, numOfSocketsCreated);
        numOfSocketsCreated++;
        socketProcesses.add(socketProcess);
        socketProcess.run();
    }

    private void closeSocket(){
        try{
            this.SERVER_SOCKET.close();
            LOGGER.fine("SERVER_SOCKET closed");
        } catch (Exception ex){
            LOGGER.warning(ex.toString());
            ex.printStackTrace();
        }
    }

    private void sleepWithExceptionHandle(Integer millisecondsToSleep){
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            LOGGER.info("sleepWithExceptionHandle interrupted: " + ex.toString());
            ex.printStackTrace();
        }
    }
}
