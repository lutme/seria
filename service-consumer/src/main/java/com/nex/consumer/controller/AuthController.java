package com.nex.consumer.controller;

import com.nex.consumer.config.JwtService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private JwtService jwtService;

    @PostMapping("/login")
    public Mono<LoginResponse> login() {
        String token = jwtService.generateToken("xiaonan");

        return Mono.just(new LoginResponse("Bearer", token));
    }

    public record LoginResponse(String tokenType, String accessToken) {}
}
