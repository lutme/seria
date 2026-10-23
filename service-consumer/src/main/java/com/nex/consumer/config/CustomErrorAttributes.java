package com.nex.consumer.config;

import com.nex.common.ApiErrorResponse;
import com.nex.common.MessageUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public @NonNull Map<String, Object> getErrorAttributes(
            @NonNull WebRequest webRequest,
            @NonNull ErrorAttributeOptions options) {

        Map<String, Object> attributes =
                super.getErrorAttributes(webRequest, options);

        int status = (int) attributes.getOrDefault("status", "500");
        String path = (String) attributes.getOrDefault("path", "/");

        String message = MessageUtil.getMessage(status);

        ApiErrorResponse response =
                ApiErrorResponse.of(status, message, path);

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("code", status);
        result.put("message", response.message());
        result.put("path", response.path());
        result.put("timestamp", response.timestamp());

        return result;
    }
}
