package com.nex.common;

public record ApiResponse<T> (
        Integer code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(201, message, null);
    }
}
