package com.ghl.library.Interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghl.library.Mapper.AdminMapper;
import com.ghl.library.Pojo.Admin;
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
 * {@link AdminLoginInterceptor} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class AdminLoginInterceptorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AdminMapper adminMapper;

    private AdminLoginInterceptor interceptor;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new AdminLoginInterceptor();
        ReflectionTestUtils.setField(interceptor, "adminMapper", adminMapper);
        ReflectionTestUtils.setField(interceptor, "objectMapper", objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("管理员未登录应返回 401 与业务码 5000")
    void rejectAnonymous() throws Exception {
        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(objectMapper.readTree(response.getContentAsString()).get("code").asInt())
                .isEqualTo(Code.ADMIN_LOGIN_SESSION_EMPTY_ERROR);
    }

    @Test
    @DisplayName("管理员会话有效应放行")
    void allowLoggedInAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", 1);
        request.setSession(session);
        when(adminMapper.selectById(1)).thenReturn(new Admin(1, "admin", "123456", 0, 0));

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    @DisplayName("管理员不存在应使会话失效并拒绝")
    void rejectMissingAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", 99);
        request.setSession(session);
        when(adminMapper.selectById(99)).thenReturn(null);

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(session.isInvalid()).isTrue();
    }
}
