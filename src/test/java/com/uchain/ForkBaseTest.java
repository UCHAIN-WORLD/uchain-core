package com.uchain;

import com.uchain.core.block.Block;
import com.uchain.core.block.BlockHeader;
import com.uchain.core.SwitchResult;
import com.uchain.core.transaction.Transaction;
import com.uchain.core.consensus.ForkBase;
import com.uchain.core.consensus.ForkItem;
import com.uchain.core.consensus.ConfirmedBlock;
import com.uchain.core.consensus.OnSwitchBlock;
import com.uchain.cryptohash.BinaryData;
import com.uchain.cryptohash.PrivateKey;
import com.uchain.cryptohash.PublicKey;
import com.uchain.cryptohash.UInt256;
import com.uchain.main.Settings;
import com.uchain.main.Witness;
import lombok.val;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForkBaseTest {

    private static PublicKey PubA = PublicKey.apply(new BinaryData("022ac01a1ea9275241615ea6369c85b41e2016abc47485ec616c3c583f1b92a5c8"));
    private static PrivateKey PriA = PrivateKey.apply(new BinaryData("efc382ccc0358f468c2a80f3738211be98e5ae419fc0907cb2f51d3334001471"));
    private static PublicKey PubB = PublicKey.apply(new BinaryData("0238eb90b322fac718ce10b21d451d00b7003a2a1de2a1d584a158d7b7ffee297b"));
    private static PrivateKey PriB = PrivateKey.apply(new BinaryData("485cfb9f743d9997e316f5dca216b1c6adf12aa301c1d520e020269debbebbf0"));
    private static PublicKey PubC = PublicKey.apply(new BinaryData("0234b9b7d2909231d143a6693082665837965438fc273fbc4c507996e41394c8c1"));
    private static PrivateKey PriC = PrivateKey.apply(new BinaryData("5dfee6af4775e9635c67e1cea1ed617efb6d22ca85abfa97951771d47934aaa0"));
    private List<Witness> witnesses = new ArrayList<Witness>();

    public ForkBaseTest() {
        Witness A = new Witness();
        A.setName("A");
        A.setPubkey("022ac01a1ea9275241615ea6369c85b41e2016abc47485ec616c3c583f1b92a5c8");
        A.setPrivkey("efc382ccc0358f468c2a80f3738211be98e5ae419fc0907cb2f51d3334001471");
        this.witnesses.add(A);
        Witness B = new Witness();
        B.setName("B");
        B.setPubkey("0238eb90b322fac718ce10b21d451d00b7003a2a1de2a1d584a158d7b7ffee297b");
        B.setPrivkey("485cfb9f743d9997e316f5dca216b1c6adf12aa301c1d520e020269debbebbf0");
        this.witnesses.add(B);
    }

    private Block genesis = ForkBaseTest.genesisBlock();
    private static final List<String> dirs = new ArrayList<String>();
    private static final List<ForkBase>  dbs    = new ArrayList<ForkBase>();


    @Test
    public void testHead() throws Exception{
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        ForkBase forkBase = ForkBaseTest.open("test_forkBase_head", witnesses,false);
        assert(forkBase.head()==null);
        forkBase.add(genesis);
        assert(forkBase.head().getBlock().equals(genesis));
        forkBase.add(blk1a);
        assert(forkBase.head().getBlock().equals(blk1a));
        forkBase.add(blk2a);
        assert(forkBase.head().getBlock().equals(blk2a));
        forkBase.add(blk3a);
        assert(forkBase.head().getBlock().equals(blk3a));
        forkBase.add(blk4a);
        assert(forkBase.head().getBlock().equals(blk4a));
        forkBase.add(blk3b);
        assert(forkBase.head().getBlock().equals(blk3b));
        forkBase.add(blk4b);
        assert(forkBase.head().getBlock().equals(blk4b));
    }

    @Test
    public void testGet()throws Exception{
        ForkBase forkBase = ForkBaseTest.open("test_forkBase_get", witnesses,false);
        assertBlock(forkBase,genesis);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        assertBlock(forkBase,blk1a);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        assertBlock(forkBase,blk2a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        assertBlock(forkBase,blk3a);
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        assertBlock(forkBase,blk3b, false, true, true, true);
    }

    public void assertBlock(ForkBase forkBase,Block block){
        assertBlock(forkBase,block,false,false,true,true);
    }

    public void assertBlock(ForkBase forkBase,Block block,Boolean beforeId,Boolean beforeHeight, Boolean afterId, Boolean afterHeight){
        assert((forkBase.get(block.id())!=null) == beforeId);
        assert((forkBase.get(block.height())!=null) == beforeHeight);
        forkBase.add(block);
        assert((forkBase.get(block.id())!=null) == afterId);
        assert((forkBase.get(block.height())!=null) == afterHeight);
    }

    @Test
    public void testGetNext() throws Exception{
        ForkBase forkBase = ForkBaseTest.open("test_forkBase_next", witnesses,false);
        assert(forkBase.getNext(genesis.id())==null);
        forkBase.add(genesis);

        assert(forkBase.getNext(genesis.id())==null);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk1b = ForkBaseTest.newBlock(PubB, PriB, genesis);
        Block blk2b = ForkBaseTest.newBlock(PubB, PriB, blk1b);
        forkBase.add(blk1a);
        assert(forkBase.getNext(genesis.id()).equals(blk1a.id()));
        forkBase.add(blk1b);
        assert(forkBase.getNext(genesis.id()).equals(blk1a.id()));
        forkBase.add(blk2b);
        assert(forkBase.getNext(genesis.id()).equals(blk1b.id()));
    }

    @Test
    public void testAdd()throws Exception{
        ForkBase forkBase = ForkBaseTest.open("test_forkBase_add", witnesses,false);
        assert(forkBase.add(genesis));
        assert(!forkBase.add(genesis));
        val blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        assert(forkBase.add(blk1a));
        val blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        val blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        assert(!forkBase.add(blk3a));
    }

    @Test
    public void testSwitch() throws Exception{
        Witness C = new Witness();
        C.setName("C");
        C.setPubkey("0234b9b7d2909231d143a6693082665837965438fc273fbc4c507996e41394c8c1");
        C.setPrivkey("5dfee6af4775e9635c67e1cea1ed617efb6d22ca85abfa97951771d47934aaa0");
        this.witnesses.add(C);
        ForkBase forkBase = ForkBaseTest.open("test_forkBase_switch", witnesses,false);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        Block blk5a = ForkBaseTest.newBlock(PubA, PriA, blk4a);
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        Block blk4c = ForkBaseTest.newBlock(PubC, PriC, blk3b);
        forkBase.add(genesis);
        forkBase.add(blk1a);
        forkBase.add(blk2a);
        forkBase.add(blk3b);
        forkBase.add(blk4b);
        assert(forkBase.get(blk1a.id()).isMaster());
        assert(forkBase.get(blk2a.id()).isMaster());
        assert(forkBase.get(blk3b.id()).isMaster());
        assert(forkBase.get(blk4b.id()).isMaster());
        assert(forkBase.head().id().equals(blk4b.id()));
        forkBase.add(blk3a);
        forkBase.add(blk4a);
        assert(forkBase.get(blk1a.id()).isMaster());
        assert(forkBase.get(blk2a.id()).isMaster());
        assert(!forkBase.get(blk3a.id()).isMaster());
        assert(!forkBase.get(blk4a.id()).isMaster());
        assert(forkBase.head().id().equals(blk4b.id()));
        forkBase.add(blk5a);
        assert(!forkBase.get(blk3b.id()).isMaster());
        assert(!forkBase.get(blk4b.id()).isMaster());
        assert(forkBase.get(blk3a.id()).isMaster());
        assert(forkBase.get(blk4a.id()).isMaster());
        assert(forkBase.get(blk5a.id()).isMaster());
        forkBase.add(blk4c);
        assert(!forkBase.get(blk4b.id()).isMaster());
        assert(!forkBase.get(blk3a.id()).isMaster());
        assert(!forkBase.get(blk4a.id()).isMaster());
        assert(!forkBase.get(blk5a.id()).isMaster());
        assert(forkBase.get(blk3b.id()).isMaster());
        assert(forkBase.get(blk4c.id()).isMaster());
    }

    @Test
    public void testSwitchFailed() throws Exception{
        Witness C = new Witness();
        C.setName("C");
        C.setPubkey("0234b9b7d2909231d143a6693082665837965438fc273fbc4c507996e41394c8c1");
        C.setPrivkey("5dfee6af4775e9635c67e1cea1ed617efb6d22ca85abfa97951771d47934aaa0");
        this.witnesses.add(C);

        ForkBase forkBase = ForkBaseTest.open("test_forkBase_switch", witnesses,true);
        val blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        val blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        val blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        val blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        val blk5a = ForkBaseTest.newBlock(PubA, PriA, blk4a);
        val blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        val blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        val blk4c = ForkBaseTest.newBlock(PubC, PriC, blk3b);

        forkBase.add(genesis);
        forkBase.add(blk1a);
        forkBase.add(blk2a);
        forkBase.add(blk3b);
        forkBase.add(blk4b);
        assert(forkBase.get(blk1a.id()).isMaster());
        assert(forkBase.get(blk2a.id()).isMaster());
        assert(forkBase.get(blk3b.id()).isMaster());
        assert(forkBase.get(blk4b.id()).isMaster());
        assert(forkBase.head().id().equals(blk4b.id()));
        forkBase.add(blk3a);
        forkBase.add(blk4a);
        assert(forkBase.get(blk1a.id()).isMaster());
        assert(forkBase.get(blk2a.id()).isMaster());
        assert(!forkBase.get(blk3a.id()).isMaster());
        assert(!forkBase.get(blk4a.id()).isMaster());
        assert(forkBase.head().id().equals(blk4b.id()));
        forkBase.add(blk5a);
        assert(!forkBase.get(blk3b.id()).isMaster());
        assert(!forkBase.get(blk4b.id()).isMaster());
        assert(forkBase.get(blk3a.id()).isMaster());
        assert(forkBase.get(blk4a.id()).isMaster());
        assert(forkBase.get(blk5a.id()).isMaster());
        assert(forkBase.head().id().equals(blk5a.id()));
        forkBase.add(blk4c);
        assert(forkBase.get(blk1a.id()).isMaster());
        assert(forkBase.get(blk2a.id()).isMaster());
        assert(forkBase.get(blk3a.id()).isMaster());
        assert(forkBase.get(blk4a.id()).isMaster());
        assert(forkBase.get(blk5a.id()).isMaster());
        assert(!forkBase.get(blk3b.id()).isMaster());
        assert(!forkBase.get(blk4b.id()).isMaster());
        assert(forkBase.get(blk4c.id())==null);
        assert(forkBase.head().id().equals(blk5a.id()));
    }

    @Test
    public void testRemoveFork()throws Exception{
        Witness C = new Witness();
        C.setName("C");
        C.setPubkey("0234b9b7d2909231d143a6693082665837965438fc273fbc4c507996e41394c8c1");
        C.setPrivkey("5dfee6af4775e9635c67e1cea1ed617efb6d22ca85abfa97951771d47934aaa0");
        this.witnesses.add(C);
        ForkBase forkBase = ForkBaseTest.open("test_forkBase_removeFork", witnesses,false);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        Block blk5a = ForkBaseTest.newBlock(PubA, PriA, blk4a);
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        Block blk3c = ForkBaseTest.newBlock(PubC, PriC, blk2a);
        forkBase.add(genesis);
        forkBase.add(blk1a);
        forkBase.add(blk2a);
        forkBase.add(blk3b);
        forkBase.add(blk4b);
        forkBase.add(blk3a);
        forkBase.add(blk4a);
        forkBase.add(blk5a);
        assert(forkBase.removeFork(blk4a.id()));
        assert(forkBase.get(blk1a.id())!=null);
        assert(forkBase.get(blk2a.id())!=null);
        assert(forkBase.get(blk3a.id())!=null);
        assert(forkBase.get(blk3b.id())!=null);
        assert(forkBase.get(blk4b.id())!=null);
        assert(forkBase.get(blk4a.id())==null);
        assert(forkBase.get(blk5a.id())==null);
        assert(forkBase.removeFork(blk2a.id()));
        assert(forkBase.get(blk1a.id())!=null);
        assert(forkBase.get(blk2a.id())==null);
        assert(forkBase.get(blk3a.id())==null);
        assert(forkBase.get(blk3b.id())==null);
        assert(forkBase.get(blk4b.id())==null);
        assert(!forkBase.removeFork(blk3c.id()));
    }
    @Test
    public void testFork()throws Exception{
        ForkBase forkBase = ForkBaseTest.open("test_forkBase_fork", witnesses,false);
        Block blk1a = ForkBaseTest.newBlock(PubA, PriA, genesis);
        Block blk2a = ForkBaseTest.newBlock(PubA, PriA, blk1a);
        Block blk3a = ForkBaseTest.newBlock(PubA, PriA, blk2a);
        Block blk4a = ForkBaseTest.newBlock(PubA, PriA, blk3a);
        forkBase.add(genesis);
        forkBase.add(blk1a);
        assert(forkBase.head().getBlock().id().equals(blk1a.id()));
        forkBase.add(blk2a);
        assert(forkBase.head().getBlock().id().equals(blk2a.id()));
        forkBase.add(blk3a);
        assert(forkBase.head().getBlock().id().equals(blk3a.id()));
        Block blk3b = ForkBaseTest.newBlock(PubB, PriB, blk2a);
        Block blk4b = ForkBaseTest.newBlock(PubB, PriB, blk3b);
        forkBase.add(blk3b);
        assert(forkBase.head().getBlock().id().equals(blk3b.id()));
        assert(forkBase.get(blk3a.id()).isMaster() == false);
        assert(forkBase.get(blk3b.id()).isMaster() == true);
        forkBase.add(blk4a);
        assert(forkBase.head().getBlock().id().equals(blk3b.id()));
        forkBase.add(blk4b);
        assert(forkBase.head().getBlock().id().equals(blk4b.id()));
    }

    @AfterClass
    public static void cleanUp(){
        dbs.forEach(dbtmp->{dbtmp.close();});
        dirs.forEach(dirtmp->deleteDir(dirtmp));
    }

    private static void deleteDir(String dir){
        try {
            File scFileDir = new File(dir);
            File TrxFiles[] = scFileDir.listFiles();
            for(File curFile:TrxFiles ){
                curFile.delete();
            }
            scFileDir.delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Block genesisBlock() {
        PublicKey pub = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"));
        PrivateKey pri = PrivateKey.apply(new BinaryData("7a93d447bffe6d89e690f529a3a0bdff8ff6169172458e04849ef1d4eafd7f86"));

        BlockHeader genesisHeader = BlockHeader.build(
                0, Instant.now().toEpochMilli(),
                UInt256.Zero(), UInt256.Zero(), pub, pri);
        List<Transaction> queue = new ArrayList<Transaction>();
        return Block.build(genesisHeader, queue);
    }

    private static Block newBlock(PublicKey pub, PrivateKey pri, Block prevBlock) throws IOException {
        UInt256 root = null;
        try {
            root = SerializerTest.testHash256("test");
        } catch (IOException e) {
            e.printStackTrace();
        }
        long timeStamp = Instant.now().toEpochMilli();
        BlockHeader header = BlockHeader.build(
                prevBlock.height() + 1, timeStamp,
                root, prevBlock.id(), pub, pri);
        List<Transaction> queue = new ArrayList<Transaction>();
        return Block.build(header, queue);
    }

    private static boolean applyBlock(Block blk) {
        return !blk.getHeader().getProducer().equals(PubC);
    }
    public static ForkBase open(String dir, List<Witness> witnesses,boolean flag) throws java.text.ParseException {
        Settings settings = new Settings("src/main/resources/config.properties");
        settings.getChainSettings().getForkBaseSettings().setDir(dir);
        settings.getConsensusSettings().setWitnessList(witnesses);

        ConfirmedBlock funcConfirmed = block -> System.out.println("confirm block "+block.height());
        OnSwitchBlock funcOnSwitch;

        funcOnSwitch = (a, b,c) -> {
            if(flag) {
                System.out.println("switch");
                System.out.println(forkStr("from", a));
                System.out.println(forkStr("to", b));
                int i = (int) b.stream().filter(forkItem -> applyBlock(forkItem.getBlock())).count();
                SwitchResult switchResult;
                if (i < b.size()) {
                    switchResult = new SwitchResult(false, b.get(i));
                } else {
                    switchResult = new SwitchResult(true, null);
                }
                return switchResult;
            }else {
                System.out.println("switch");
                System.out.println(forkStr("from", a));
                System.out.println(forkStr("to", b));
                SwitchResult switchResult = new SwitchResult(true, null);
                return switchResult;
            }
        };

        ForkBase forkBase = new ForkBase(settings,funcConfirmed,funcOnSwitch);

        dbs.add(forkBase);
        dirs.add(dir);
        return forkBase;
    }

    private static String forkStr(String title, List<ForkItem> fork) {
        String str = fork.stream().map(blk->
            blk.getBlock().height()+"("+blk.getBlock().id().toString().substring(0,6)+")"
        ).collect(Collectors.joining("<-"));
        str = title+":"+str;
        return str;
    }
}


