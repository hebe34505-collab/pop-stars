package com.popstars.service;

import com.popstars.dto.FriendBoard;
import com.popstars.dto.FriendView;
import com.popstars.dto.TextRequest;
import com.popstars.entity.Friendship;
import com.popstars.entity.UserAccount;
import com.popstars.repository.FriendshipRepository;
import com.popstars.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 添加好友需要对方同意。
 */
@Service
public class FriendService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FriendshipRepository friendshipRepository;
    private final UserAccountRepository userAccountRepository;

    public FriendService(FriendshipRepository friendshipRepository,
                         UserAccountRepository userAccountRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public FriendBoard board(UserAccount user) {
        FriendBoard board = new FriendBoard();
        for (Friendship row : friendshipRepository.findAccepted(user.getId())) {
            board.friends.add(toView(row, otherId(row, user.getId())));
        }
        for (Friendship row : friendshipRepository.findByFriendIdAndStatusOrderByIdDesc(user.getId(), "PENDING")) {
            board.incoming.add(toView(row, row.getUserId()));
        }
        for (Friendship row : friendshipRepository.findByUserIdAndStatusOrderByIdDesc(user.getId(), "PENDING")) {
            board.outgoing.add(toView(row, row.getFriendId()));
        }
        return board;
    }

    public void request(UserAccount user, TextRequest body) {
        String username = body.username == null ? "" : body.username.trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("请填写对方的用户名");
        }
        if (username.equals(user.getUsername())) {
            throw new IllegalArgumentException("不能添加自己");
        }
        UserAccount target = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("没有这个用户"));
        if (friendshipRepository.findByUserIdAndFriendId(user.getId(), target.getId()).isPresent()
                || friendshipRepository.findByUserIdAndFriendId(target.getId(), user.getId()).isPresent()) {
            throw new IllegalArgumentException("已经是好友，或申请还在处理中");
        }
        Friendship row = new Friendship();
        row.setUserId(user.getId());
        row.setFriendId(target.getId());
        row.setStatus("PENDING");
        row.setCreatedAt(LocalDateTime.now());
        friendshipRepository.save(row);
    }

    public void accept(UserAccount user, Long id) {
        Friendship row = friendshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("这条申请不存在"));
        if (!user.getId().equals(row.getFriendId())) {
            throw new IllegalArgumentException("只能同意发给自己的申请");
        }
        if (!"PENDING".equals(row.getStatus())) {
            throw new IllegalArgumentException("这条申请已经处理过了");
        }
        row.setStatus("ACCEPTED");
        friendshipRepository.save(row);
    }

    public void remove(UserAccount user, Long id) {
        Friendship row = friendshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("这条关系不存在"));
        if (!user.getId().equals(row.getUserId()) && !user.getId().equals(row.getFriendId())) {
            throw new IllegalArgumentException("不能操作别人的好友关系");
        }
        friendshipRepository.delete(row);
    }

    public void deleteByUser(Long userId) {
        friendshipRepository.deleteByUserIdOrFriendId(userId, userId);
    }

    private Long otherId(Friendship row, Long selfId) {
        return selfId.equals(row.getUserId()) ? row.getFriendId() : row.getUserId();
    }

    private FriendView toView(Friendship row, Long personId) {
        UserAccount person = userAccountRepository.findById(personId).orElse(null);
        FriendView view = new FriendView();
        view.id = row.getId();
        view.username = person == null ? "" : person.getUsername();
        view.nickname = person == null ? "已删除用户" : person.getNickname();
        view.createdAt = row.getCreatedAt().format(TIME_FORMAT);
        return view;
    }
}
