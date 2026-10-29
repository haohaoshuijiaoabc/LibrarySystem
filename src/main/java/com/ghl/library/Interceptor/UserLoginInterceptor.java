package com.ghl.library.Interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Pojo.User;
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
import java.util.Objects;

/**
 * 用户登录拦截器。
 *
 * <p>拦截 {@code /user/**} 请求，校验会话中的用户是否仍然有效；
 * 未登录或账号已被封禁时直接返回统一的 JSON 错误响应，避免前端解析空响应体失败。</p>
 *
 * @author ghl
 */
@Component
public class UserLoginInterceptor implements HandlerInterceptor {

    /** 用户状态：封禁 */
    private static final int USER_STATE_LOCKED = 1;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Integer id = session == null ? null : (Integer) session.getAttribute("login");
        if (id == null) {
            writeUnauthorized(response);
            return false;
        }

        User user = userMapper.selectById(id);
        if (user == null || Objects.equals(user.getIsLocked(), USER_STATE_LOCKED)) {
            session.invalidate();
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
                Result.error(Code.USER_LOGIN_SESSION_EMPTY_ERROR, Code.USER_LOGIN_SESSION_EMPTY_ERROR_MSG));
    }
}
