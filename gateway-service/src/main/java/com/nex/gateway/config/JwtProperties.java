package com.nex.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Jwt 配置
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secretBase64;

    private long expirationMinutes;

    private List<String> whitelist = new ArrayList<>();

}
