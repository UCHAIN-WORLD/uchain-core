package com.uchain;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Singleton
@Path("")
public class RestfulResource {

    @GET
    @Path("{param:getblocks}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlocks(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                                        @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        System.out.println("**************");
        System.out.println(CommandReceiverService.getBlocks(heigth));
        System.out.println("**************");
        String responseStr = requestParam + "[" + heigth + "]";
        return responseStr;
    }

    @GET
    @Path("{param:getblock}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlock(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                                  @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String responseStr = requestParam + "[" + heigth + "]";
        return responseStr;
    }

    @GET
    @Path("{param:getblockcount}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlockCount(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                           @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String responseStr = requestParam + "[" + heigth + "]";
        return responseStr;
    }

    @POST
    @Path("{param:produceblock}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String postProduceBlock(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                                @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String responseStr = requestParam + "[" + heigth + "]";
        return responseStr;
    }

}
