package com.popstars.service;

import com.popstars.dto.UserView;
import com.popstars.entity.UserAccount;
import com.popstars.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员查看和删除注册用户。
 */
@Service
public class UserAdminService {

    private final UserAccountRepository userAccountRepository;
    private final ScoreService scoreService;
    private final AuthService authService;
    private final ServiceDeskService serviceDeskService;
    private final FriendService friendService;

    public UserAdminService(UserAccountRepository userAccountRepository,
                            ScoreService scoreService,
                            AuthService authService,
                            ServiceDeskService serviceDeskService,
                            FriendService friendService) {
        this.userAccountRepository = userAccountRepository;
        this.scoreService = scoreService;
        this.authService = authService;
        this.serviceDeskService = serviceDeskService;
        this.friendService = friendService;
    }

    public List<UserView> listUsers() {
        List<UserView> result = new ArrayList<>();
        for (UserAccount user : userAccountRepository.findAll()) {
            result.add(authService.toView(user));
        }
        return result;
    }

    @Transactional
    public void deleteUser(UserAccount current, Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (user.getId().equals(current.getId())) {
            throw new IllegalArgumentException("不能删除当前登录的账号");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }
        scoreService.deleteScoresOfUser(user.getId());
        serviceDeskService.deleteByUser(user.getId());
        friendService.deleteByUser(user.getId());
        userAccountRepository.delete(user);
    }
}
