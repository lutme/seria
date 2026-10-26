package com.nex.common.snowflake;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "snowflake")
@Data
public class SnowflakeIdProperties {

    /**
     * 工作节点编号，范围 0～31
     */
    private long workerId;

    /**
     * 数据中心编号，范围 0～31
     */
    private long datacenterId;

}
