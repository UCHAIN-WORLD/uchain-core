package com.uchain;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.DTO.CommandSuffix;

import java.io.IOException;
import java.util.ArrayList;

public class CommandReceiverService {
    static public String getBlocks(String height){
        return "";
//        try {
//            ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(height);
//            if(commandSuffixes.size() ==1){
//                val suffixParam = commandSuffixes.get(0).getSuffixParam();
//                val suffixValue = commandSuffixes.get(0).getSuffixValue();
//                if(suffixParam.matches("(?:-h|-height)")){
//                    int heightValue = Integer.valueOf(suffixValue);
//                    MainApp.nodeActor.tell();
//                }
//            }
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return "";
//        }
    }

    public static ArrayList<CommandSuffix> parseCommandSuffixes(String height){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            JavaType javaType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, CommandSuffix.class);
            return mapper.readValue(height, javaType);
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<CommandSuffix>();
        }
    }
}
