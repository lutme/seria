package com.nex.consumer.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * gateway 染色校验过滤器
 * 校验 gateway token 以防越过 gateway 直接访问服务
 */

@Component
public class GatewayTokenFilter extends OncePerRequestFilter {

    private static final String GATEWAY_TOKEN_HEADER = "X-Internal-Gateway-Token";

    private final String gatewayToken;

    public GatewayTokenFilter(@Value("${security.gateway.token}") String gatewayToken) {

        if (gatewayToken == null || gatewayToken.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration: 'security.gateway.token'. "
                            + "Please configure it in application.yml or application.properties"
            );
        }

        this.gatewayToken = gatewayToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 获取请求头
        String requestToken = request.getHeader(GATEWAY_TOKEN_HEADER);

        boolean valid = requestToken != null
                && MessageDigest.isEqual(
                gatewayToken.getBytes(StandardCharsets.UTF_8),
                requestToken.getBytes(StandardCharsets.UTF_8)
        );

        if (!valid) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            // 这里是简化了返回数据结构 实际情况可以根据项目封装的 Result 进行处理
            response.getWriter().write("{\"code\":403,\"message\":\"Requests to services must not bypass the gateway\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
