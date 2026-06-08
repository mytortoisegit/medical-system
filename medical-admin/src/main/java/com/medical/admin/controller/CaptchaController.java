package com.medical.admin.controller;

import com.medical.common.result.R;
import com.medical.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "登录、退出、验证码接口")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码")
    public R<Map<String, String>> getCaptcha() {
        Map<String, String> captcha = captchaService.generate();
        return R.ok(captcha);
    }
}
