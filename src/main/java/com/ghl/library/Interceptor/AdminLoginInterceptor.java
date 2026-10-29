package com.ghl.library.Interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghl.library.Mapper.AdminMapper;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Util.Code;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 管理员登录拦截器。
 *
 * <p>拦截 {@code /admin/**} 请求，校验管理员会话；
 * 未登录时返回业务码 {@code 5000} 的 JSON 响应，管理端据此跳转登录页。</p>
 *
 * @author ghl
 */
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Integer id = session == null ? null : (Integer) session.getAttribute("admin");
        if (id == null || adminMapper.selectById(id) == null) {
            if (session != null) {
                session.invalidate();
            }
            writeUnauthorized(response);
            return false;
        }
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(),
                Result.error(Code.ADMIN_LOGIN_SESSION_EMPTY_ERROR,
                        Code.ADMIN_LOGIN_SESSION_EMPTY_ERROR_MSG));
    }
}
