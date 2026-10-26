package com.nex.user.vo;

public record LoginVo(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
