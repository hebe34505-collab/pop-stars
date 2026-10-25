package com.popstars.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 需要登录的接口在这里登记。
 * 关卡查询、注册和登录保持公开。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/api/auth/me",
                        "/api/auth/logout",
                        "/api/auth/nickname",
                        "/api/scores/**",
                        "/api/service/**",
                        "/api/friends/**",
                        "/api/admin/**"
                );
    }
}
