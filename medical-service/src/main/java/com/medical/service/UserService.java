package com.medical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.model.dto.LoginDTO;
import com.medical.model.entity.User;
import com.medical.model.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token及用户信息
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 用户退出
     */
    void logout(Long userId);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
