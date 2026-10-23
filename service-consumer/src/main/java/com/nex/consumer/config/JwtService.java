package com.nex.consumer.config;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    private final SecurityProperties securityProperties;

    public JwtService(JwtEncoder jwtEncoder, SecurityProperties securityProperties) {
        this.jwtEncoder = jwtEncoder;
        this.securityProperties = securityProperties;
    }

    /**
     * 生成 JWT。
     *
     * @param key 登录成功后的唯一标识
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(String key) {

        // 1. 校验用户名
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "username must not be blank"
            );
        }

        // 2. 读取配置文件中的 Token 有效期
        Long expirationMinutes = securityProperties
                .getJwt()
                .getExpirationMinutes();

        if (expirationMinutes == null || expirationMinutes <= 0) {
            throw new IllegalStateException(
                    "app.security.jwt.expiration-minutes "
                            + "must be greater than 0"
            );
        }

        // 3. 计算签发时间和过期时间
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(
                Math.multiplyExact(expirationMinutes, 60L)
        );

        // 4. 构建 JWT 声明
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(key)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("token_type", "access")
                .build();

        // 5. 指定 HS256 签名算法
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        // 6. 签名并返回 Token
        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
