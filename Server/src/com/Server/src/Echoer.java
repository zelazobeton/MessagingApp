package com.example.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Echoer extends Thread {
    private Socket socket;

    public Echoer(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            while(true){
                String echoString = input.readLine();
                if(echoString.equals("exit")){
                    break;
                }

                try{
                    Thread.sleep(150);
                } catch (InterruptedException ex){
                    System.out.println("Thread interrupted");
                }
                output.println("Echo from the server: " + echoString);
            }

        }catch (IOException ex){
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
}
