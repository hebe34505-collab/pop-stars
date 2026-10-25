package com.popstars.config;

import com.popstars.entity.UserAccount;
import com.popstars.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 检查请求头里的登录令牌。
 * 管理员接口还会再检查角色，不能只靠前端把按钮藏起来。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String LOGIN_USER = "loginUser";

    private final UserAccountRepository userAccountRepository;

    public LoginInterceptor(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7).trim();
        }

        UserAccount user = null;
        if (token != null && !token.isEmpty()) {
            user = userAccountRepository.findByToken(token).orElse(null);
        }
        if (user == null) {
            writeJson(response, 401, "请先登录");
            return false;
        }

        if (request.getRequestURI().startsWith("/api/admin") && !"ADMIN".equals(user.getRole())) {
            writeJson(response, 403, "只有管理员可以操作");
            return false;
        }

        request.setAttribute(LOGIN_USER, user);
        return true;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
