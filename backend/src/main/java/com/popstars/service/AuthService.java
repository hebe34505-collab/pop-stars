package com.popstars.service;

import com.popstars.dto.LoginRequest;
import com.popstars.dto.LoginResult;
import com.popstars.dto.RegisterRequest;
import com.popstars.dto.UserView;
import com.popstars.entity.UserAccount;
import com.popstars.repository.UserAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 注册、登录、退出和修改昵称。
 */
@Service
public class AuthService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserAccountRepository userAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserView register(RegisterRequest request) {
        String username = request.username == null ? "" : request.username.trim();
        String password = request.password == null ? "" : request.password;
        String nickname = request.nickname == null ? "" : request.nickname.trim();
        checkUsername(username);
        checkPassword(password);
        if (nickname.isEmpty()) {
            nickname = username;
        }
        checkNickname(nickname);
        if (userAccountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("这个用户名已经注册过了");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(user);
        return toView(user);
    }

    public LoginResult login(LoginRequest request) {
        String username = request.username == null ? "" : request.username.trim();
        String password = request.password == null ? "" : request.password;
        UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码不正确");
        }

        user.setToken(UUID.randomUUID().toString().replace("-", ""));
        userAccountRepository.save(user);

        LoginResult result = new LoginResult();
        result.token = user.getToken();
        result.user = toView(user);
        return result;
    }

    public void logout(UserAccount user) {
        user.setToken(null);
        userAccountRepository.save(user);
    }

    public UserView changeNickname(UserAccount user, String nickname) {
        String value = nickname == null ? "" : nickname.trim();
        checkNickname(value);
        user.setNickname(value);
        userAccountRepository.save(user);
        return toView(user);
    }

    public UserView toView(UserAccount user) {
        UserView view = new UserView();
        view.id = user.getId();
        view.username = user.getUsername();
        view.nickname = user.getNickname();
        view.role = user.getRole();
        view.createdAt = user.getCreatedAt() == null ? "" : user.getCreatedAt().format(TIME_FORMAT);
        return view;
    }

    private void checkUsername(String username) {
        if (!username.matches("[A-Za-z0-9]{3,16}")) {
            throw new IllegalArgumentException("用户名需要 3 到 16 位字母或数字");
        }
    }

    private void checkPassword(String password) {
        if (password.length() < 6 || password.length() > 20) {
            throw new IllegalArgumentException("密码需要 6 到 20 位");
        }
    }

    private void checkNickname(String nickname) {
        if (nickname.isEmpty() || nickname.length() > 16) {
            throw new IllegalArgumentException("昵称需要 1 到 16 个字");
        }
    }
}
