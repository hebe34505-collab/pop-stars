package com.popstars.controller;

import com.popstars.config.LoginInterceptor;
import com.popstars.dto.MyScoreView;
import com.popstars.dto.ScoreRequest;
import com.popstars.dto.ScoreView;
import com.popstars.entity.UserAccount;
import com.popstars.service.ScoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 保存成绩，并查看自己的最高分和历史记录。
 */
@RestController
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping("/api/scores")
    public ScoreView save(HttpServletRequest request, @RequestBody ScoreRequest body) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        return scoreService.save(user, body);
    }

    @GetMapping("/api/scores/me")
    public MyScoreView me(HttpServletRequest request) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        return scoreService.myScores(user);
    }
}
