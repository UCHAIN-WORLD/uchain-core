package com.uchain.core.producer;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uchain.core.Block;
import com.uchain.core.producer.ProduceStateImpl.Failed;
import com.uchain.core.producer.ProduceStateImpl.NotMyTurn;
import com.uchain.core.producer.ProduceStateImpl.NotSynced;
import com.uchain.core.producer.ProduceStateImpl.NotYet;
import com.uchain.core.producer.ProduceStateImpl.Success;
import com.uchain.core.producer.ProduceStateImpl.TimeMissed;
import com.uchain.network.message.BlockMessageImpl.BlockMessage;

import akka.actor.ActorRef;

/**
 * 启动线程，生产区块，并发送
 *
 */
public class ProduceTask implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ProduceTask.class);
	private Producer producer;
	private ActorRef peerManager;
	private boolean cancelled;

	
	public ProduceTask(Producer producer, ActorRef peerManager, boolean cancelled) {
		this.producer = producer;
		this.peerManager = peerManager;
		this.cancelled = cancelled;
	}

	public void cancel() {
		cancelled = true;
	}
	@Override
	public void run() {
		 while (!cancelled) {
			 long sleep = 500 - Instant.now().toEpochMilli() % 500;
		     if (sleep < 10) 
		    	 sleep = sleep + 500;
		     try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		     
		     while (!cancelled) {
		    	 ProduceState produceState = producer.produce();
		    	 if(produceState instanceof NotSynced) {
		    		 log.debug("not synced");
		    	 }else if(produceState instanceof NotYet) {
		    		 NotYet notYet = (NotYet)produceState;
		    		 log.debug("Not yet, next produce time:"+ notYet.getNextProduceTime()+", current time:"+notYet.getCurrTime());
		    	 }else if(produceState instanceof TimeMissed) {
		    		 TimeMissed timeMissed = (TimeMissed)produceState;
		    		 log.debug("missed, this produce time:"+ timeMissed.getThisProduceTime()+", current time:"+timeMissed.getCurrTime());
		    	 }else if(produceState instanceof NotMyTurn) {
		    		 NotMyTurn notMyTurn = (NotMyTurn)produceState;
		    		 log.debug("not my turn, ("+notMyTurn.getProducer()+")");
		    	 }else if(produceState instanceof Failed) {
		    		 Failed failed = (Failed)produceState;
		    		 log.error("error occurred when producing block", failed.getE());
		    	 }else if(produceState instanceof Success) {
		    		 Success success = (Success)produceState;
		    		 if(success.getBlock()!=null) {
		    			 Block blk = success.getBlock();
		    			 log.info("block ("+blk.height()+", "+blk.timeStamp()+") produced by "+success.getProducer()+" on "+success.getTime());
		    			 peerManager.tell(new BlockMessage(blk), ActorRef.noSender());
		    		 }else {
		    			 log.error("produce block failed");
		    		 }
		    	 }
		       }
		 }
	}
}
