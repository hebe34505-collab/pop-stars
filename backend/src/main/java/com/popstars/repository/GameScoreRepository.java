package com.popstars.repository;

import com.popstars.entity.GameScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 游戏成绩表的数据库操作。
 */
public interface GameScoreRepository extends JpaRepository<GameScore, Long> {

    List<GameScore> findByUserIdOrderByIdDesc(Long userId);

    Optional<GameScore> findFirstByUserIdOrderByScoreDesc(Long userId);

    List<GameScore> findAllByOrderByIdDesc();

    void deleteByUserId(Long userId);

    boolean existsByUserIdAndLevelNoAndWinTrue(Long userId, int levelNo);

    Optional<GameScore> findFirstByUserIdAndLevelNoAndWinTrueOrderByIdDesc(Long userId, int levelNo);
}
