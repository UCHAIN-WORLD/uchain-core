package com.uchain;

import akka.actor.ActorRef;
import com.uchain.main.Settings;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class JerseyServer {
    private static final Logger logger = LoggerFactory.getLogger("JerseyServer");

    public static HttpServer startServer(ActorRef nodeActor, Settings chain){
        final APIApplication apiApplication = new APIApplication(nodeActor);
        final String BASE_URI = "http://" + chain.getRpcServerSetting().getRpcServerHost()
                +":"+chain.getRpcServerSetting().getRpcServerPort()+"/";
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), apiApplication);//RESTful服务
    }

    public static void runServer(ActorRef nodeActor, Settings settings) throws IOException{
        final HttpServer server = startServer(nodeActor, settings);
        logger.info("Jersey server started...");
        System.in.read();
        server.shutdown();
    }
}
