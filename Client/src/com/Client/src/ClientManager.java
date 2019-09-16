package com.Client.src;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private final Integer CONNECTION_PORT;
    private final String HOST;
    private Socket socket;
    private UserContext userContext;

    public ClientManager(String HOST, Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.HOST = HOST;
        this.userContext = null;
        this.socket = null;
    }

    public void run(){
        try{
            connectToSocket();
            createUserContext();
            userContext.verifyUser();

            LOGGER.fine("User connected and verified");
        }
        catch (IOException ex){
            LOGGER.warning(ex.getMessage());
            closeSocket(socket);
        }
    }

    private void createUserContext() throws IOException{
        userContext = new UserContext(new ConsoleInOutHandler(),
                                      new ServerInOutHandler(socket.getInputStream(),
                                                             socket.getOutputStream()));
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
            socket.close();
        } catch (Exception ex){
            LOGGER.warning("No socket to close");
        }
    }
}
