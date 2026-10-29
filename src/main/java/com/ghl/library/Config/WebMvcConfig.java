package com.ghl.library.Config;

import com.ghl.library.Interceptor.AdminLoginInterceptor;
import com.ghl.library.Interceptor.UserLoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置：注册登录拦截器，区分读者端与管理端会话。
 *
 * @author ghl
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private UserLoginInterceptor userLoginInterceptor;

    @Resource
    private AdminLoginInterceptor adminLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/login");

        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
    }
}
