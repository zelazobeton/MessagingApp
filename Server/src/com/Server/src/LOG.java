package com.Server.src;

class LOG {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void ERROR(String logMsg){
        System.out.println(ANSI_RED + "ERROR: " + logMsg);
    }

    public static void DEBUG(String logMsg){
        System.out.println(ANSI_GREEN + "DEBUG: " + logMsg);
    }

    public static void WRN(String logMsg){
        System.out.println(ANSI_YELLOW + "WRN: " + logMsg);
    }
}
