package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.PasswordSecurityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 密码安全服务实现：记录密码错误次数并在达到阈值时锁定账号。
 *
 * @author ghl
 */
@Slf4j
@Service
public class PasswordSecurityServiceImpl implements PasswordSecurityService {

    /** 允许的最大密码错误次数 */
    private static final int MAX_PASSWORD_ERROR_COUNT = 3;

    /** 用户状态：封禁 */
    private static final int USER_STATE_LOCKED = 1;

    @Resource
    private UserMapper userMapper;

    @Override
    public void handleFailure(Integer id) {
        userMapper.update(new UpdateWrapper<User>()
                .eq("id", id)
                .setSql("password_error_count = password_error_count + 1"));

        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("密码错误计数时用户不存在：userId={}", id);
            return;
        }
        if (user.getPasswordErrorCount() != null
                && user.getPasswordErrorCount() >= MAX_PASSWORD_ERROR_COUNT) {
            user.setIsLocked(USER_STATE_LOCKED);
            userMapper.updateById(user);
            log.warn("用户密码错误次数超限，账号已锁定：userId={}", id);
        }
    }

    @Override
    public void resetErrorCount(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return;
        }
        user.setPasswordErrorCount(0);
        userMapper.updateById(user);
    }
}
