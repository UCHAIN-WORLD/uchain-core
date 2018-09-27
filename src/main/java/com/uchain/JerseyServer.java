package com.uchain;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class JerseyServer {
    public static final String BASE_URI = "http://localhost:1943/";

    public static HttpServer startServer(){
        final APIApplication apiApplication = new APIApplication();
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), apiApplication);
    }

    public static void runServer() throws IOException{
        final HttpServer server = startServer();
        System.out.println("Jersey server started...");
        System.in.read();
        server.shutdown();
    }
}
