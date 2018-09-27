package com.uchain.core.producer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.uchain.core.Block;
import com.uchain.core.BlockChain;
import com.uchain.core.Transaction;
import com.uchain.core.producer.ProduceStateImpl.*;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt256;
import com.uchain.main.ConsensusSettings;
import com.uchain.main.Witness;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Producer extends AbstractActor {

	private ConsensusSettings settings;
	private ActorRef peerManager;
	private BlockChain chain;

	public Producer(ConsensusSettings settings, BlockChain chain, ActorRef peerManager) {
		this.settings = settings;
		this.peerManager = peerManager;
		this.chain = chain;
	}

	public static Props props(ConsensusSettings settings, BlockChain chain, ActorRef peerManager) {
		return Props.create(Producer.class, settings, chain, peerManager);
	}

	private Map<UInt256, Transaction> txPool = new HashMap<UInt256, Transaction>();
	private boolean canProduce = false;

	@Override
	public void preStart() throws Exception {
		ProduceTask task = new ProduceTask(this, peerManager, canProduce);
		getContext().system().scheduler().scheduleOnce(Duration.ZERO, task, getContext().system().dispatcher());
	}

	/**
	 * 同步最新的区块完后开始加入生产区块
	 * @return
	 */
	 public ProduceState produce() {
		 try {
			 Long now = Instant.now().toEpochMilli(); //精确到毫秒
			 if(canProduce) {
				 return tryProduce(now);
			 }else {
				 Long next = nextBlockTime(1);
				 if (next >= now) {
					 canProduce = true;
					 return tryProduce(now);
				 }else {
					 return new NotSynced(next, now);
				 }
			 }
		 }catch(Exception e) {
			 return new Failed(e);
		 }
	 }

	 /**
	  * 满足时间，且此刻轮训到当前节点，开始生产区块
	  * @param now
	  * @return
	  */
	private ProduceState tryProduce(Long now) {
		Long next = nextBlockTime(1);
		if (now + settings.getAcceptableTimeError() < next) {
			return new NotYet(next,now);
		}else {
		    Witness witness = getWitness(nextProduceTime(now, next));
		    if("".equals(witness.getPrivkey())) {
		    	return new NotMyTurn(witness.getName(), PublicKey.apply(new BinaryData(witness.getPubkey())));
		    }else {
				Collection<Transaction> valueCollection = txPool.values();
				List<Transaction> txs = new ArrayList<>(valueCollection);
				Block block = chain.produceBlock(PublicKey.apply(new BinaryData(witness.getPubkey())),
						PrivateKey.apply(new BinaryData(witness.getPrivkey())), nextProduceTime(now, next), txs);
				txPool.clear();
				return new Success(block, witness.getName(), now);
		    }
		}
	}
	
	/**
	 * 根据上一个区块时间，获取下一个区块期望时间
	 * @param nextN
	 * @return
	 */
	private Long nextBlockTime(int nextN) {
		if(nextN == 0) nextN =1;
		long headTime = chain.getLatestHeader().getTimeStamp();
		long slot = headTime / settings.getProduceInterval(); //ProduceInterval 生成区块间隔时间
		slot += nextN;
		return slot * settings.getProduceInterval();
	}
	
	/**
	 * 下一个区块生产时间
	 * @param now
	 * @param next
	 * @return
	 */
	private Long nextProduceTime(Long now,Long next) {
		if (now <= next) {
			return next;
		}else {
			long slot = now / settings.getProduceInterval();
			return slot * settings.getProduceInterval();
		}
	}
	/**
	 * 获取给定时间点的生产者
	 * @param timeMs time from 1970 in ms
	 * @return
	 */
	private Witness getWitness(Long timeMs) {
		long slot = timeMs / settings.getProduceInterval();
		long index = slot % (settings.getWitnessList().size() * settings.getProducerRepetitions());
		index /= settings.getProducerRepetitions();
		return settings.getWitnessList().get((int)index); //获取
	}
	
	/**
	 * 洗牌
	 * @param nowSec
	 * @param witnesses
	 * @return
	 */
	private Witness[] updateWitnessSchedule(Long nowSec, Witness[] witnesses) {
		Witness[] newWitness = witnesses;
		BigInteger nowHi = new BigInteger(nowSec.toString()).shiftLeft(32); // this << n
		BigInteger param = new BigInteger("2685821657736338717");
		int witnessNum = newWitness.length;
		for (int i = 0; i < witnessNum; i++) {
			BigInteger ii = BigInteger.valueOf(i);
			BigInteger k = ii.multiply(param).add(nowHi);
			k = k.xor(k.shiftRight(12));
			k = k.xor(k.shiftLeft(25));
			k = k.xor(k.shiftRight(27));
			k = k.multiply(param);

			int jmax = newWitness.length - i;
			int j = k.remainder(BigInteger.valueOf(jmax)).add(ii).intValue();

			Witness a = newWitness[i];
			Witness b = newWitness[j];
			newWitness[i] = b;
			newWitness[j] = a;
		}
		return newWitness;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Object.class, msg -> {
		}).build();
	}


}
