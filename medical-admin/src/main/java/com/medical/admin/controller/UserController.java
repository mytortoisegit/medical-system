package com.medical.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.result.PageResult;
import com.medical.common.result.R;
import com.medical.model.dto.UserQueryDTO;
import com.medical.model.entity.User;
import com.medical.model.vo.UserVO;
import com.medical.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户（大夫）的增删改查接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表")
    @PreAuthorize("hasRole('admin')")
    public R<PageResult<UserVO>> page(@Valid UserQueryDTO queryDTO) {
        Page<UserVO> page = userService.pageQuery(queryDTO);
        return R.ok(PageResult.of(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情")
    @PreAuthorize("hasRole('admin')")
    public R<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.fail(404, "用户不存在");
        }
        return R.ok(convertToVO(user));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    @PreAuthorize("hasRole('admin')")
    public R<Void> add(@Valid @RequestBody User user) {
        userService.addUser(user);
        return R.ok("新增成功，默认密码为 123456");
    }

    @PutMapping
    @Operation(summary = "更新用户信息")
    @PreAuthorize("hasRole('admin')")
    public R<Void> update(@Valid @RequestBody User user) {
        userService.updateUser(user);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasRole('admin')")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok("删除成功");
    }

    @PutMapping("/resetPassword/{id}")
    @Operation(summary = "重置用户密码（重置为123456）")
    @PreAuthorize("hasRole('admin')")
    public R<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return R.ok("密码已重置为 123456");
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setAvatar(user.getAvatar());
        vo.setRoleId(user.getRoleId());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
