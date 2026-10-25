package com.popstars.controller;

import com.popstars.config.LoginInterceptor;
import com.popstars.dto.LevelConfig;
import com.popstars.dto.ScoreView;
import com.popstars.dto.UserView;
import com.popstars.entity.UserAccount;
import com.popstars.service.LevelService;
import com.popstars.service.ScoreService;
import com.popstars.service.UserAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员接口。登录拦截器会先确认角色是 ADMIN。
 */
@RestController
public class AdminController {

    private final UserAdminService userAdminService;
    private final ScoreService scoreService;
    private final LevelService levelService;

    public AdminController(UserAdminService userAdminService,
                           ScoreService scoreService,
                           LevelService levelService) {
        this.userAdminService = userAdminService;
        this.scoreService = scoreService;
        this.levelService = levelService;
    }

    @GetMapping("/api/admin/users")
    public List<UserView> users() {
        return userAdminService.listUsers();
    }

    @DeleteMapping("/api/admin/users/{id}")
    public void deleteUser(HttpServletRequest request, @PathVariable Long id) {
        UserAccount current = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        userAdminService.deleteUser(current, id);
    }

    @GetMapping("/api/admin/scores")
    public List<ScoreView> scores() {
        return scoreService.allScores();
    }

    @DeleteMapping("/api/admin/scores/{id}")
    public void deleteScore(@PathVariable Long id) {
        scoreService.deleteScore(id);
    }

    @PutMapping("/api/admin/levels/{levelNo}")
    public LevelConfig updateLevel(@PathVariable int levelNo, @RequestBody LevelConfig body) {
        return levelService.updateLevel(levelNo, body);
    }
}
