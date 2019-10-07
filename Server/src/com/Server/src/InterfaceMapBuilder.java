package com.Server.src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class InterfaceMapBuilder {
    private static Logger LOGGER = LoggerSingleton.getInstance().LOGGER;
    private static String INTERFACE_OPTIONS_FILE = "userInterfacesToSendToClient.txt";

    public static Map<String, String> build(){
        try(BufferedReader br = new BufferedReader(new FileReader(INTERFACE_OPTIONS_FILE))) {
            return createInterfaceOptionsMap(br);
        }
        catch (IOException | NullPointerException ex) {
            LOGGER.warning("Error while building interfaceMap: " + ex.toString());
            ex.printStackTrace();
            return null;
        }
    }

    private static Map<String, String> createInterfaceOptionsMap(BufferedReader br) throws IOException{
        Map<String, String> interfaceOptionsMap = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();

        String stateName;
        for(String line; (line = br.readLine()) != null; ) {
            stateName = line;
            line = br.readLine();
            while(!("".equals(line)) && line != null) {
                stringBuilder.append(line);
                stringBuilder.append("_");
                line = br.readLine();
            }
            interfaceOptionsMap.put(stateName, stringBuilder.toString());
            stringBuilder.setLength(0);
        }
        return interfaceOptionsMap;
    }
}
