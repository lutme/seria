package com.nex.provider;

import com.nex.common.snowflake.SnowflakeIdGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdRunner implements CommandLineRunner {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public SnowflakeIdRunner(SnowflakeIdGenerator generator) {
        this.snowflakeIdGenerator = generator;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("生成的分布式ID：" + snowflakeIdGenerator.nextId());
        System.out.println("生成的分布式ID：" + snowflakeIdGenerator.nextId());
    }
}
