package com.medical.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.common.constant.Constants;
import com.medical.common.enums.ResultCode;
import com.medical.common.exception.BusinessException;
import com.medical.common.utils.JwtUtil;
import com.medical.common.utils.RedisUtil;
import com.medical.service.security.LoginRateLimiter;
import com.medical.mapper.SysRoleMapper;
import com.medical.mapper.UserMapper;
import com.medical.model.dto.LoginDTO;
import com.medical.model.dto.UserQueryDTO;
import com.medical.model.entity.SysRole;
import com.medical.model.entity.User;
import com.medical.model.vo.UserVO;
import com.medical.service.CaptchaService;
import com.medical.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 0. 限流检查
        String clientIp = getClientIp(request);
        loginRateLimiter.checkRateLimit(loginDTO.getUsername(), clientIp);

        // 0.1 验证码校验
        if (!captchaService.validate(loginDTO.getCaptchaKey(), loginDTO.getCaptcha())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "验证码错误或已过期");
        }

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
            // 记录限流计数
            loginRateLimiter.recordFailedAttempt(loginDTO.getUsername(), clientIp);
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

        // 5. 登录成功，清除限流计数和验证码
        loginRateLimiter.clearRateLimit(loginDTO.getUsername(), clientIp);
        captchaService.remove(loginDTO.getCaptchaKey());

        // 6. 查询角色编码
        String roleCode = "ROLE_user"; // 默认角色
        if (user.getRoleId() != null) {
            SysRole sysRole = sysRoleMapper.selectById(user.getRoleId());
            if (sysRole != null) {
                roleCode = sysRole.getRoleCode();
            }
        }

        // 7. 生成Token（携带角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCode);

        // 8. 将Token存入Redis
        String redisKey = Constants.LOGIN_USER_KEY + user.getId();
        redisUtil.set(redisKey, token, Constants.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        // 8. 更新登录信息（仅更新必要字段，不覆盖password/create_time等）
        this.update(new LambdaUpdateWrapper<User>()
                .set(User::getLoginFailCount, 0)
                .set(User::getLastLoginTime, LocalDateTime.now())
                .set(User::getLockTime, null)
                .eq(User::getId, user.getId()));

        // 9. 构建返回结果
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userVO);

        log.info("用户登录成功: {}", user.getUsername());
        return result;
    }

    /**
     * 获取客户端真实IP（考虑反向代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
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

    // ==================== 用户管理（管理员操作） ====================

    @Override
    public Page<UserVO> pageQuery(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername());
        wrapper.like(StrUtil.isNotBlank(queryDTO.getRealName()), User::getRealName, queryDTO.getRealName());
        wrapper.eq(queryDTO.getRoleId() != null, User::getRoleId, queryDTO.getRoleId());
        wrapper.eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus());
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        // 转换为VO（脱敏，不返回密码）
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(u -> BeanUtil.copyProperties(u, UserVO.class))
                .collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public boolean addUser(User user) {
        // 检查用户名唯一性
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE.getCode(), "用户名已存在");
        }
        // 默认密码 123456
        user.setPassword(passwordEncoder.encode("123456"));
        return this.save(user);
    }

    @Override
    public boolean updateUser(User user) {
        User exist = this.getById(user.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 不允许修改密码（通过专门的重置密码接口）
        user.setPassword(null);
        return this.updateById(user);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String defaultPwd = passwordEncoder.encode("123456");
        this.update(new LambdaUpdateWrapper<User>()
                .set(User::getPassword, defaultPwd)
                .eq(User::getId, userId));
        // 清除Token强制重新登录
        redisUtil.delete(Constants.LOGIN_USER_KEY + userId);
        log.info("重置用户密码: userId={}", userId);
    }
}
