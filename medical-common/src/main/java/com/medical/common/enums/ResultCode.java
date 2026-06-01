package com.medical.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 客户端错误 */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    /** 服务端错误 */
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    /** 业务错误 */
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_ACCOUNT_LOCKED(1003, "账号已被锁定"),
    USER_ACCOUNT_DISABLED(1004, "账号已被禁用"),
    TOKEN_EXPIRED(1005, "登录已过期，请重新登录"),
    TOKEN_INVALID(1006, "无效的Token"),

    MEDICINE_NOT_FOUND(2001, "药品不存在"),
    MEDICINE_STOCK_INSUFFICIENT(2002, "药品库存不足"),
    MEDICINE_NAME_DUPLICATE(2003, "药品名称已存在"),

    PRESCRIPTION_NOT_FOUND(3001, "处方不存在"),

    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_FORMAT_ERROR(4002, "文件格式错误"),
    FILE_SIZE_EXCEED(4003, "文件大小超出限制"),

    DATA_DUPLICATE(5001, "数据已存在"),
    DATA_NOT_FOUND(5002, "数据不存在"),
    DATA_SAVE_FAILED(5003, "数据保存失败"),
    DATA_UPDATE_FAILED(5004, "数据更新失败"),
    DATA_DELETE_FAILED(5005, "数据删除失败");

    /** 状态码 */
    private final int code;
    /** 消息 */
    private final String message;
}
