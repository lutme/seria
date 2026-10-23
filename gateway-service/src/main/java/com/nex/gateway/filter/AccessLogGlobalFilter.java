package com.nex.gateway.filter;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 接口请求日志过滤器
 */
@Component
public class AccessLogGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AccessLogGlobalFilter.class);

    private static final String REQUEST_ID = "X-Request-Id";

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {

        // 1. 生成请求唯一标识
        String requestId = UUID.randomUUID().toString();

        // 2. 将请求 ID 放入响应头
        exchange.getResponse()
                .getHeaders()
                .set(REQUEST_ID, requestId);

        // 3. 获取请求信息
        String method = exchange.getRequest()
                .getMethod()
                .name();

        String path = exchange.getRequest()
                .getURI()
                .getRawPath();

        // 4. 记录请求开始时间
        long startTime = System.nanoTime();

        log.info("[Gateway] START requestId={} method={} path={}", requestId, method, path);

        // 5. 放行请求，执行后续过滤器和路由
        return chain.filter(exchange).doFinally(signalType -> {
                    // 6. 统计耗时
                    long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

                    // 7. 获取最终响应状态
                    HttpStatusCode status = exchange.getResponse().getStatusCode();

                    String statusCode = status == null ? "UNKNOWN" : String.valueOf(status.value());

                    // 8. 输出访问日志
                    log.info(
                            "[Gateway] END requestId={} method={} "
                                    + "path={} status={} cost={}ms signal={}",
                            requestId,
                            method,
                            path,
                            statusCode,
                            cost,
                            signalType
                    );
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
