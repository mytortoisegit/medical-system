package com.medical.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息VO（脱敏返回）
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private Long roleId;
    private String roleName;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
