package com.nex.user.controller;

import com.nex.common.ApiResponse;
import com.nex.user.dto.LoginDto;
import com.nex.user.dto.RegisterDto;
import com.nex.user.service.UserAuthService;
import com.nex.user.vo.LoginVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<Long> register(@Valid @RequestBody RegisterDto dto) {
        return userAuthService.register(dto);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginVo> login(@Valid @RequestBody LoginDto dto) {
        return userAuthService.login(dto);
    }
}
