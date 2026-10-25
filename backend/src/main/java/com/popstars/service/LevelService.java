package com.popstars.service;

import com.popstars.dto.LevelConfig;
import com.popstars.entity.LevelSetting;
import com.popstars.repository.LevelSettingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 从数据库读取关卡，再交给接口返回。
 */
@Service
public class LevelService {

    private final LevelSettingRepository levelSettingRepository;

    public LevelService(LevelSettingRepository levelSettingRepository) {
        this.levelSettingRepository = levelSettingRepository;
    }

    /** 全部关卡，按关卡号从小到大。 */
    public List<LevelConfig> listLevels() {
        List<LevelConfig> result = new ArrayList<>();
        for (LevelSetting setting : levelSettingRepository.findAllByOrderByLevelNoAsc()) {
            result.add(toConfig(setting));
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("数据库里还没有关卡配置");
        }
        return result;
    }

    /** 管理员修改某一关的目标分数和开局网格。 */
    public LevelConfig updateLevel(int levelNo, LevelConfig input) {
        LevelSetting setting = levelSettingRepository.findByLevelNo(levelNo)
                .orElseThrow(() -> new IllegalArgumentException("关卡不存在"));
        if (input.getRows() < 4 || input.getRows() > 12 || input.getCols() < 4 || input.getCols() > 12) {
            throw new IllegalArgumentException("行数和列数要在 4 到 12 之间");
        }
        if (input.getColorCount() < 2 || input.getColorCount() > 6) {
            throw new IllegalArgumentException("颜色数量要在 2 到 6 之间");
        }
        if (input.getTargetScore() <= 0 || input.getMaxMoves() <= 0) {
            throw new IllegalArgumentException("目标分数和步数必须大于 0");
        }

        setting.setRowCount(input.getRows());
        setting.setColCount(input.getCols());
        setting.setColorCount(input.getColorCount());
        setting.setTargetScore(input.getTargetScore());
        setting.setMaxMoves(input.getMaxMoves());
        levelSettingRepository.save(setting);
        return toConfig(setting);
    }

    /** 第 1 关。旧的单个关卡接口继续使用。 */
    public LevelConfig getLevel() {
        LevelSetting setting = levelSettingRepository.findByLevelNo(1)
                .orElseThrow(() -> new IllegalStateException("数据库里还没有第 1 关"));
        return toConfig(setting);
    }

    private LevelConfig toConfig(LevelSetting setting) {
        LevelConfig config = new LevelConfig();
        config.setLevelNo(setting.getLevelNo());
        config.setLevelName(setting.getLevelName());
        config.setRows(setting.getRowCount());
        config.setCols(setting.getColCount());
        config.setColorCount(setting.getColorCount());
        config.setTargetScore(setting.getTargetScore());
        config.setMaxMoves(setting.getMaxMoves());
        return config;
    }
}
