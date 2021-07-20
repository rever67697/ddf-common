package com.ddf.common.ids.service.service.impl.snowflake;

import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import com.ddf.common.ids.service.model.common.Status;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Objects;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 雪花id核心实现
 */
public class SnowflakeIDGenImpl implements IDGen {

    @Override
    public boolean init() {
        return true;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeIDGenImpl.class);

    private final long twepoch;
    private final long workerIdBits = 10L;
    private final long maxWorkerId = ~(-1L << workerIdBits);//最大能够分配的workerid =1023
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private static final Random RANDOM = new Random();

    public SnowflakeIDGenImpl(String zkAddress, int port) {
        //Thu Nov 04 2010 09:42:54 GMT+0800 (中国标准时间)
        this(zkAddress, port, 1288834974657L);
    }

    /**
     * @param zkAddress zk地址
     * @param port      snowflake监听端口
     * @param twepoch   起始的时间戳
     */
    public SnowflakeIDGenImpl(String zkAddress, int port, long twepoch) {
        this.twepoch = twepoch;
        Preconditions.checkArgument(timeGen() > twepoch, "Snowflake not support twepoch gt currentTime");
        final String ip = Utils.getIp();
        SnowflakeZookeeperHolder holder = new SnowflakeZookeeperHolder(ip, String.valueOf(port), zkAddress);
        LOGGER.info("twepoch:{} ,ip:{} ,zkAddress:{} port:{}", twepoch, ip, zkAddress, port);
        boolean initFlag = holder.init();
        if (initFlag) {
            workerId = holder.getWorkerID();
            LOGGER.info("START SUCCESS USE ZK WORKERID-{}", workerId);
        } else {
            Preconditions.checkArgument(initFlag, "Snowflake Id Gen is not init ok");
        }
        Preconditions.checkArgument(workerId >= 0 && workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
    }


    /**
     * 对于雪花id来说，这个key毫无意义，如果调用方需要这个作为前缀，自行处理
     *
     * @param key
     * @return
     */
    @Override
    public synchronized Result get(String key) {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        return new Result("-1", Status.EXCEPTION);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("wait interrupted");
                    return new Result("-2", Status.EXCEPTION);
                }
            } else {
                return new Result("-3", Status.EXCEPTION);
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return new Result(String.valueOf(id), Status.SUCCESS);

    }


    @Override
    public ResultList list(int length) {
        if (0 == length) {
            return new ResultList(-1, Status.EXCEPTION);
        }
        ResultList resultList = new ResultList();
        resultList.setStatus(Status.SUCCESS);
        resultList.setIdList(Lists.newArrayList());
        for (int i = 0; i < length; i++) {
            final Result result = get(null);
            if (Objects.equals(Status.EXCEPTION, result.getStatus())) {
                return new ResultList(Long.parseLong(result.getId()), Status.EXCEPTION);
            }
            resultList.getIdList().add(result.getId());
        }
        return resultList;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public long getWorkerId() {
        return workerId;
    }

}
