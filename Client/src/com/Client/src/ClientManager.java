package com.Client.src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class ClientManager {
    private Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private final Integer CONNECTION_PORT;
    private final String HOST;
    private Socket socket;
    private UserContext userContext;
    private PrintWriter clientWriter;
    private ArrayBlockingQueue<String> userOutputBuffer;

    public ClientManager(String HOST, Integer CONNECTION_PORT) {
        this.CONNECTION_PORT = CONNECTION_PORT;
        this.HOST = HOST;
        this.userContext = null;
        this.socket = null;
        this.clientWriter = null;
        this.userOutputBuffer = new ArrayBlockingQueue<>(10);
    }

    public void run(){
        try{
            connectToSocket();
            createUserContext();
            userContext.verifyUser();
            LOGGER.fine("User connected and verified");

            SendReceiveLoopMgr sendReceiveLoopMgr = new SendReceiveLoopMgr(socket.getInputStream(),
                                                                           userOutputBuffer);
            new Thread(new UIThread(clientWriter, userOutputBuffer)).start();

            sendReceiveLoopMgr.run();
        }
        catch (IOException ex){
            LOGGER.warning(ex.getMessage());
            closeSocket(socket);
        }
    }

    private void createUserContext() throws IOException{
        clientWriter = new PrintWriter(socket.getOutputStream(), true);
        userContext = new UserContext(new ConsoleInOutHandler(),
                                      new ServerInOutHandler(socket.getInputStream(),
                                                             clientWriter));
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
