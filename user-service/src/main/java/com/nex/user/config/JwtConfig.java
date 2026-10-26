package com.nex.user.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 鉴权
 */
@Configuration
public class JwtConfig {

    /**
     * 从 SecurityProperties 中获取配置文件绑定的 JWT 密钥。
     */
    @Bean
    protected SecretKey secretKey(JwtProperties properties) {

        String secretBase64 = properties.getSecretBase64();

        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret-base64 must be configured"
            );
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretBase64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("jwt.secret-base64 must be valid Base64", ex);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * JWT 编码器：供 JwtService 签发 Token 使用。
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey)
        );
    }
}
