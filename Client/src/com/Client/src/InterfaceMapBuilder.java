package com.Client.src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class InterfaceMapBuilder {
    private static Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private static String INTERFACE_OPTIONS_FILE = "userInterfaces.txt";

    public static Map<String, List<String>> build(){
        try(BufferedReader br = new BufferedReader(new FileReader(INTERFACE_OPTIONS_FILE))) {
            return createInterfaceOptionsMap(br);
        }
        catch (IOException | NullPointerException ex) {
            LOGGER.warning("Error while building interfaceMap: " + ex.toString());
            ex.printStackTrace();
            return null;
        }
    }

    private static Map<String, List<String>> createInterfaceOptionsMap(BufferedReader br) throws IOException{
        Map<String, List<String>> interfaceOptionsMap = new HashMap<>();
        List<String> interfaceOptionsForSingleState = new ArrayList<>();
        String stateName;
        for(String line; (line = br.readLine()) != null; ) {
            stateName = line;
            line = br.readLine();
            while(!("".equals(line)) && line != null) {
                interfaceOptionsForSingleState.add(line);
                line = br.readLine();
            }
            interfaceOptionsMap.put(stateName, interfaceOptionsForSingleState);
            interfaceOptionsForSingleState = new ArrayList<>();
        }
        return interfaceOptionsMap;
    }
}
