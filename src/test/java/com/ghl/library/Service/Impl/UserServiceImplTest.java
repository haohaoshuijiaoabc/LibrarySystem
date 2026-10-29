package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ghl.library.DTO.UserLoginDTO;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Pojo.User;
import com.ghl.library.Util.Code;
import com.ghl.library.VO.UserLoginVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserServiceImpl} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("用户登录成功应写入会话")
    void loginSuccess() {
        when(userMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new User(1, "alice", "123456", "13800000001", 0, 0));

        Result<Void> result = userService.login(new UserLoginDTO("alice", "123456"), session);

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        verify(session).setAttribute("login", 1);
    }

    @Test
    @DisplayName("登录参数为空应报错")
    void loginWithBlankInput() {
        LibraryException exception = loginAndCatch(new UserLoginDTO(" ", "  "));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.INFO_EMPTY_ERROR);
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("用户不存在应报错")
    void loginWithUnknownUser() {
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        LibraryException exception = loginAndCatch(new UserLoginDTO("alice", "123456"));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_OR_PASSWORD_ERROR);
    }

    @Test
    @DisplayName("密码错误应报错")
    void loginWithWrongPassword() {
        when(userMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));

        LibraryException exception = loginAndCatch(new UserLoginDTO("alice", "bad"));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_OR_PASSWORD_ERROR);
        verify(session, never()).setAttribute(any(), any());
    }

    @Test
    @DisplayName("被封禁用户登录应报错")
    void loginWithLockedUser() {
        when(userMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new User(1, "alice", "123456", null, 1, 3));

        LibraryException exception = loginAndCatch(new UserLoginDTO("alice", "123456"));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_FROZEN_ERROR);
    }

    @Test
    @DisplayName("获取当前用户信息应返回脱敏 VO")
    void getInfo() {
        when(session.getAttribute("login")).thenReturn(1);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", "13800000001", 0, 0));

        Result<UserLoginVO> result = userService.getInfo(session);

        assertThat(result.getData().getUsername()).isEqualTo("alice");
        assertThat(result.getData().getPhone()).isEqualTo("13800000001");
    }

    @Test
    @DisplayName("未登录获取用户信息应报错")
    void getInfoWithoutLogin() {
        when(session.getAttribute("login")).thenReturn(null);

        LibraryException exception = getInfoAndCatch();

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_LOGIN_SESSION_EMPTY_ERROR);
    }

    @Test
    @DisplayName("会话用户不存在应报错")
    void getInfoWithMissingUser() {
        when(session.getAttribute("login")).thenReturn(1);
        when(userMapper.selectById(1)).thenReturn(null);

        LibraryException exception = getInfoAndCatch();

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_ERROR);
    }

    @Test
    @DisplayName("退出登录应移除会话属性")
    void logout() {
        Result<Void> result = userService.logout(session);

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        verify(session).removeAttribute("login");
    }

    private LibraryException loginAndCatch(UserLoginDTO dto) {
        try {
            userService.login(dto, session);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }

    private LibraryException getInfoAndCatch() {
        try {
            userService.getInfo(session);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }
}
