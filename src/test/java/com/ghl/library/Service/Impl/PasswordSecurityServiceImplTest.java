package com.ghl.library.Service.Impl;

import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PasswordSecurityServiceImpl} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class PasswordSecurityServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PasswordSecurityServiceImpl passwordSecurityService;

    @Test
    @DisplayName("密码错误次数未达阈值不应锁定")
    void handleFailureBelowThreshold() {
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 2));

        passwordSecurityService.handleFailure(1);

        verify(userMapper).update(any());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("密码错误次数达到阈值应锁定账号")
    void handleFailureReachThreshold() {
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 3));

        passwordSecurityService.handleFailure(1);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsLocked()).isEqualTo(1);
    }

    @Test
    @DisplayName("用户不存在时计数逻辑不应抛异常")
    void handleFailureWithMissingUser() {
        when(userMapper.selectById(99)).thenReturn(null);

        passwordSecurityService.handleFailure(99);

        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("登录成功应重置密码错误次数")
    void resetErrorCount() {
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 3));

        passwordSecurityService.resetErrorCount(1);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertThat(captor.getValue().getPasswordErrorCount()).isZero();
    }

    @Test
    @DisplayName("用户不存在时重置逻辑应安全返回")
    void resetErrorCountWithMissingUser() {
        when(userMapper.selectById(99)).thenReturn(null);

        passwordSecurityService.resetErrorCount(99);

        verify(userMapper, never()).updateById(any(User.class));
    }
}
