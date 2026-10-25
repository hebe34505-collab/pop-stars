package com.popstars.service;

import com.popstars.dto.NoticeRequest;
import com.popstars.dto.NoticeView;
import com.popstars.entity.Notice;
import com.popstars.repository.NoticeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 发布和查看公告。
 */
@Service
public class NoticeService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<NoticeView> list() {
        List<NoticeView> result = new ArrayList<>();
        for (Notice notice : noticeRepository.findAllByOrderByIdDesc()) {
            result.add(toView(notice));
        }
        return result;
    }

    public NoticeView create(NoticeRequest request) {
        String title = request.title == null ? "" : request.title.trim();
        String content = request.content == null ? "" : request.content.trim();
        if (title.isEmpty() || title.length() > 30) {
            throw new IllegalArgumentException("公告标题需要 1 到 30 个字");
        }
        if (content.isEmpty() || content.length() > 300) {
            throw new IllegalArgumentException("公告内容需要 1 到 300 个字");
        }
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setCreatedAt(LocalDateTime.now());
        noticeRepository.save(notice);
        return toView(notice);
    }

    public void delete(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new IllegalArgumentException("这条公告不存在");
        }
        noticeRepository.deleteById(id);
    }

    private NoticeView toView(Notice notice) {
        NoticeView view = new NoticeView();
        view.id = notice.getId();
        view.title = notice.getTitle();
        view.content = notice.getContent();
        view.createdAt = notice.getCreatedAt().format(TIME_FORMAT);
        return view;
    }
}
