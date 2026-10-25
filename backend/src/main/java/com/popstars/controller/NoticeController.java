package com.popstars.controller;

import com.popstars.dto.NoticeRequest;
import com.popstars.dto.NoticeView;
import com.popstars.service.NoticeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公告。查看公开，发布和删除只有管理员可以。
 */
@RestController
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/api/notices")
    public List<NoticeView> list() {
        return noticeService.list();
    }

    @PostMapping("/api/admin/notices")
    public NoticeView create(@RequestBody NoticeRequest body) {
        return noticeService.create(body);
    }

    @DeleteMapping("/api/admin/notices/{id}")
    public void delete(@PathVariable Long id) {
        noticeService.delete(id);
    }
}
