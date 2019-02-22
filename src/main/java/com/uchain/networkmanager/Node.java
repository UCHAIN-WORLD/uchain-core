package com.uchain.networkmanager;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Lists;
import com.uchain.common.Serializabler;
import com.uchain.core.*;
import com.uchain.core.block.Block;
import com.uchain.core.block.BlockChain;
import com.uchain.core.consensus.ForkItem;
import com.uchain.core.producerblock.ProduceAsyncTask;
import com.uchain.core.producerblock.ProduceStateImpl.NodeStopMessage;
import com.uchain.core.producerblock.Producer;
import com.uchain.core.producerblock.SendRawTransaction;
import com.uchain.core.transaction.Transaction;
import com.uchain.core.transaction.TransactionReceipt;
import com.uchain.cryptohash.BinaryData;
import com.uchain.cryptohash.CryptoUtil;
import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import com.uchain.main.Settings;
import com.uchain.networkmanager.message.*;
import com.uchain.networkmanager.message.BlockMessageImpl.*;
import com.uchain.networkmanager.peermanager.PeerHandlerManager;
import com.uchain.networkmanager.upnpsettings.UPnP;
import com.uchain.networkmanager.upnpsettings.UPnPGatewayImpl;
import com.uchain.plugin.MongodbPlugin;
import com.uchain.plugin.Notification;
import com.uchain.plugin.notifymessage.*;
import com.uchain.util.NetworkTimeProvider;
import com.uchain.uvm.Repository;
import com.uchain.uvm.repository.RepositoryWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bitlet.weupnp.GatewayDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.uchain.util.BIUtil.toBI;

@Getter
@Setter
public class Node extends AbstractActor {
    Logger log = LoggerFactory.getLogger(Node.class);
    private Settings settings;
    private BlockChain chain;
    private NetworkTimeProvider timeProvider;
    private ActorRef peerHandlerManager;
    private ActorRef producer;
    private Notification notification = new Notification();
    private ActorRef mongodbPlugin;
    private Repository repository;

    public Node(Settings settings) {
        this.settings = settings;
        NotificationOnBlock notificationOnBlock = this::onBlock;
        NotificationOnTransaction notificationOnTransaction = this::onTransaction;

        if (this.settings.getMongodbSetting().isPlugins_mongodb_enabled()) {
            mongodbPlugin = context().actorOf(MongodbPlugin.props(settings.getMongodbSetting()));
            notification.register(mongodbPlugin);
        }

        //  if (this.settings.isUpnpEnabled()) {
        UPnP upnp = new UPnP(settings);
        GatewayDevice gatewayDevice = upnp.getValidGateway();
        if (gatewayDevice != null) {
            UPnPGatewayImpl uPnPGateway = new UPnPGatewayImpl(gatewayDevice);
            int port = Integer.parseInt(settings.getBindAddress().split(":")[1]);
            uPnPGateway.addPort(port);

            InetSocketAddress externalSocketAddress = new InetSocketAddress(uPnPGateway.getExternalAddress(), port);
        }

        //   }

        chain = LevelDBBlockChainBuilder.populate(settings, notificationOnBlock, notificationOnTransaction, this);
        repository = new RepositoryWrapper(chain);
        timeProvider = new NetworkTimeProvider(settings.getNetworkTimeProviderSettings());
        peerHandlerManager = context().actorOf(PeerHandlerManager.props(settings, timeProvider), "peerHandlerManager");
        context().actorOf(NetworkManager.props(settings, chain.getChainInfo(), timeProvider, peerHandlerManager), "networkManager");
        producer = context().actorOf(Producer.props(settings.getConsensusSettings()));
    }

    public static Props props(Settings settings) {
        return Props.create(Node.class, settings);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(VersionMessage.class, msg -> processVersionMessage(msg))
                .match(GetBlocksMessage.class, msg -> processGetBlocksMessage(msg))
                .match(BlockMessage.class, msg -> processBlockMessage(msg))
                .match(BlocksMessage.class, msg -> processBlocksMessage(msg))
                .match(TransactionsMessage.class, msg -> processTransactionsMessage(msg))
                .match(InventoryMessage.class, msg -> processInventoryMessage(msg))
                .match(GetDataMessage.class, msg -> processGetDataMessage(msg))
                .match(NodeStopMessage.class, msg -> nodeStopMessage())
                .match(RPCCommandMessage.class, msg -> processRPCCommandMessage(msg))
                .match(ProduceAsyncTask.class, msg -> msg.invoke(chain)).build();
    }

