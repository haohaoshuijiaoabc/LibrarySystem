package com.ghl.library.Interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.User;
import com.ghl.library.Util.Code;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link UserLoginInterceptor} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class UserLoginInterceptorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserMapper userMapper;

    private UserLoginInterceptor interceptor;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new UserLoginInterceptor();
        ReflectionTestUtils.setField(interceptor, "userMapper", userMapper);
        ReflectionTestUtils.setField(interceptor, "objectMapper", objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("未登录应返回 401 与业务码 1000")
    void rejectAnonymous() throws Exception {
        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("code").asInt()).isEqualTo(Code.USER_LOGIN_SESSION_EMPTY_ERROR);
    }

    @Test
    @DisplayName("会话用户有效应放行")
    void allowLoggedInUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", 1);
        request.setSession(session);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    @DisplayName("账号被封禁应使会话失效并拒绝")
    void rejectLockedUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", 1);
        request.setSession(session);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 1, 3));

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(session.isInvalid()).isTrue();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("用户已被删除应拒绝访问")
    void rejectMissingUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", 1);
        request.setSession(session);
        when(userMapper.selectById(1)).thenReturn(null);

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(objectMapper.readTree(response.getContentAsString()).get("code").asInt())
                .isEqualTo(Code.USER_LOGIN_SESSION_EMPTY_ERROR);
    }
}
