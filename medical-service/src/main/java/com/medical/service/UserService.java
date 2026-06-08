package com.medical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.model.dto.LoginDTO;
import com.medical.model.dto.UserQueryDTO;
import com.medical.model.entity.User;
import com.medical.model.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /** 登录 */
    Map<String, Object> login(LoginDTO loginDTO);

    /** 退出 */
    void logout(Long userId);

    /** 分页查询用户列表 */
    Page<UserVO> pageQuery(UserQueryDTO queryDTO);

    /** 新增用户 */
    boolean addUser(User user);

    /** 更新用户 */
    boolean updateUser(User user);

    /** 重置密码为默认密码 */
    void resetPassword(Long userId);

    /** 获取当前登录用户信息 */
    UserVO getCurrentUser(Long userId);

    /** 修改密码 */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
