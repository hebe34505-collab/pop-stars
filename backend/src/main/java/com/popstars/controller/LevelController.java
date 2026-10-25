package com.popstars.controller;

import com.popstars.dto.LevelConfig;
import com.popstars.service.LevelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 把数据库中的关卡配置交给前端。
 * 游戏开始和重新开始时都会读取这个接口。
 */
@RestController
public class LevelController {

    private final LevelService levelService;

    public LevelController(LevelService levelService) {
        this.levelService = levelService;
    }

    @GetMapping("/api/level")
    public LevelConfig level() {
        return levelService.getLevel();
    }

    @GetMapping("/api/levels")
    public List<LevelConfig> levels() {
        return levelService.listLevels();
    }
}
