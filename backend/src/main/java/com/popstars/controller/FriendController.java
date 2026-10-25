package com.popstars.controller;

import com.popstars.config.LoginInterceptor;
import com.popstars.dto.FriendBoard;
import com.popstars.dto.TextRequest;
import com.popstars.entity.UserAccount;
import com.popstars.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 添加好友、同意申请、删除好友。
 */
@RestController
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/api/friends")
    public FriendBoard board(HttpServletRequest request) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        return friendService.board(user);
    }

    @PostMapping("/api/friends")
    public void request(HttpServletRequest request, @RequestBody TextRequest body) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        friendService.request(user, body);
    }

    @PostMapping("/api/friends/{id}/accept")
    public void accept(HttpServletRequest request, @PathVariable Long id) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        friendService.accept(user, id);
    }

    @DeleteMapping("/api/friends/{id}")
    public void remove(HttpServletRequest request, @PathVariable Long id) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        friendService.remove(user, id);
    }
}
