package com.uchain.plugin;

import akka.actor.AbstractActor;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.BsonDateTime;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import com.mongodb.reactivestreams.client.*;
import com.uchain.core.block.Block;
import com.uchain.core.transaction.Transaction;
import com.uchain.core.transaction.TransactionType;
import com.uchain.plugin.notifymessage.*;
import lombok.val;

import static com.uchain.plugin.SubscriberHelpers.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MongodbPlugin extends AbstractActor {
    Logger log = LoggerFactory.getLogger(MongodbPlugin.class);

    private MongodbSetting mongodbSetting;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection blockCol;
    private MongoCollection txCol;
    private MongoCollection accountCol;
    private MongoCollection tpsHourCol;
    private MongoCollection tpsTenSecCol;

    public MongodbPlugin(MongodbSetting mongodbSetting) {
        this.mongodbSetting = mongodbSetting;
        mongoClient = MongoClients.create(this.mongodbSetting.getPlugins_mongodb_uri());
        database = mongoClient.getDatabase("uchain");
        blockCol = database.getCollection("block");
        txCol = database.getCollection("transaction");
        accountCol = database.getCollection("account");
        tpsHourCol = database.getCollection("tps_hour");
        tpsTenSecCol = database.getCollection("tps_tensec");

        init();
    }

    public static Props props(MongodbSetting mongodbSetting) {
        return Props.create(MongodbPlugin.class, mongodbSetting);
    }

    public ActorRef apply(MongodbSetting mongodbSetting) {
        return context().system().actorOf(props(mongodbSetting));
    }

    public ActorRef apply(MongodbSetting mongodbSetting, String name) {
        return context().system().actorOf(props(mongodbSetting), name);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(NewBlockProducedNotify.class, msg -> {
                })
                .match(BlockAddedToHeadNotify.class, msg -> addBlock(msg.getBlock()))
                .match(BlockConfirmedNotify.class, msg -> updateBlock(msg.getBlock()))
                .match(AddTransactionNotify.class, msg -> {
                    if (findTransaction(msg.getTransaction()) == false) {
                        addTransaction(msg.getTransaction(), null);
                    }
                })
                .match(ForkSwitchNotify.class, msg -> {
                    msg.getFrom().forEach(item -> removeBlock(item.getBlock()));
                    msg.getTo().forEach(item -> addBlock(item.getBlock()));
                })
                .build();

    }

    public boolean findTransaction(Transaction tx) {
        FindPublisher publisher = txCol.find(eq("txHash", tx.getId().toString()));

        try {
            ObservableSubscriber sub = new ObservableSubscriber();
            publisher.subscribe(sub);
            if (sub.get(10, TimeUnit.SECONDS).size() > 0) {
                return true;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }

    private void removeBlock(Block block) {
        log.info("MongodbPlugin remove block " + block.getHeader().getIndex() + ", " + block.getHeader().shortId());
        blockCol.deleteOne(eq("blockHash", block.getHeader().getId().toString())).subscribe(new PrintSubscriber<DeleteResult>(""));

        block.getTransactions().forEach(tx -> {
            txCol.deleteOne(eq("txHash", tx.getId().toString())).subscribe(new PrintSubscriber<DeleteResult>(""));
        });

        updateTps(block, false);
    }

    private void addBlock(Block block) {
        log.info("mongodb add block:" + block.getHeader().getIndex() + "," + block.shortId());

        val header = block.getHeader();
        List<String> txlist = new ArrayList<>();
        block.getTransactions().forEach(tx -> txlist.add(tx.getId().toString()));

        Document document = new Document("height", block.getHeader().getIndex())
                .append("blockHash", header.getId().toString())
                .append("timeStamp", new BsonDateTime(header.getTimeStamp()))
                .append("prevBlock", header.getPrevBlock().toString())
                .append("producer", header.getProducer().toString())
                .append("producerSig", header.getProducerSig().toString())
                .append("version", header.getVersion())
                .append("merkleRoot", header.getMerkleRoot().toString())
                .append("txNum", block.getTransactions().size())
                .append("txHashs", txlist)
                .append("createdAt", new BsonDateTime(Instant.now().toEpochMilli()))
                .append("confirmed", false);

        blockCol.insertOne(document).subscribe(new OperationSubscriber<Success>());
        block.getTransactions().forEach(tx -> {
            if (findTransaction(tx)) {
                txCol.updateOne(eq("txHash", tx.getId().toString()), set("refBlockHash", block.getHeader().getId().toString())).subscribe(new OperationSubscriber());
                txCol.updateOne(eq("txHash", tx.getId().toString()), set("refBlockHeight", block.getHeader().getIndex())).subscribe(new OperationSubscriber());
                txCol.updateOne(eq("txHash", tx.getId().toString()), set("refBlockTime", new BsonDateTime(block.getHeader().getTimeStamp()))).subscribe(new OperationSubscriber());
                updateAccount(tx, block);
            } else {
                addTransaction(tx, block);
            }
        });

        updateTps(block, true);
    }

    private void updateBlock(Block block) {
        blockCol.updateOne(eq("blockHash", block.getHeader().getId().toString()), set("confirmed", true)).subscribe(new PrintSubscriber<UpdateResult>("Update Result: %s"));
        block.getTransactions().forEach(tx -> {
            txCol.updateOne(eq("txHash", tx.getId().toString()), set("confirmed", true)).subscribe(new PrintSubscriber<UpdateResult>("Update Result: %s"));
        });
    }

    private void updateAccount(Transaction tx, Block block) {
        val option = new UpdateOptions();
        option.upsert(true);
        if (tx.fromAddress().length() > 0) {
            accountCol.updateOne(eq("address", tx.fromAddress()), set("timeStamp", new BsonDateTime(block.getHeader().getTimeStamp())), option).subscribe(new OperationSubscriber());
        }

        accountCol.updateOne(eq("address", tx.toAddress()), set("timeStamp", new BsonDateTime(block.getHeader().getTimeStamp())), option).subscribe(new OperationSubscriber());
    }

    private void updateTps(Block block, boolean isIncrease) {
        val option = new UpdateOptions();
        option.upsert(true);

        long tenSec = 10000;
        long oneHour = 3600000;

        long time10s = block.getHeader().getTimeStamp() / tenSec * tenSec;
        long timeHour = block.getHeader().getTimeStamp() / oneHour * oneHour;

        if (isIncrease)
            tpsHourCol.updateOne(eq("timeStamp", new BsonDateTime(timeHour)), inc("txs", block.getTransactions().size()), option).subscribe(new OperationSubscriber());
        else
            tpsHourCol.updateOne(eq("timeStamp", new BsonDateTime(timeHour)), inc("txs", -block.getTransactions().size()), option).subscribe(new OperationSubscriber());

        if (isIncrease)
            tpsTenSecCol.updateOne(eq("timeStamp", new BsonDateTime(time10s)), inc("txs", block.getTransactions().size()), option).subscribe(new OperationSubscriber());
        else
            tpsTenSecCol.updateOne(eq("timeStamp", new BsonDateTime(time10s)), inc("txs", -block.getTransactions().size()), option).subscribe(new OperationSubscriber());
    }

    private void addTransaction(Transaction tx, Block block) {
        val newTx = new Document("txHash", tx.getId().toString())
                .append("type", tx.getTxType().toString())
                .append("from", tx.getTxType() == TransactionType.Miner ? "" : tx.fromAddress())
                .append("to", tx.toAddress())
                .append("toName", tx.getToName())
                .append("amount", tx.getAmount().toString())
                .append("assetId", tx.getAssetId().toString())
                .append("nonce", tx.getNonce().toString())
                .append("data", tx.getData().toString())
                .append("signature", tx.getSignature().toString())
                .append("version", tx.getVersion())
                .append("createdAt", new BsonDateTime(Instant.now().toEpochMilli()))
                .append("confirmed", false);

        if (block != null) {
            newTx.append("refBlockHash", block.getHeader().getId().toString())
                    .append("refBlockHeight", block.getHeader().getIndex())
                    .append("refBlockTime", new BsonDateTime(block.getHeader().getTimeStamp()));
            updateAccount(tx, block);
        } else {
            newTx.append("refBlockHeight", Integer.MAX_VALUE);
        }

        txCol.insertOne(newTx).subscribe(new OperationSubscriber<Success>());

    }

    private void init() {
        try {
            val publisher = blockCol.find().first();
            OperationSubscriber<Success> sub = new OperationSubscriber<>();
            publisher.subscribe(sub);
            if (sub.get(10, TimeUnit.SECONDS).size() == 0) {
                log.info("init mongo");
                blockCol.createIndex(new Document("height", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                blockCol.createIndex(new Document("blockHash", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                txCol.createIndex(new Document("txHash", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                txCol.createIndex(new Document("refBlockHeight", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                txCol.createIndex(new Document("from", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                txCol.createIndex(new Document("to", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                accountCol.createIndex(new Document("address", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                accountCol.createIndex(new Document("timeStamp", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                tpsHourCol.createIndex(new Document("timeStamp", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
                tpsTenSecCol.createIndex(new Document("timeStamp", 1)).subscribe(new PrintSubscriber<String>("Created an index named: %s"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("init mongodb failed." + e.getMessage());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