    private void onBlock(Block block) {
        log.info("block (" + block.height() + ", " + block.timeStamp() + ") produced by " + block.getHeader().getProducer().toAddress().substring(0, 6) + " " + block.shortId());
        peerHandlerManager.tell(new BlockMessage(block), getSelf());

    }

    private void onTransaction(Transaction trx) {
        log.info(trx.toString());
    }

    private void processRPCCommandMessage(RPCCommandMessage msg) {

        if (msg instanceof GetBlocksCmd) {
            CmdGetBlocks();
        }
        if (msg instanceof GetBlockByHeightCmd) {
            Integer h = ((GetBlockByHeightCmd) msg).getHeight();
            CmdGetBlockByHeight(h);
        }
        if (msg instanceof GetBlockByIdCmd) {
            UInt256 id = ((GetBlockByIdCmd) msg).getId();
            CmdGetBlockById(id);
        }
        if (msg instanceof GetBlockCountCmd) {
            CmdGetBlockCount();
        }
        if (msg instanceof GetAccountCmd) {
            UInt160 address = ((GetAccountCmd) msg).getAddress();
            CmdGetAccount(address);
        }

        if (msg instanceof SendRawTransactionCmd) {
            CmdSendRawTransaction(((SendRawTransactionCmd) msg).getRawTx());
        }

        if (msg instanceof GetTransactionCmd) {
            CmdGetTransaction(((GetTransactionCmd) msg).getId());
        }

        if (msg instanceof GetTransactionReceiptCmd) {
            CmdGetTransactionReceipt(((GetTransactionReceiptCmd) msg).getId());
        }
    }

    private void CmdGetBlocks() {
        int blockNum = chain.getHeight();
        val blocks = new ArrayList<Block>();
        try {
            for (int i = blockNum - 5; i <= blockNum; i++) {
                if (i >= 0)
                    blocks.add(chain.getBlock(i));
            }
            getSender().tell(Serializabler.JsonMapperTo(blocks), getSender());
        } catch (Throwable e) {
            e.printStackTrace();
            getSender().tell("false", getSender());
        }
    }

    private void CmdGetBlockById(UInt256 id) {
        val block = chain.getBlock(id);
        if (block == null) getSender().tell("false", getSender());
        try {
            getSender().tell(Serializabler.JsonMapperTo(block), getSender());
        } catch (Throwable e) {
            e.printStackTrace();
            getSender().tell("false", getSender());
        }

    }

    private void CmdGetBlockByHeight(Integer h) {
        val block = chain.getBlock(h);
        try {
            getSender().tell(Serializabler.JsonMapperTo(block), getSender());
        } catch (Throwable e) {
            e.printStackTrace();
            getSender().tell("false", getSender());
        }
    }

    private void CmdGetBlockCount() {
        getSender().tell(chain.getHeight(), getSender());
    }

    private void CmdGetAccount(UInt160 address) {
        Account account = chain.getAccount(address);
        if (account == null)
            getSender().tell("404", getSender());
        else
            getSender().tell(account, getSender());
    }

    private void CmdSendRawTransaction(SendRawTransaction msg) {
        BinaryData rawTx = msg.getRawTx();
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(CryptoUtil.binaryData2array(rawTx)));
        val tx = Transaction.deserialize(is);

        boolean validTx = true;

        if (!tx.verifySignature()) {
            validTx = false;
        }

        BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(toBI(tx.getGasLimit()));
        BigInteger totalCost = tx.getValue().add(txGasCost);
        BigInteger senderBalance = repository.getBalance(tx.fromPubKeyHash(), tx.getAssetId());

        if (senderBalance.compareTo(totalCost) < 0) {
            log.info(String.format("Not enough cash: Require: %s, Sender cash: %s", totalCost, senderBalance));
            validTx = false;
        }

