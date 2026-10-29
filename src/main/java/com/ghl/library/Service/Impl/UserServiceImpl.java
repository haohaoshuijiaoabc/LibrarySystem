package com.ghl.library.Service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghl.library.Annotation.PasswordErrorCountOperation;
import com.ghl.library.DTO.UserLoginDTO;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.UserService;
import com.ghl.library.Util.Code;
import com.ghl.library.VO.UserLoginVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户业务实现。
 *
 * @author ghl
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 用户状态：封禁 */
    private static final int USER_STATE_LOCKED = 1;

    @Resource
    private UserMapper userMapper;

    /**
     * 用户登录。
     *
     * <p>密码错误次数统计与封禁校验由 {@link PasswordErrorCountOperation} 注解配合 AOP 完成。</p>
     *
     * @param userLoginDTO 登录参数
     * @param session      会话
     * @return 登录结果
     */
    @Override
    @PasswordErrorCountOperation
    public Result<Void> login(UserLoginDTO userLoginDTO, HttpSession session) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        if (StrUtil.hasBlank(username, password)) {
            throw new LibraryException(Code.INFO_EMPTY_ERROR, Code.INFO_EMPTY_ERROR_MSG);
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !Objects.equals(password, user.getPassword())) {
            throw new LibraryException(Code.USER_OR_PASSWORD_ERROR, Code.USER_OR_PASSWORD_ERROR_MSG);
        }
        if (Objects.equals(user.getIsLocked(), USER_STATE_LOCKED)) {
            throw new LibraryException(Code.USER_FROZEN_ERROR, Code.USER_FROZEN_ERROR_MSG);
        }

        session.setAttribute("login", user.getId());
        log.info("用户登录成功：userId={}, username={}", user.getId(), user.getUsername());
        return Result.success();
    }

    @Override
    public Result<UserLoginVO> getInfo(HttpSession session) {
        Integer id = (Integer) session.getAttribute("login");
        if (id == null) {
            throw new LibraryException(Code.USER_LOGIN_SESSION_EMPTY_ERROR,
                    Code.USER_LOGIN_SESSION_EMPTY_ERROR_MSG);
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new LibraryException(Code.USER_ERROR, Code.USER_ERROR_MSG);
        }
        return Result.success(new UserLoginVO(user.getId(), user.getUsername(), user.getPhone()));
    }

    @Override
    public Result<Void> logout(HttpSession session) {
        session.removeAttribute("login");
        return Result.success();
    }
}
