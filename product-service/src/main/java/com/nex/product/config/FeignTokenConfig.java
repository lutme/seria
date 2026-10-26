package com.nex.product.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign 调用传递 Gateway 染色 token
 */
@Configuration
public class FeignTokenConfig {

    private static final String GATEWAY_TOKEN_HEADER = "X-Internal-Gateway-Token";

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${gateway.stamp.token}") String gatewayToken) {

        return template -> template.header(GATEWAY_TOKEN_HEADER, gatewayToken);
    }
}