        if (validTx) {
            chain.addTransaction(tx);       //add tx into tx pool

            //broadcast tx to other nodes
            List<UInt256> txList = new ArrayList<>();
            txList.add(tx.id());
            peerHandlerManager.tell(new BlockMessageImpl.InventoryMessage(new InventoryPayload(InventoryType.Tx, txList)).pack(), getSelf());
            getSender().tell(tx.id(), getSender());
        }
    }

    private void CmdGetTransaction(UInt256 id) {
        Transaction tx = chain.getTransaction(id);
        if (tx == null)
            getSender().tell("404", getSender());
        else
            getSender().tell(tx, getSender());
    }

    private void CmdGetTransactionReceipt(UInt256 id) {
        TransactionReceipt txReceipt = chain.getTransactionReceipt(id);
        if (txReceipt == null)
            getSender().tell("404", getSender());
        else
            getSender().tell(txReceipt, getSender());
    }

    private void processVersionMessage(VersionMessage msg) {
        // first msg, start to sync
        sendGetBlocksMessage();
    }

    private UInt256 findLatestHash(List<UInt256> hashs) {
        int index = 0;
        Boolean found = false;
        UInt256 latest = chain.getHeader(0).id();
        while (index < hashs.size() && found == false) {
            if (chain.containsBlock(hashs.get(index))) {
                latest = hashs.get(index);
                found = true;
            }
            index += 1;
        }
        return latest;
    }

    private void processGetBlocksMessage(GetBlocksMessage msg) {
        log.info("receive GetBlocksMessage  " + msg.getBlockHashs().getHashStart().get(0).shortString());
        if (msg.getBlockHashs().getHashStart().get(0).equals(UInt256.Zero())) {
            peerHandlerManager.tell(new InventoryMessage(new InventoryPayload(InventoryType.Block, Arrays.asList(chain.getLatestHeader().id()))).pack(), getSelf());
        } else {
            UInt256 hash = findLatestHash(msg.getBlockHashs().getHashStart());
            List<UInt256> hashs = Lists.newArrayList();
            int hashCountMax = 10;
            int hashCount = 0;
            hashs.add(hash);
            UInt256 next = chain.getNextBlockId(hash);
            while (next != null && hashCount < hashCountMax) {
                hashCount += 1;
                hashs.add(next);
                next = chain.getNextBlockId(next);
            }
            log.info("send InventoryMessage, , block hash count = " + hashs.size());
            peerHandlerManager.tell(new InventoryMessage(new InventoryPayload(InventoryType.Block, hashs)).pack(), getSelf());
        }
    }

    private void processBlockMessage(BlockMessage msg) {
        log.info("received block " + msg.getBlock().height() + " (" + msg.getBlock().shortId() + ")");
        if (chain.tryInsertBlock(msg.getBlock(), true)) {
            peerHandlerManager.tell(new InventoryMessage(new InventoryPayload(InventoryType.Block, Arrays.asList(msg.getBlock().id()))), getSelf());
            log.info("success insert block " + msg.getBlock().height() + " (" + msg.getBlock().shortId() + ")");
        } else {
            log.error("failed insert block " + msg.getBlock().height() + ", (" + msg.getBlock().shortId() + ") to db");
            if (!chain.containsBlock(msg.getBlock().id())) {
                // out of sync, or there are fork chains, try to get more blocks
                if (msg.getBlock().height() - chain.getHeight() < 100) // do not send too many request during init sync
                    sendGetBlocksMessage();
            }
        }
    }

    private void processBlocksMessage(BlocksMessage msg) {
        log.info("received " + msg.getBlocksPayload().getBlocks().size() + " blocks, first is "
                + msg.getBlocksPayload().getBlocks().get(0).height() + " "
                + msg.getBlocksPayload().getBlocks().get(0).shortId());
        msg.getBlocksPayload().getBlocks().forEach(block -> {
            if (chain.tryInsertBlock(block, true)) {
                log.info("success insert block " + block.height() + " ($" + block.shortId() + ")");
                // no need to send INV during sync
            } else {
                log.error("failed insert block " + block.height() + " ($" + block.shortId() + ") to db");
            }
        });
        if (msg.getBlocksPayload().getBlocks().size() > 1) {
            int msgBlockSize = msg.getBlocksPayload().getBlocks().size();
            Block msgLastBlock = msg.getBlocksPayload().getBlocks().get(msgBlockSize - 1);
            peerHandlerManager.tell(new GetBlocksMessage(new GetBlocksPayload(Arrays.asList(msgLastBlock.id()), UInt256.Zero())).pack(), getSelf());
        }
    }

    private void processTransactionsMessage(TransactionsMessage msg) {
        log.info("received " + msg.getTxs().getTxs().size() + " transactions from network");
        msg.getTxs().getTxs().forEach(tx -> chain.addTransaction(tx));
        // TODO: for the new txs broadcast INV
    }

    private void processInventoryMessage(InventoryMessage msg) {
        log.info("received Inventory");
        if (msg.getInv().getInvType() == InventoryType.Block) {
            List<UInt256> newBlocks = Lists.newArrayList();
            msg.getInv().getHashs().forEach(h -> {
                if (chain.containsBlock(h) == false) {
                    newBlocks.add(h);
                }
            });
            if (newBlocks.size() > 0) {
                log.info("send GetDataMessage to request " + newBlocks.size() + " new blocks.  " + newBlocks.get(0).toString());
                peerHandlerManager.tell(new GetDataMessage(new InventoryPayload(InventoryType.Block, newBlocks)).pack(), getSelf());
            }
        } else if (msg.getInv().getInvType() == InventoryType.Tx) {
            peerHandlerManager.tell(new GetDataMessage(new InventoryPayload(InventoryType.Tx, msg.getInv().getHashs())).pack(), getSelf());
        }
    }

    private void processGetDataMessage(GetDataMessage msg) {
        log.info("received GetDataMessage");
        if (msg.getInv().getInvType() == InventoryType.Block) {
            int sendMax = 10;
            int sentNum = 0;
            List<Block> blocks = Lists.newArrayList();
            int hashNum = msg.getInv().getHashs().size();
            for (int i = 0; i < hashNum; i++) {
                UInt256 h = msg.getInv().getHashs().get(i);
                Block block = chain.getBlock(h);
                if (block != null) {
                    if (sentNum < sendMax) {
                        blocks.add(block);
                        sentNum += 1;
                    }
                } else {
                    log.error("received GetDataMessage but block not found");
                }
            }
            if (blocks.size() > 0) {
                peerHandlerManager.tell(new BlocksMessage(new BlocksPayload(blocks)).pack(), getSelf());
            }
        } else if (msg.getInv().getInvType() == InventoryType.Tx) {
            List<Transaction> txs = Lists.newArrayList();
            int hashNum = msg.getInv().getHashs().size();
            for (int i = 0; i < hashNum; i++) {
                UInt256 h = msg.getInv().getHashs().get(i);
                Transaction tx = chain.getPendingTransaction(h);
                if (tx != null) {
                    txs.add(tx);
                }
            }
            if (txs.size() > 0) {
                peerHandlerManager.tell(new TransactionsMessage(new TransactionsPayload(txs)).pack(), getSelf());
            }
        }
    }

    private void sendGetBlocksMessage() {
        int index = chain.getHeight();
        int step = 1;
        int count = 0;
        List<UInt256> blockLocatorHashes = Lists.newArrayList();
        while (index > 0) {
            blockLocatorHashes.add(chain.getHeader(index).id());
            count += 1;
            if (count > 10)
                step *= 2;
            index -= step;
        }
        blockLocatorHashes.add(chain.getHeader(0).id());
        log.info("send GetBlocksMessage. Current status: " + chain.getHeight() + " " + chain.getLatestHeader().shortId());
        peerHandlerManager.tell(new GetBlocksMessage(new GetBlocksPayload(blockLocatorHashes, UInt256.Zero())).pack(), getSelf());
    }

    private void nodeStopMessage() {
        log.info("stopping node");
        chain.close();
        getContext().stop(getSelf());
    }

    /**
     * mongodb
     */

    public void addTransaction(Transaction tx) {
        notification.send(new AddTransactionNotify(tx), getSelf());
    }

    public void newBlockProduce(Block block) {
        //log.info("new block produce :"+block.getHeader().getId());
        notification.send(new NewBlockProducedNotify(block), getSelf());
    }

    public void blockAddedToHead(Block block) {
        //log.info("block add to head :"+block.getHeader().getId());
        notification.send(new BlockAddedToHeadNotify(block), getSelf());
    }

    public void confirmedBlock(Block block) {
        //log.info("confirmed block  :"+block.getHeader().getId());
        notification.send(new BlockConfirmedNotify(block), getSelf());
    }

    public void forkSwitch(List<ForkItem> from, List<ForkItem> to) {
        notification.send(new ForkSwitchNotify(from, to), getSelf());
    }
}
