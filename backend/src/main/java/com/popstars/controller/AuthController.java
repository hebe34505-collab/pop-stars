package com.popstars.controller;

import com.popstars.config.LoginInterceptor;
import com.popstars.dto.LoginRequest;
import com.popstars.dto.LoginResult;
import com.popstars.dto.NicknameRequest;
import com.popstars.dto.RegisterRequest;
import com.popstars.dto.UserView;
import com.popstars.entity.UserAccount;
import com.popstars.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册、登录、退出和昵称。
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/register")
    public UserView register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/api/auth/login")
    public LoginResult login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/api/auth/logout")
    public void logout(HttpServletRequest request) {
        authService.logout(current(request));
    }

    @GetMapping("/api/auth/me")
    public UserView me(HttpServletRequest request) {
        return authService.toView(current(request));
    }

    @PutMapping("/api/auth/nickname")
    public UserView nickname(HttpServletRequest request, @RequestBody NicknameRequest body) {
        return authService.changeNickname(current(request), body.nickname);
    }

    private UserAccount current(HttpServletRequest request) {
        return (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
    }
}
