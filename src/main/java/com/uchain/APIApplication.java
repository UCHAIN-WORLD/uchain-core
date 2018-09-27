package com.uchain;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;

public class APIApplication extends ResourceConfig{

    public APIApplication(){
        packages("jersey");

        //load resource
        register(RestfulResource.class);

        //register data transfer
        register(JacksonJsonProvider.class);

        //logging
        register(LoggingFilter.class);
    }

    public static void main(String[] args){
        try {
            JerseyServer.runServer();
            System.out.println("OK");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
