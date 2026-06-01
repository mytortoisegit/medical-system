package com.medical.common.result;

import com.medical.common.enums.ResultCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 *
 * @param <T> 数据类型
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    private R() {
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 成功响应 ====================

    public static <T> R<T> ok() {
        return ok(ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> R<T> ok(String message) {
        return ok(message, null);
    }

    public static <T> R<T> ok(T data) {
        return ok(ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = message;
        r.data = data;
        return r;
    }

    // ==================== 失败响应 ====================

    public static <T> R<T> fail() {
        return fail(ResultCode.INTERNAL_ERROR);
    }

    public static <T> R<T> fail(String message) {
        return fail(ResultCode.INTERNAL_ERROR.getCode(), message);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }

    // ==================== 判断方法 ====================

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    // ==================== 链式调用 ====================

    public R<T> code(int code) {
        this.code = code;
        return this;
    }

    public R<T> message(String message) {
        this.message = message;
        return this;
    }

    public R<T> data(T data) {
        this.data = data;
        return this;
    }
}
