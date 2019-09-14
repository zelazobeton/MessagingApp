package com.example.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Client {
    private BufferedReader serverReader;
    private PrintWriter clientWriter;

    public Client(PrintWriter clientWriter, BufferedReader serverReader) {
        this.serverReader = serverReader;
        this.clientWriter = clientWriter;
    }

    public void echo(){
        Scanner scanner = new Scanner(System.in);
        String echoString;
        String response;
        do {
            System.out.println("Enter string to be echoed");
            echoString = scanner.nextLine();
            clientWriter.println(echoString);
            if(!echoString.equals("exit")){
                try{
                    response = serverReader.readLine();
                    System.out.println(response);
                }
                catch (IOException ex){
                    System.out.println("Msg from server cannot be read: " + ex.getMessage());
                }
            }
        } while(!echoString.equals("exit"));
    }

    public boolean connect(){
        clientWriter.println("connect_1");
        try{
            String response = serverReader.readLine();
            System.out.println(response);
            if(response.equals("OK_1")){
                return true;
            }
            else{
                return false;
            }
        }
        catch (IOException ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
