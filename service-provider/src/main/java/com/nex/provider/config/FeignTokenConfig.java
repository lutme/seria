package com.nex.provider.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTokenConfig {

    private static final String GATEWAY_TOKEN_HEADER = "X-Internal-Gateway-Token";

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${security.gateway.token}") String gatewayToken) {

        return template -> template.header(GATEWAY_TOKEN_HEADER, gatewayToken);
    }
}
