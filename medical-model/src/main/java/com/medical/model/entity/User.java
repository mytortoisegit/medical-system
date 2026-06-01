package com.medical.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名（登录账号） */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别（0-未知 1-男 2-女） */
    private Integer gender;

    /** 头像URL */
    private String avatar;

    /** 角色ID */
    private Long roleId;

    /** 账号状态（0-正常 1-禁用） */
    private Integer status;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 登录失败次数 */
    private Integer loginFailCount;

    /** 锁定时间 */
    private LocalDateTime lockTime;
}
