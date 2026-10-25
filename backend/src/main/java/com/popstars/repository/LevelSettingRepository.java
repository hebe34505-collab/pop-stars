package com.popstars.repository;

import com.popstars.entity.LevelSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 关卡配置表的数据库操作。
 */
public interface LevelSettingRepository extends JpaRepository<LevelSetting, Long> {

    Optional<LevelSetting> findByLevelNo(int levelNo);

    List<LevelSetting> findAllByOrderByLevelNoAsc();
}
