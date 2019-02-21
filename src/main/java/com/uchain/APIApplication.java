package com.uchain;

import akka.actor.ActorRef;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

public class APIApplication extends ResourceConfig{

    public APIApplication(ActorRef nodeActor){
        packages("jersey");

        //load resource 所有的cli请求转发到类RestfulResource
        register(new RestfulResource(nodeActor));

        //register data transfer
        register(JacksonJsonProvider.class);

        //logging
        register(LoggingFilter.class);
    }

}
