package com.nex.provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/provider")
@RefreshScope
public class ProviderController {

    @Value("${demo.message:尚未读取到Nacos配置}")
    private String message;


    @GetMapping("/hello")
    public String hello() {
        return "Hello from service-provider！";
    }

    @GetMapping("/config")
    public String getConfig() {
        return message;
    }
}
