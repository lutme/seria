package com.nex.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 异常同意处理钩子
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    private HttpStatusCode resolveStatus(Throwable ex) {
        Throwable current = ex;

        while (current != null) {
            if (current instanceof ResponseStatusException rse) {
                return rse.getStatusCode();
            }

            if (current instanceof TimeoutException) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }

            if (current instanceof ConnectException) {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }

            // Gateway 找不到可用服务实例时也可能抛出此类异常
            if (current.getClass().getSimpleName()
                    .equals("NotFoundException")) {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }

            current = current.getCause();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveReason(Throwable ex) {
        Throwable current = ex;

        while (current != null) {
            if (current instanceof ResponseStatusException rse) {
                String reason = rse.getReason();

                if (reason != null && !reason.isBlank()) {
                    return reason;
                }
            }

            current = current.getCause();
        }

        assert ex != null;
        return ex.getMessage();
    }

    @Override
    public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {

        var response = exchange.getResponse();

        // 响应已经提交时，不能再修改状态码或响应体
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatusCode status = resolveStatus(ex);

        log.error(
                "Gateway request failed: method={}, path={}, status={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                status.value(),
                ex
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", status.value());
        body.put("message", resolveReason(ex));
        body.put("path", exchange.getRequest().getURI().getPath());
        body.put("timestamp", OffsetDateTime.now().toString());

        response.setStatusCode(status);
        response.getHeaders().setContentType(
                MediaType.APPLICATION_JSON
        );

        return Mono.fromCallable(
                () -> objectMapper.writeValueAsBytes(body)
        ).flatMap(bytes -> response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes))
        ));
    }
}
