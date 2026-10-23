package com.nex.consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "service-provider")
public interface ProviderClient {

    @GetMapping("/provider/hello")
    String hello();
}
