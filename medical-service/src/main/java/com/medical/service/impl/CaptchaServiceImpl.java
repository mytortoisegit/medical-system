package com.medical.service.impl;

import cn.hutool.core.util.IdUtil;
import com.medical.common.constant.Constants;
import com.medical.common.utils.RedisUtil;
import com.medical.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现 - 基于 EasyCaptcha 字符验证码（兼容 Java 17+）
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, String> generate() {
        // 生成字符验证码（4位数字+字母混合，130x48像素）
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);

        // 答案
        String answer = captcha.text();

        // 生成唯一Key
        String captchaKey = IdUtil.fastSimpleUUID();

        // 存入Redis，5分钟过期
        String redisKey = Constants.CAPTCHA_KEY + captchaKey;
        redisUtil.set(redisKey, answer, Constants.CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.debug("生成验证码: key={}, answer={}", captchaKey, answer);

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", captcha.toBase64());
        return result;
    }

    @Override
    public boolean validate(String captchaKey, String captchaValue) {
        if (captchaKey == null || captchaValue == null) {
            return false;
        }

        String redisKey = Constants.CAPTCHA_KEY + captchaKey;
        String correctAnswer = redisUtil.get(redisKey);

        if (correctAnswer == null) {
            log.debug("验证码已过期或不存在: key={}", captchaKey);
            return false;
        }

        // 比较答案（不区分大小写，去除前后空格）
        boolean result = correctAnswer.trim()
                .equalsIgnoreCase(captchaValue.trim());

        if (!result) {
            log.debug("验证码错误: key={}, expect={}, actual={}", captchaKey, correctAnswer, captchaValue);
        }

        return result;
    }

    @Override
    public void remove(String captchaKey) {
        if (captchaKey != null) {
            redisUtil.delete(Constants.CAPTCHA_KEY + captchaKey);
        }
    }
}
