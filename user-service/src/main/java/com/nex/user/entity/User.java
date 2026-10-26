package com.nex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@TableName("users")
@Data
public class User {

    /**
     * 雪花算法生成的用户ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private String username;

    private String passwordHash;

    private String nickname;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
