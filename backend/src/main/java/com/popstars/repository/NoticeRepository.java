package com.popstars.repository;

import com.popstars.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 公告表。
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByIdDesc();
}
