package com.nex.common.snowflake;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * 雪花算法生产分布式 id
 */
public final class SnowflakeIdGenerator {

    private final Snowflake snowflake;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {

        if (workerId < 0 || workerId > 31) {
            throw new IllegalArgumentException("workerId 必须在 0 到 31 之间");
        }

        if (datacenterId < 0 || datacenterId > 31) {
            throw new IllegalArgumentException("datacenterId 必须在 0 到 31 之间");
        }

        this.snowflake = IdUtil.getSnowflake(workerId, datacenterId);
    }

    /**
     * 生成一个分布式 ID
     */
    public long nextId() {
        return snowflake.nextId();
    }
}
