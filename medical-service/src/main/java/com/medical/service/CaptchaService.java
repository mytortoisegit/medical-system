package com.medical.service;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成算术验证码
     *
     * @return {captchaKey: "uuid", captchaImage: "base64图片"}
     */
    Map<String, String> generate();

    /**
     * 校验验证码
     *
     * @param captchaKey   验证码Key
     * @param captchaValue 用户输入的验证码
     * @return 是否通过
     */
    boolean validate(String captchaKey, String captchaValue);

    /**
     * 删除验证码缓存（验证通过后清除）
     */
    void remove(String captchaKey);
}
