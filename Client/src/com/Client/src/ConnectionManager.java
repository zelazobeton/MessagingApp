package com.Client.src;

import com.Client.src.ClientManagerService.ClientManager;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class ConnectionManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private final Integer CONNECTION_PORT;
    private final String HOST;
    private Socket socket;

    public ConnectionManager(String HOST, Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.HOST = HOST;
        this.socket = null;
    }

    public void run(){
        try{
            connectToSocket();
            ClientManager clientManager = new ClientManager(socket.getInputStream(),
                                                            socket.getOutputStream());

            clientManager.run();
        }
        catch (IOException ex){
            LOGGER.warning(ex.getMessage());
            closeSocket(socket);
        }
    }

    private void connectToSocket(){
        while(true){
            try{
                socket = new Socket(HOST, CONNECTION_PORT);
                socket.setSoTimeout(5000);
                LOGGER.fine("Socket successfully created");
                return;
            }
            catch (IOException ex){
                LOGGER.warning(ex.getMessage());
                closeSocket(socket);
            }
        }
    }

    private void closeSocket(Socket socket){
        try{
            if(socket != null){
                socket.close();
            }
        } catch (Exception ex){
            LOGGER.warning("No socket to close");
        }
    }
}
