package com.uchain.core.producer;

import com.uchain.core.Block;
import com.uchain.crypto.PublicKey;

import lombok.Getter;
import lombok.Setter;

public class ProduceStateImpl {
	
	@Getter
	@Setter
	public static class NotSynced implements ProduceState {
		private Long nextProduceTime;
		private Long currTime;
		
		public NotSynced(Long nextProduceTime, Long currTime) {
			this.nextProduceTime = nextProduceTime;
			this.currTime = currTime;
		}
	}
	@Getter
	@Setter
	public static class NotYet implements ProduceState {
		private Long nextProduceTime;
		private Long currTime;
		
		public NotYet(Long nextProduceTime, Long currTime) {
			super();
			this.nextProduceTime = nextProduceTime;
			this.currTime = currTime;
		}
	}
	
	@Getter
	@Setter
	public static class NotMyTurn implements ProduceState {
		private String producer;
		private PublicKey pubKey;
		
		public NotMyTurn(String producer, PublicKey pubKey) {
			this.producer = producer;
			this.pubKey = pubKey;
		}
	}
	
	@Getter
	@Setter
	public static class TimeMissed implements ProduceState {
		private Long thisProduceTime;
		private Long currTime;
		
		public TimeMissed(Long thisProduceTime, Long currTime) {
			this.thisProduceTime = thisProduceTime;
			this.currTime = currTime;
		}
	}
	
	@Getter
	@Setter
	public static class Success implements ProduceState {
		private Block block;
		private String producer;
		private Long time;
		
		public Success(Block block, String producer, Long time) {
			this.block = block;
			this.producer = producer;
			this.time = time;
		}
	}
	
	@Getter
	public static class Failed implements ProduceState {
		private Throwable e;

		public Failed(Throwable e) {
			this.e = e;
		}
	}
}
