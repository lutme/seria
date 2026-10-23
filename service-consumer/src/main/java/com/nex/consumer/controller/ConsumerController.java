package com.nex.consumer.controller;

import com.nex.consumer.feign.ProviderClient;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Resource
    private ProviderClient providerClient;

    @GetMapping("/hello")
    public String hello() {
        return providerClient.hello();
    }

}
