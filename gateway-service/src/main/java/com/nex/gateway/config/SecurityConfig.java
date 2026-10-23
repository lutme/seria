package com.nex.gateway.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 鉴权
 */

@Configuration
public class SecurityConfig {

    /**
     * 从 SecurityProperties 中获取配置文件绑定的 JWT 密钥。
     */
    @Bean
    protected SecretKey secretKey(SecurityProperties properties) {

        String secretBase64 = properties.getJwt().getSecretBase64();

        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException(
                    "app.security.jwt.secret-base64 must be configured"
            );
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretBase64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("app.security.jwt.secret-base64 must be valid Base64", ex);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * JWT 解码器：验证 JWT 签名及有效期。
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * JWT 编码器：供 JwtService 签发 Token 使用。
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey)
        );
    }

    /**
     * 网关安全规则：白名单从配置文件读取，
     * 其余请求都必须通过 JWT 身份认证。
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         SecurityProperties properties,
                                                         JwtAuthenticationEntryPoint entryPoint) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> {
                    String[] whitelist = properties.getWhitelist().toArray(String[]::new);
                    if (whitelist.length > 0) {
                        exchange.pathMatchers(whitelist).permitAll();
                    }
                    // 其余请求必须携带有效 JWT
                    exchange.anyExchange().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(entryPoint)
                )
                .build();
    }
}
