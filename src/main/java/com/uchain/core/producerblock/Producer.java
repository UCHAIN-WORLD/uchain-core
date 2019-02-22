package com.uchain.core.producerblock;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Scheduler;
import com.uchain.core.block.BlockChain;
import com.uchain.core.producerblock.ProduceStateImpl.ProducerStopMessage;
import com.uchain.main.ConsensusSettings;
import com.uchain.main.Witness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Producer extends AbstractActor {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);
    private ConsensusSettings settings;
    private Scheduler scheduler = getContext().system().scheduler();
    private ActorRef node = getContext().getParent();

    private int blocksPerRound;
    private int minProducingTime;
    private int earlyMS;

    private FiniteDuration delayOneBlock;
    private FiniteDuration noDelay;
    private boolean enableProduce = false;

    public static Props props(ConsensusSettings settings) {
        return Props.create(Producer.class, settings);
    }

    public Producer(ConsensusSettings settings) {
        this.settings = settings;
        blocksPerRound = settings.getWitnessList().size() * settings.getProducerRepetitions();
        minProducingTime = settings.getProduceInterval() / 10;
        earlyMS = settings.getProduceInterval() / 5;
        delayOneBlock = blockDuration(1);
        noDelay = blockDuration(0);
        scheduleBegin(null);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ProducerStopMessage.class, msg -> {
            log.info("stopping producer task");
            getContext().stop(getSelf());
        }).build();
    }

    /**
     * 调度开始生产区块
     * @param duration
     */
    private void scheduleBegin(FiniteDuration duration) {
        FiniteDuration dur;
        if (duration != null) {
            dur = duration;
        } else {
            dur = noDelay;
        }
        ProduceTaskBegin produceTaskBegin = this::beginProduce;
        //执行一次调度
        scheduler.scheduleOnce(dur, node, produceTaskBegin, getContext().system().dispatcher(), ActorRef.noSender());
    }

    private void scheduleEnd(FiniteDuration duration) {
        FiniteDuration dur;
        if (duration != null) {
            dur = duration;
        } else {
            dur = noDelay;
        }
        ProduceTaskEnd produceTaskEnd = this::endProduce;
        scheduler.scheduleOnce(dur, node, produceTaskEnd, getContext().system().dispatcher(), ActorRef.noSender());
    }

    private void beginProduce(BlockChain chain) {
        if (!maybeProduce(chain)) {
            scheduleBegin(delayOneBlock);
        }
    }

    /**
     * 产生块后继续轮训产生块
     * @param chain
     */
    private void endProduce(BlockChain chain) {
        chain.produceBlockFinalize(Instant.now().toEpochMilli());
        scheduleBegin(null);
    }

    private boolean maybeProduce(BlockChain chain) {
        try {
            if (enableProduce) {
                producing(chain);
                return true;
            } else if (Instant.now().toEpochMilli() <= nextTime(chain.getHeadTime())) {
                enableProduce = true;
                producing(chain);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.debug("begin produce failed", e);
            return false;
        }
    }

    /**
     * 开始生产区块
     * @param chain
     */
    private void producing(BlockChain chain) {
        long headTime = chain.getHeadTime();
        long now = Instant.now().toEpochMilli();
        if (headTime - now > settings.getProduceInterval()) {
            scheduleBegin(delayOneBlock);
        } else {
            long next = nextBlockTime(headTime, now);
            Witness witness = getWitness(next);
            if (witness.getPrivkey().isEmpty()) {//私钥为空，没有轮到执行窗口
                FiniteDuration delay = calcDelay(Instant.now().toEpochMilli(), next);
                scheduleBegin(delay);
            } else {//产生区块后继续轮训
                chain.startProduceBlock(witness, next);
                FiniteDuration delay = calcDelay(Instant.now().toEpochMilli(), next);
                scheduleEnd(delay);
            }
        }
    }

    /**
     * 获取下一个区块期望时间
     *
     * @param headTime
     * @param now
     * @return
     */
    private long nextBlockTime(long headTime, long now) {
        long next = nextTime(Math.max(headTime, now));
        if (next - now < minProducingTime) {
            next += settings.getProduceInterval();
        }
        return next;
    }

    private long nextTime(long time) {
        return time + settings.getProduceInterval() - time % settings.getProduceInterval();
    }

    private FiniteDuration calcDelay(long now, long next) {
        long delay = next - now;
        // produce last block in advance
        Integer rest = restBlocks(next);
        if (rest == 1) {
            if (delay < earlyMS) {
                delay = 0;
            } else {
                delay = delay - earlyMS;
            }
        }
        return calcDuration(Math.toIntExact(delay));
    }

    private Integer restBlocks(long time) {
        return Math.toIntExact(settings.getProducerRepetitions() - time / settings.getProduceInterval() % settings.getProducerRepetitions());
    }


    private FiniteDuration blockDuration(Integer blocks) {
        return new FiniteDuration(blocks * settings.getProduceInterval() * 1000, TimeUnit.MICROSECONDS);
    }

    private FiniteDuration calcDuration(Integer delay) {
        return new FiniteDuration(delay * 1000, TimeUnit.MICROSECONDS);
    }


    private Witness getWitness(long time) {
        long slot = time / settings.getProduceInterval() % blocksPerRound;
        long index = slot / settings.getProducerRepetitions();
        return settings.getWitnessList().get((int) index);
    }
}
