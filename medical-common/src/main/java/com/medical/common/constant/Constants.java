package com.medical.common.constant;

/**
 * 系统常量
 */
public class Constants {

    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Token请求头 */
    public static final String TOKEN_HEADER = "Authorization";

    /** 登录用户Redis Key前缀 */
    public static final String LOGIN_USER_KEY = "login:user:";

    /** 验证码Redis Key前缀 */
    public static final String CAPTCHA_KEY = "captcha:";

    /** Token过期时间（小时） */
    public static final int TOKEN_EXPIRE_HOURS = 24;

    /** 验证码过期时间（分钟） */
    public static final int CAPTCHA_EXPIRE_MINUTES = 5;

    /** 密码最大错误次数 */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;

    /** 密码错误锁定时间（分钟） */
    public static final int LOGIN_LOCK_MINUTES = 30;

    /** 系统管理员角色标识 */
    public static final String ROLE_ADMIN = "admin";

    /** 系统用户角色标识 */
    public static final String ROLE_USER = "user";
}
