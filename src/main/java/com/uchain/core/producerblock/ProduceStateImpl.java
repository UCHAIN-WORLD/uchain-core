package com.uchain.core.producerblock;

import lombok.Getter;
import lombok.Setter;

public class ProduceStateImpl {
	@Getter
	@Setter
	public static class Success implements ProduceState {
		private Long time;
		
		public Success(Long time) {
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

    public static class ProducerStopMessage {
    }

    public static class NodeStopMessage{}
}
