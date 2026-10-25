package com.popstars.controller;

import com.popstars.config.LoginInterceptor;
import com.popstars.dto.MessageView;
import com.popstars.dto.TextRequest;
import com.popstars.entity.UserAccount;
import com.popstars.service.ServiceDeskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 联系客服。玩家留言，管理员在管理页回复。
 */
@RestController
public class ServiceController {

    private final ServiceDeskService serviceDeskService;

    public ServiceController(ServiceDeskService serviceDeskService) {
        this.serviceDeskService = serviceDeskService;
    }

    @PostMapping("/api/service")
    public MessageView leave(HttpServletRequest request, @RequestBody TextRequest body) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        return serviceDeskService.leave(user, body);
    }

    @GetMapping("/api/service/mine")
    public List<MessageView> mine(HttpServletRequest request) {
        UserAccount user = (UserAccount) request.getAttribute(LoginInterceptor.LOGIN_USER);
        return serviceDeskService.mine(user);
    }

    @GetMapping("/api/admin/service")
    public List<MessageView> all() {
        return serviceDeskService.all();
    }

    @PutMapping("/api/admin/service/{id}")
    public MessageView reply(@PathVariable Long id, @RequestBody TextRequest body) {
        return serviceDeskService.reply(id, body);
    }
}
