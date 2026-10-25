package com.popstars.repository;

import com.popstars.entity.ServiceMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 客服留言表。
 */
public interface ServiceMessageRepository extends JpaRepository<ServiceMessage, Long> {

    List<ServiceMessage> findByUserIdOrderByIdDesc(Long userId);

    List<ServiceMessage> findAllByOrderByIdDesc();

    void deleteByUserId(Long userId);
}
