package com.popstars.service;

import com.popstars.dto.MessageView;
import com.popstars.dto.TextRequest;
import com.popstars.entity.ServiceMessage;
import com.popstars.entity.UserAccount;
import com.popstars.repository.ServiceMessageRepository;
import com.popstars.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家留言，管理员回复。不是即时聊天。
 */
@Service
public class ServiceDeskService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ServiceMessageRepository serviceMessageRepository;
    private final UserAccountRepository userAccountRepository;

    public ServiceDeskService(ServiceMessageRepository serviceMessageRepository,
                              UserAccountRepository userAccountRepository) {
        this.serviceMessageRepository = serviceMessageRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public MessageView leave(UserAccount user, TextRequest request) {
        String content = request.content == null ? "" : request.content.trim();
        if (content.isEmpty() || content.length() > 200) {
            throw new IllegalArgumentException("留言需要 1 到 200 个字");
        }
        ServiceMessage message = new ServiceMessage();
        message.setUserId(user.getId());
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        serviceMessageRepository.save(message);
        return toView(message, user);
    }

    public List<MessageView> mine(UserAccount user) {
        return toViews(serviceMessageRepository.findByUserIdOrderByIdDesc(user.getId()));
    }

    public List<MessageView> all() {
        return toViews(serviceMessageRepository.findAllByOrderByIdDesc());
    }

    public MessageView reply(Long id, TextRequest request) {
        ServiceMessage message = serviceMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("这条留言不存在"));
        String reply = request.reply == null ? "" : request.reply.trim();
        if (reply.isEmpty() || reply.length() > 200) {
            throw new IllegalArgumentException("回复需要 1 到 200 个字");
        }
        message.setReply(reply);
        message.setRepliedAt(LocalDateTime.now());
        serviceMessageRepository.save(message);
        UserAccount user = userAccountRepository.findById(message.getUserId()).orElse(null);
        return toView(message, user);
    }

    public void deleteByUser(Long userId) {
        serviceMessageRepository.deleteByUserId(userId);
    }

    private List<MessageView> toViews(List<ServiceMessage> messages) {
        List<MessageView> result = new ArrayList<>();
        for (ServiceMessage message : messages) {
            UserAccount user = userAccountRepository.findById(message.getUserId()).orElse(null);
            result.add(toView(message, user));
        }
        return result;
    }

    private MessageView toView(ServiceMessage message, UserAccount user) {
        MessageView view = new MessageView();
        view.id = message.getId();
        view.username = user == null ? "" : user.getUsername();
        view.nickname = user == null ? "已删除用户" : user.getNickname();
        view.content = message.getContent();
        view.reply = message.getReply() == null ? "" : message.getReply();
        view.createdAt = message.getCreatedAt().format(TIME_FORMAT);
        view.repliedAt = message.getRepliedAt() == null ? "" : message.getRepliedAt().format(TIME_FORMAT);
        return view;
    }
}
