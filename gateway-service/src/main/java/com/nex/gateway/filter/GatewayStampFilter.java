package com.nex.gateway.filter;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求染色过滤器 用于服务校验
 */
@Component
public class GatewayStampFilter implements GlobalFilter, Ordered {

    private static final String GATEWAY_TOKEN_HEADER = "X-Internal-Gateway-Token";

    private final String gatewayToken;

    public GatewayStampFilter(@Value("${gateway.stamp.token}") String gatewayToken) {

        if (gatewayToken == null || gatewayToken.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration: 'security.gateway.token'. "
                            + "Please configure it in application.yml or application.properties"
            );
        }

        this.gatewayToken = gatewayToken;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {

        // 1. 移除客户端传入的伪造标识
        ServerHttpRequest req = exchange.getRequest()
                .mutate()
                .headers(httpHeaders -> httpHeaders.remove(GATEWAY_TOKEN_HEADER))
                .header(GATEWAY_TOKEN_HEADER, gatewayToken)
                .build();

        // 2. 仅向后端转发带有可信标识的请求
        return chain.filter(exchange.mutate().request(req).build());
    }

    /**
     * 返回 -2 是为了排在接口请求日志过滤器之前
     * @return
     */
    @Override
    public int getOrder() {
        return -2;
    }
}
