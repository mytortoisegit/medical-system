package com.medical.common.constant;

/**
 * Redis缓存Key常量
 */
public class RedisConstants {

    /** 字典数据缓存 */
    public static final String DICT_KEY = "dict:";

    /** 药品信息缓存 */
    public static final String MEDICINE_KEY = "medicine:";

    /** 药品库存缓存 */
    public static final String MEDICINE_STOCK_KEY = "medicine:stock:";

    /** 处方缓存 */
    public static final String PRESCRIPTION_KEY = "prescription:";

    /** 用户权限缓存 */
    public static final String USER_PERMISSION_KEY = "user:permission:";

    /** 接口限流Key */
    public static final String RATE_LIMIT_KEY = "rate:limit:";

    /** 默认缓存过期时间（分钟） */
    public static final long DEFAULT_EXPIRE_MINUTES = 30;

    /** 药品列表缓存过期时间（小时） */
    public static final long MEDICINE_LIST_EXPIRE_HOURS = 2;
}
