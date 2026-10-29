package com.ghl.library.AOP;

import com.ghl.library.Annotation.PasswordErrorCountOperation;
import com.ghl.library.DTO.UserLoginDTO;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.PasswordSecurityService;
import com.ghl.library.Util.Code;
import org.aspectj.lang.ProceedingJoinPoint;
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
 * {@link PasswordErrorCountAspect} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class PasswordErrorCountAspectTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordSecurityService passwordSecurityService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private PasswordErrorCountOperation operation;

    @InjectMocks
    private PasswordErrorCountAspect aspect;

    @Test
    @DisplayName("被封禁用户应直接拒绝登录")
    void lockedUserShouldBeRejected() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new UserLoginDTO("alice", "123456")});
        when(userMapper.selectOne(any())).thenReturn(new User(1, "alice", "123456", null, 1, 3));

        LibraryException exception = handleAndCatch();

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_FROZEN_ERROR);
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("用户名为空时应直接放行给目标方法处理")
    void blankUsernameShouldProceed() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new UserLoginDTO(" ", "123456")});
        when(joinPoint.proceed()).thenReturn("proceed");

        assertThat(aspect.handleSecurity(joinPoint, operation)).isEqualTo("proceed");
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("用户名为 null 时不应抛空指针")
    void nullUsernameShouldProceed() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new UserLoginDTO(null, "123456")});
        when(joinPoint.proceed()).thenReturn("proceed");

        assertThat(aspect.handleSecurity(joinPoint, operation)).isEqualTo("proceed");
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("登录成功应重置密码错误次数")
    void loginSuccessShouldResetCount() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new UserLoginDTO("alice", "123456")});
        when(userMapper.selectOne(any())).thenReturn(new User(1, "alice", "123456", null, 0, 2));
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.handleSecurity(joinPoint, operation);

        assertThat(result).isEqualTo("ok");
        verify(passwordSecurityService).resetErrorCount(1);
    }

    @Test
    @DisplayName("密码错误应累加错误次数并抛出原异常")
    void wrongPasswordShouldIncreaseCount() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new UserLoginDTO("alice", "bad")});
        when(userMapper.selectOne(any())).thenReturn(new User(1, "alice", "123456", null, 0, 2));
        when(joinPoint.proceed())
                .thenThrow(new LibraryException(Code.USER_OR_PASSWORD_ERROR, Code.USER_OR_PASSWORD_ERROR_MSG));

        LibraryException exception = handleAndCatch();

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_OR_PASSWORD_ERROR);
        verify(passwordSecurityService).handleFailure(1);
    }

    @Test
    @DisplayName("其他业务异常不应累加密码错误次数")
    void otherExceptionShouldNotIncreaseCount() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new UserLoginDTO("alice", "123456")});
        when(userMapper.selectOne(any())).thenReturn(new User(1, "alice", "123456", null, 0, 0));
        when(joinPoint.proceed())
                .thenThrow(new LibraryException(Code.INFO_EMPTY_ERROR, Code.INFO_EMPTY_ERROR_MSG));

        LibraryException exception = handleAndCatch();

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.INFO_EMPTY_ERROR);
        verify(passwordSecurityService, never()).handleFailure(any());
    }

    private LibraryException handleAndCatch() throws Throwable {
        try {
            aspect.handleSecurity(joinPoint, operation);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }
}
