package com.Client.src;


import java.io.IOException;
import java.util.logging.*;

public class LoggerSingleton {
    public static final Logger LOGGER = Logger.getLogger("GLOBAL_CLIENT_LOGGER");
    private static LoggerSingleton instance = null;

    public static LoggerSingleton getInstance() {
        if(instance == null) {
            prepareLogger();
            instance = new LoggerSingleton();
        }
        return instance;
    }

    private static void prepareLogger() {
        try{
            FileHandler fileHandler = new FileHandler("log.txt");
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);

            SimpleConsoleHandler consoleHandler = new SimpleConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.ALL);
            LOGGER.addHandler(consoleHandler);


            LOGGER.setUseParentHandlers(false);
            LOGGER.setLevel(Level.ALL);
        }
        catch (IOException ex){
            System.out.println("Logger error: " + ex.toString());
        }
    }

    static class SimpleConsoleHandler extends Handler{
        private static final String ANSI_RED = "\u001B[31m";
        private static final String ANSI_GREEN = "\u001B[32m";
        private static final String ANSI_YELLOW = "\u001B[33m";

        @Override
        public void publish(LogRecord record) {
            if(record.getLevel() == Level.WARNING){
                System.out.println(ANSI_RED + "WARNING: " + record.getMessage());
            }
            else if(record.getLevel() == Level.INFO){
                System.out.println(ANSI_YELLOW + "INFO: " + record.getMessage());
            }
            else{
                System.out.println(ANSI_GREEN + "DEBUG: " + record.getMessage());
            }
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void close() throws SecurityException {
            System.out.close();
        }
    }
}
