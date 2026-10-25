package com.nex.common.snowflake;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "snowflake")
public class SnowflakeIdProperties {

    /**
     * 工作节点编号，范围 0～31
     */
    private long workerId;

    /**
     * 数据中心编号，范围 0～31
     */
    private long datacenterId;

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
    }
}
