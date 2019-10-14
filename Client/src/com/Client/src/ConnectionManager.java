package com.Client.src;

import com.Client.src.ClientManagerService.ClientMgr;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class ConnectionManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private final Integer CONNECTION_PORT;
    private final Integer CONNECTION_TIMEOUT = 5000;
    private final String HOST;
    private Socket socket;

    public ConnectionManager(String HOST, Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.HOST = HOST;
        this.socket = null;
    }

    void run(){
        try{
            connectToSocket();
            ClientMgr clientMgr = new ClientMgr(socket.getInputStream(),
                                                socket.getOutputStream());

            clientMgr.run();
        }
        catch (IOException ex){
            LOGGER.warning(ex.toString());
            closeSocket(socket);
        }
    }

    private void connectToSocket(){
        while(true){
            try{
                socket = new Socket(HOST, CONNECTION_PORT);
                socket.setSoTimeout(CONNECTION_TIMEOUT);
                LOGGER.fine("Socket successfully created");
                return;
            }
            catch (IOException ex){
                LOGGER.warning(ex.toString());
                closeSocket(socket);
            }
        }
    }

    private void closeSocket(Socket socket){
        try{
            if(socket != null){
                socket.close();
                LOGGER.warning("Socket closed");
            }
        } catch (Exception ex){
            LOGGER.warning("Error while closing socket: No socket to close");
        }
    }
}
