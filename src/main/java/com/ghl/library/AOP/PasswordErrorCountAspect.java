package com.ghl.library.AOP;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ghl.library.Annotation.PasswordErrorCountOperation;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.PasswordSecurityService;
import com.ghl.library.Util.Code;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 密码错误次数统计切面。
 *
 * <p>通过自定义注解 {@link PasswordErrorCountOperation} 标记登录方法：
 * 登录前校验账号是否被封禁，登录失败时累加密码错误次数，成功时清零，
 * 达到阈值后自动锁定账号。</p>
 *
 * @author ghl
 */
@Aspect
@Component
@Slf4j
public class PasswordErrorCountAspect {

    /** 用户状态：封禁 */
    private static final int USER_STATE_LOCKED = 1;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordSecurityService service;


    @Around("@annotation(errorCount)")
    public Object handleSecurity(ProceedingJoinPoint joinPoint, PasswordErrorCountOperation errorCount) throws Throwable {
        Object arg = joinPoint.getArgs()[0];
        Object username = BeanUtil.getFieldValue(arg, "username");
        String name = username == null ? null : username.toString();

        // 用户名为空，直接交给目标方法处理（会抛出 INFO_EMPTY_ERROR）
        if (StrUtil.isBlank(name)) {
            return joinPoint.proceed();
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", name));

        // 用户存在且已被冻结
        if (user != null && Objects.equals(user.getIsLocked(), USER_STATE_LOCKED)) {
            throw new LibraryException(Code.USER_FROZEN_ERROR, Code.USER_FROZEN_ERROR_MSG);
        }

        try {
            Object result = joinPoint.proceed();
            // 登录成功，重置错误次数（仅当用户存在）
            if (user != null) {
                service.resetErrorCount(user.getId());
            }
            return result;
        } catch (LibraryException e) {
            // 仅当【用户存在】且异常码为【密码错误】时，才增加错误计数
            if (user != null && e.getCode() == Code.USER_OR_PASSWORD_ERROR) {
                service.handleFailure(user.getId());
            }
            throw e;
        }
    }
}