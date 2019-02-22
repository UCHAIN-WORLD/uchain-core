package com.uchain;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import com.uchain.common.Serializabler;
import com.uchain.core.Account;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.transaction.Transaction;
import com.uchain.core.transaction.TransactionReceipt;
import com.uchain.core.producerblock.SendRawTransaction;
import com.uchain.cryptohash.PublicKeyHash;
import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import com.uchain.networkmanager.message.BlockMessageImpl;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.concurrent.TimeUnit;

@Singleton
@Path("")
public class RestfulResource {

    ActorRef nodeActor;
    ActorRef producerActor;
    LevelDBBlockChain chain;

    public RestfulResource(ActorRef nodeActor){
        this.nodeActor = nodeActor;
        this.producerActor = null;
        this.chain = null;
    }

    @GET
    @Path("{param:getblocks}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlocks(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                        @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        if(!(query == null || query.equals("")||query.equals("[]"))) return "{ status : 400 ,result : false}";
        BlockMessageImpl.RPCCommandMessage cmdLine = new BlockMessageImpl.GetBlocksCmd();
        Future f = Patterns.ask(nodeActor, cmdLine, 1000);
        try {
            String res = (String) Await.result(f, Duration.create(1, TimeUnit.SECONDS));
            return "{ status : 200 ,result : "+res+" }";
        }
        catch (Exception e){
            e.printStackTrace();
            return "{ status : 404 ,result : false}";
        }
    }

    @GET
    @Path("{param:getblock}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlock(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                  @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        int checked = CommandReceiverService.commandCheck(query);
        if(checked != 200) return "{ status : 400 ,result : false}";
        BlockMessageImpl.RPCCommandMessage cmdLine = CommandReceiverService.getBlock(query, nodeActor, producerActor, chain);
        if(cmdLine == null) return "false";
        Future f = Patterns.ask(nodeActor, cmdLine, 1000);
        try {
            String res = (String) Await.result(f, Duration.create(1, TimeUnit.SECONDS));
            return "{ status : 200 ,result : "+res+" }";
        }
        catch (Exception e){
            e.printStackTrace();
            return "{ status : 404 ,result : false}";
        }
    }

    @GET
    @Path("{param:getaccount}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccount(@PathParam("param") String requestParam, @QueryParam("query") String query,
                           @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        int checked = CommandReceiverService.commandCheck(query);
        if(checked != 200) return "{ status : 400 ,result : false}";
        String accountAddress = CommandReceiverService.getAccount(query, nodeActor, producerActor);
        UInt160 address = PublicKeyHash.fromAddress(accountAddress);
        if(address == null) return "{ status : 400 ,result : \"Address isn't exist!\"}";
        BlockMessageImpl.RPCCommandMessage cmdLine = new BlockMessageImpl.GetAccountCmd(address);
        Future f = Patterns.ask(nodeActor, cmdLine, 1000);
        try {
            Object res =  Await.result(f, Duration.create(1, TimeUnit.SECONDS));
            if(res.equals("404")){
                return "{ status : 404 ,result : false}";
            }
            return "{ status : 200 ,result : "+Serializabler.JsonMapperTo((Account)res)+" }";
        }
        catch (Exception e){
            return "{ status : 404 ,result : false}";
        }
    }

    @GET
    @Path("{param:getblockcount}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlockCount(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                           @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        BlockMessageImpl.RPCCommandMessage cmdLine = new BlockMessageImpl.GetBlockCountCmd();
        Future f = Patterns.ask(nodeActor, cmdLine, 1000);
        try {
            Integer res = (Integer) Await.result(f, Duration.create(1, TimeUnit.SECONDS));
            return "{ status : 200 ,result : "+res+" }";
        }
        catch (Exception e){
            e.printStackTrace();
            return "{ status : 404 ,result : false}";
        }
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

    @GET
    @Path("{param:sendrawtransaction}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String sendRawTransaction(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                   @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        int checked = CommandReceiverService.commandCheck(query);
        if(checked != 200) return "{ status : 400 ,result : false}";
        SendRawTransaction rawTx = CommandReceiverService.sendRawTransaction(query, nodeActor, producerActor, chain);
        BlockMessageImpl.RPCCommandMessage cmdLine = new BlockMessageImpl.SendRawTransactionCmd(rawTx);
        Future f = Patterns.ask(nodeActor, cmdLine, 10000);
        try {
            UInt256 res = (UInt256) Await.result(f, Duration.create(10, TimeUnit.SECONDS));
            return "{ status : 200 , result : { \"txHash\" : \""+res.toString()+"\" } }";
        }
        catch (Exception e){
            e.printStackTrace();
            return "{ status : 404 ,result : false}";
        }
    }

    @POST
    @Path("{param:rpc}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String postContractRpc(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                   @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        return "";
    }

    @GET
    @Path("{param:getTransaction}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String postGetTransaction(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                  @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        //check command

        String txHash = CommandReceiverService.getTransactionHash(query);
        System.out.println(txHash);
        if (null == txHash){
            return "{ status : 404 ,result : false}";
        }
        UInt256 txId = UInt256.parse(txHash);
        BlockMessageImpl.RPCCommandMessage cmdLine = new BlockMessageImpl.GetTransactionCmd(txId);
        Future f = Patterns.ask(nodeActor, cmdLine, 10000);
        try {
            Transaction res = (Transaction) Await.result(f, Duration.create(10, TimeUnit.SECONDS));
            return "{ status : 200 , result : { "+res.toString()+" } }";
        }
        catch (Exception e){
            e.printStackTrace();
            return "{ status : 404 ,result : false}";
        }
    }

    @GET
    @Path("{param:getTxReceipt}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String postGetTransactionReceipt(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                     @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        //check command

        String txHash = CommandReceiverService.getTransactionHash(query);
        if (null == txHash){
            return "{ status : 404 ,result : false}";
        }
        UInt256 txId = UInt256.parse(txHash);
        BlockMessageImpl.RPCCommandMessage cmdLine = new BlockMessageImpl.GetTransactionReceiptCmd(txId);
        Future f = Patterns.ask(nodeActor, cmdLine, 10000);
        try {
            TransactionReceipt res = (TransactionReceipt) Await.result(f, Duration.create(10, TimeUnit.SECONDS));
            return "{ status : 200 , result : { "+res.toString()+" } }";
        }
        catch (Exception e){
            e.printStackTrace();
            return "{ status : 404 ,result : false}";
        }
    }

}
