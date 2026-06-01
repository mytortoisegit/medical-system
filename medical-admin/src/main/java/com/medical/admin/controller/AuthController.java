package com.medical.admin.controller;

import com.medical.common.base.BaseController;
import com.medical.common.result.R;
import com.medical.common.utils.JwtUtil;
import com.medical.model.dto.LoginDTO;
import com.medical.model.vo.UserVO;
import com.medical.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 认证控制器 - 登录、退出、用户信息
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "登录、退出、用户信息接口")
public class AuthController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.login(loginDTO);
        return R.ok("登录成功", result);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出")
    public R<Void> logout(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(jwt);
        if (userId != null) {
            userService.logout(userId);
        }
        return R.ok("退出成功");
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取当前用户信息")
    public R<UserVO> getUserInfo(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(jwt);
        if (userId == null) {
            return R.fail(401, "无效的Token");
        }
        UserVO userVO = userService.getCurrentUser(userId);
        return R.ok(userVO);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public R<Void> updatePassword(@RequestHeader("Authorization") String token,
                                   @RequestParam String oldPassword,
                                   @RequestParam String newPassword) {
        String jwt = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(jwt);
        if (userId == null) {
            return R.fail(401, "无效的Token");
        }
        userService.updatePassword(userId, oldPassword, newPassword);
        return R.ok("密码修改成功，请重新登录");
    }
}
