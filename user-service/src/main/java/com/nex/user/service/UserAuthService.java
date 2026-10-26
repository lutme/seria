package com.nex.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nex.common.ApiResponse;
import com.nex.common.snowflake.SnowflakeIdGenerator;
import com.nex.user.config.JwtProperties;
import com.nex.user.config.JwtService;
import com.nex.user.dto.LoginDto;
import com.nex.user.dto.RegisterDto;
import com.nex.user.entity.User;
import com.nex.user.mapper.UserMapper;
import com.nex.user.vo.LoginVo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserMapper userMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Long> register(RegisterDto dto) {

        // 提前检查用户名是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));

        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }

        User user = new User();
        user.setId(snowflakeIdGenerator.nextId());
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            // 处理两个并发请求同时注册同一用户名的情况
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }

        return ApiResponse.success(user.getId(), "注册成功");
    }

    /**
     * 用户登录
     */
    public ApiResponse<LoginVo> login(LoginDto dto) {

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));

        // 不区分用户名不存在与密码错误，避免泄露账号信息
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        // status = 1 表示正常，其他状态不允许登录
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号不可用");
        }

        /*
         * 复用已有 JwtService
         * 传入用户 ID, 使 JWT 的 sub 表示用户 ID
         * 不依赖客户端后续重新提交用户名
         */
        String token = jwtService.generateToken(String.valueOf(user.getId()));

        long expiresIn = jwtProperties.getExpirationMinutes() * 60L;

        return ApiResponse.success(new LoginVo(token, "Bearer", expiresIn), "登录成功");
    }
}
