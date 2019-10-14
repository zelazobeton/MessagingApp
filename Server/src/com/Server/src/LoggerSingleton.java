package com.Server.src;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LoggerSingleton {
    public static final Logger LOGGER = Logger.getLogger("GLOBAL_SERVER_LOGGER");
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
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
            Date date = new Date();
            FileHandler fileHandler = new FileHandler("./logs/SERVER_" + dateFormat.format(date) + ".log");
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
            ex.printStackTrace();
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


