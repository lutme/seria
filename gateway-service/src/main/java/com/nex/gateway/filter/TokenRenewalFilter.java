
package com.nex.gateway.filter;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Component
public class TokenRenewalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TokenRenewalFilter.class);

    /**
     * 将续期后的 JWT 放入响应头, 供前端更新本地 Token
     */
    private static final String NEW_TOKEN_HEADER = "X-New-Token";

    /**
     * Redis 续期结果缓存的 Key 前缀
     */
    private static final String CACHE_PREFIX = "gateway:jwt:renew:";

    /**
     * Redis 分布式锁的 Key 后缀
     */
    private static final String LOCK_SUFFIX = ":lock";

    /**
     * 分布式锁过期时间
     * 防止持有锁的实例异常退出后, 其他实例永远拿不到锁
     */
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    /**
     * 未拿到锁时, 两次检查之间的等待时间
     */
    private static final Duration RETRY_INTERVAL = Duration.ofMillis(50);

    /**
     * 最多尝试次数, 避免请求无限等待
     */
    private static final int MAX_RETRIES = 40;

    /**
     * 原子读取或写入续期结果
     * 如果缓存中已有续期结果, 直接返回已有 Token
     * 如果没有, 则将当前候选 Token 写入缓存并返回
     * 整个 GET + SET 操作由 Redis Lua 脚本原子执行
     * 避免多个 Gateway 实例同时写入不同的续期结果
     * KEYS[1]: 续期结果缓存 Key
     * ARGV[1]: 缓存过期时间, 单位毫秒
     * ARGV[2]: 本次生成的候选 Token
     */
    private static final DefaultRedisScript<String> CACHE_SCRIPT =
            new DefaultRedisScript<>("""
                    local existing = redis.call('GET', KEYS[1])
                    if existing then
                        return existing
                    end

                    redis.call('PSETEX', KEYS[1], ARGV[1], ARGV[2])
                    return ARGV[2]
                    """, String.class);

    /**
     * 安全释放分布式锁
     * 只有当 Redis 中保存的锁值与当前请求的锁值一致时才删除锁
     * 防止旧请求误删其他请求后来获得的锁
     * KEYS[1]: 分布式锁 Key
     * ARGV[1]: 当前请求的唯一锁值
     */
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('GET', KEYS[1]) == ARGV[1] then
                        return redis.call('DEL', KEYS[1])
                    end
                    return 0
                    """, Long.class);

    /**
     * JWT 编码器, 用于生成续期后的 Token
     */
    private final JwtEncoder jwtEncoder;

    /**
     * Token 总有效期, 单位分钟
     */
    private final long expirationMinutes;

    /**
     * 剩余有效时间低于该阈值时触发续期, 单位分钟
     */
    private final long renewBeforeMinutes;

    /**
     * 响应式 Redis 操作模板
     * Gateway 使用 WebFlux, 因此这里不使用阻塞式 Redis API
     */
    private final ReactiveStringRedisTemplate redisTemplate;

    public TokenRenewalFilter(
            JwtEncoder jwtEncoder,
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${jwt.expiration-minutes:30}")
            long expirationMinutes,
            @Value("${jwt.renew-before-minutes:10}")
            long renewBeforeMinutes) {

        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration-minutes 必须大于 0");
        }

        if (renewBeforeMinutes <= 0
                || renewBeforeMinutes >= expirationMinutes) {
            throw new IllegalArgumentException(
                    "JWT renew-before-minutes 必须大于 0, 且小于 expiration-minutes");
        }

        this.jwtEncoder = jwtEncoder;
        this.redisTemplate = redisTemplate;
        this.expirationMinutes = expirationMinutes;
        this.renewBeforeMinutes = renewBeforeMinutes;
    }

    /**
     * Gateway 全局过滤器入口
     * 1. 从 Spring Security 响应式上下文中获取认证信息
     * 2. 仅处理已经认证成功的 JWT 用户
     * 3. 检查 Token 剩余有效时间
     * 4. 需要续期时, 通过 Redis 协调并获取续期结果
     * 5. 续期成功后, 将新 Token 写入响应头
     * 6. 无论是否需要续期, 都继续执行后续 Gateway 过滤器
     * 注意: 这里不负责跳过或替代 JWT 身份认证
     */
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {

        Mono<ServerWebExchange> exchangeMono = ReactiveSecurityContextHolder.getContext()
                        // 从安全上下文获取当前认证对象
                        .flatMap(context ->
                                Mono.justOrEmpty(context.getAuthentication()))

                        // 只处理已经认证成功的请求
                        .filter(Authentication::isAuthenticated)

                        // 只处理 JWT 认证类型
                        .filter(JwtAuthenticationToken.class::isInstance)
                        .cast(JwtAuthenticationToken.class)

                        // 获取 JWT, 并判断是否需要续期
                        .flatMap(authentication -> {
                            Jwt jwt = authentication.getToken();
                            Instant expiresAt = jwt.getExpiresAt();

                            // 没有过期时间的 Token 不执行续期
                            if (expiresAt == null) {
                                return Mono.just(exchange);
                            }

                            Instant now = Instant.now();

                            // 计算 Token 剩余有效时间
                            Duration remaining = Duration.between(now, expiresAt);

                            // Token 已经过期或刚好到期, 不在这里续期
                            if (remaining.isNegative() || remaining.isZero()) {
                                return Mono.just(exchange);
                            }

                            // 剩余有效时间仍然充足, 无需续期
                            if (remaining.compareTo(Duration.ofMinutes(renewBeforeMinutes)) >= 0) {
                                return Mono.just(exchange);
                            }

                            // 进入续期流程
                            return renewOnce(jwt)
                                    // 只有拿到新 Token 时才添加响应头
                                    .doOnNext(newToken ->
                                            exchange.getResponse()
                                                    .getHeaders()
                                                    .set(NEW_TOKEN_HEADER, newToken))

                                    // 记录续期失败原因, 便于排查 Redis 或签名问题
                                    .doOnError(ex -> log.error("Jwt Token 自动续期失败, subject={}", jwt.getSubject(), ex))

                                    // 续期失败不阻断当前已经认证成功的请求
                                    .onErrorResume(ex -> Mono.empty())

                                    // 续期完成后返回原始 exchange, 继续过滤器链
                                    .thenReturn(exchange);
                        })

                        // 没有认证上下文时, 原样继续过滤器链
                        .defaultIfEmpty(exchange);

        return exchangeMono.flatMap(chain::filter);
    }

    /**
     * 获取本次旧 Token 对应的唯一续期结果
     * 先查询 Redis 缓存:
     * - 缓存存在: 直接返回已经生成的新 Token
     * - 缓存不存在: 进入分布式锁协调流程
     * 使用旧 Token 的 SHA-256 摘要作为 Key 的一部分, 避免把完整的旧 JWT 放进 Redis Key
     */
    private Mono<String> renewOnce(Jwt oldJwt) {
        Instant expiresAt = oldJwt.getExpiresAt();

        if (expiresAt == null) {
            return Mono.empty();
        }

        // 旧 Token 剩余有效期决定缓存的存活时间
        long remainingMillis =
                Duration.between(Instant.now(), expiresAt).toMillis();

        if (remainingMillis <= 0) {
            return Mono.empty();
        }

        // 使用旧 Token 的摘要区分不同登录会话的续期结果
        String cacheKey = CACHE_PREFIX + sha256(oldJwt.getTokenValue());

        // 先读缓存, 避免每次请求都竞争分布式锁
        return redisTemplate.opsForValue().get(cacheKey).switchIfEmpty(Mono.defer(() -> tryRenew(oldJwt, cacheKey, remainingMillis)));
    }

    /**
     * 有界重试获取续期结果
     * 每次尝试都会先检查缓存, 再尝试获取分布式锁
     * 如果其他请求正在续期, 则短暂等待并重新检查缓存
     * 使用 Flux.range 控制尝试次数, 避免通过递归调用
     * tryRenew() 不断创建新的订阅链
     */
    private Mono<String> tryRenew(
            Jwt oldJwt,
            String cacheKey,
            long remainingMillis) {

        String lockKey = cacheKey + LOCK_SUFFIX;

        return Flux.range(0, MAX_RETRIES)
                // 按顺序执行每一次尝试, 避免并发发起多次锁竞争
                .concatMap(attempt -> {
                    // 每次尝试先读缓存, 其他请求可能刚刚已经完成续期
                    return redisTemplate.opsForValue()
                            .get(cacheKey)
                            .switchIfEmpty(Mono.defer(() ->
                                    attemptRenew(
                                            oldJwt,
                                            cacheKey,
                                            lockKey,
                                            remainingMillis)));
                })
                // 第一次拿到非空续期结果时立即结束
                .next();
    }

    /**
     * 执行一次续期尝试
     * 1. 尝试获取分布式锁
     * 2. 没拿到锁: 等待一小段时间, 再检查缓存
     * 3. 拿到锁: 再次检查缓存, 避免重复生成 Token
     * 4. 缓存仍不存在且旧 Token 仍有效: 生成候选 Token
     * 5. 通过 Lua 脚本原子保存或读取最终续期结果
     * 6. 成功、空结果或异常时, 尝试安全释放锁
     */
    private Mono<String> attemptRenew(
            Jwt oldJwt,
            String cacheKey,
            String lockKey,
            long remainingMillis) {

        // 每个持锁请求都使用独立值, 防止误释放其他请求的锁
        String lockValue = UUID.randomUUID().toString();

        return redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, LOCK_TTL)
                .flatMap(acquired -> {

                    // 没有拿到锁, 说明可能有其他请求正在续期
                    if (!acquired) {
                        // 等待后检查缓存, 若仍为空, 交给外层进行下一次尝试
                        return Mono.delay(RETRY_INTERVAL).then(redisTemplate.opsForValue().get(cacheKey));
                    }

                    // 已经获得锁, 再次检查缓存, 防止刚拿锁时其他请求已完成续期
                    Mono<String> renewalResult = redisTemplate.opsForValue()
                                    .get(cacheKey)
                                    .switchIfEmpty(Mono.defer(() -> {
                                        // 生成新 Token 前, 再确认旧 Token 没有过期
                                        Instant now = Instant.now();
                                        Instant expiresAt = oldJwt.getExpiresAt();

                                        if (expiresAt == null || !now.isBefore(expiresAt)) {
                                            return Mono.empty();
                                        }

                                        // 创建候选续期 Token
                                        String candidateToken = renew(oldJwt, now);

                                        // 缓存最多保留到旧 Token 原本的到期时间
                                        // 即使续期结果被重复读取, 也不会无限期留存
                                        long ttlMillis = Math.min(
                                                remainingMillis,
                                                Duration.between(
                                                        now, expiresAt).toMillis());

                                        if (ttlMillis <= 0) {
                                            return Mono.empty();
                                        }

                                        // Lua 保证并发写入时所有请求拿到同一个最终结果
                                        return redisTemplate.execute(
                                                        CACHE_SCRIPT,
                                                        List.of(cacheKey),
                                                        String.valueOf(ttlMillis),
                                                        candidateToken)
                                                .next();
                                    }));

                    // 不管拿到 Token 还是返回空结果, 都释放本次获得的锁
                    return renewalResult
                            .flatMap(token ->
                                    releaseLock(lockKey, lockValue)
                                            .thenReturn(token))
                            .switchIfEmpty(Mono.defer(() ->
                                    releaseLock(lockKey, lockValue)
                                            .then(Mono.empty())))
                            .onErrorResume(error ->
                                    // 尽力释放锁, 释放锁失败不能覆盖原始异常
                                    releaseLock(lockKey, lockValue)
                                            .onErrorResume(releaseError -> {
                                                log.warn("释放 Jwt Token 续期锁失败，lockKey={}", lockKey, releaseError);
                                                return Mono.empty();
                                            }).then(Mono.error(error)));
                });
    }

    /**
     * 生成新的 JWT
     * 保留旧 Token 中的业务 Claims, 例如 subject、角色和权限信息
     * 更新签发时间和过期时间, 并移除旧 Token 的时间相关标记
     */
    private String renew(Jwt oldJwt, Instant now) {

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                // 复制原有 Claims, 保留用户身份及业务权限信息
                .claims(claims -> claims.putAll(oldJwt.getClaims()))

                // 更新签发时间
                .issuedAt(now)

                // 根据配置计算新 Token 的到期时间
                .expiresAt(now.plus(Duration.ofMinutes(expirationMinutes)))

                .build();

        // 使用 HS256 对新 Token 进行签名
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    /**
     * 使用 Lua 脚本释放锁
     * 只有锁的持有者才能删除该锁, 防止误删其他请求的锁
     */
    private Mono<Long> releaseLock(String lockKey, String lockValue) {
        return redisTemplate.execute(
                        RELEASE_LOCK_SCRIPT,
                        List.of(lockKey),
                        lockValue)
                .next()
                // 没有执行结果时返回 0, 表示本次没有删除锁
                .defaultIfEmpty(0L);
    }

    /**
     * 计算字符串的 SHA-256 摘要, 并转换为十六进制字符串
     * 用于构建续期缓存 Key, 避免直接使用完整 JWT 作为 Key
     */
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    value.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 是 Java 标准算法, 正常 JDK 环境中不应缺失
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", ex);
        }
    }

    /**
     * 设置过滤器执行顺序
     * 当前值为 -3, 需结合项目中 Spring Security 与其他 Gateway
     * 过滤器的实际执行顺序确认行为
     */
    @Override
    public int getOrder() {
        return -3;
    }
}
