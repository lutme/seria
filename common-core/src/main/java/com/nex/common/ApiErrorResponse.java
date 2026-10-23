package com.nex.common;

import java.time.LocalDateTime;

public record ApiErrorResponse (
        Integer code,
        String message,
        String path,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse of (
            Integer code,
            String message,
            String path
    ) {
        return new ApiErrorResponse (
                code,
                message,
                path,
                LocalDateTime.now()
        );
    }
}