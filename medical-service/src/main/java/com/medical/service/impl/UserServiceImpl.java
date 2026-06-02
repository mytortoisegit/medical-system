package com.medical.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.common.constant.Constants;
import com.medical.common.enums.ResultCode;
import com.medical.common.exception.BusinessException;
import com.medical.common.utils.JwtUtil;
import com.medical.common.utils.RedisUtil;
import com.medical.mapper.UserMapper;
import com.medical.model.dto.LoginDTO;
import com.medical.model.entity.User;
import com.medical.model.vo.UserVO;
import com.medical.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 1. 查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));

        if (user == null) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 2. 检查账号状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }

        // 3. 检查是否被锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_LOCKED);
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            // 增加失败次数
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            // 达到最大失败次数，锁定账号
            if (failCount >= Constants.MAX_LOGIN_FAIL_COUNT) {
                this.update(new LambdaUpdateWrapper<User>()
                        .set(User::getLoginFailCount, failCount)
                        .set(User::getLockTime, LocalDateTime.now().plusMinutes(Constants.LOGIN_LOCK_MINUTES))
                        .eq(User::getId, user.getId()));
                throw new BusinessException(ResultCode.USER_ACCOUNT_LOCKED);
            }
            // 仅更新失败次数
            this.update(new LambdaUpdateWrapper<User>()
                    .set(User::getLoginFailCount, failCount)
                    .eq(User::getId, user.getId()));
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 5. 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 6. 将Token存入Redis
        String redisKey = Constants.LOGIN_USER_KEY + user.getId();
        redisUtil.set(redisKey, token, Constants.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        // 7. 更新登录信息（仅更新必要字段，不覆盖password/create_time等）
        this.update(new LambdaUpdateWrapper<User>()
                .set(User::getLoginFailCount, 0)
                .set(User::getLastLoginTime, LocalDateTime.now())
                .set(User::getLockTime, null)
                .eq(User::getId, user.getId()));

        // 8. 构建返回结果
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userVO);

        log.info("用户登录成功: {}", user.getUsername());
        return result;
    }

    @Override
    public void logout(Long userId) {
        // 清除Redis中的Token
        String redisKey = Constants.LOGIN_USER_KEY + userId;
        redisUtil.delete(redisKey);
        log.info("用户退出登录: userId={}", userId);
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 更新密码（仅更新密码字段）
        String encodedPwd = passwordEncoder.encode(newPassword);
        this.update(new LambdaUpdateWrapper<User>()
                .set(User::getPassword, encodedPwd)
                .eq(User::getId, user.getId()));

        // 清除Redis中的Token（强制重新登录）
        String redisKey = Constants.LOGIN_USER_KEY + userId;
        redisUtil.delete(redisKey);

        log.info("用户修改密码成功: userId={}", userId);
    }
}
