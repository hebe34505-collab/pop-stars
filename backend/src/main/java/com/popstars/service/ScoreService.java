package com.popstars.service;

import com.popstars.dto.MyScoreView;
import com.popstars.dto.ScoreRequest;
import com.popstars.dto.ScoreView;
import com.popstars.entity.GameScore;
import com.popstars.entity.LevelSetting;
import com.popstars.entity.UserAccount;
import com.popstars.repository.GameScoreRepository;
import com.popstars.repository.LevelSettingRepository;
import com.popstars.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 保存和查询游戏成绩。
 */
@Service
public class ScoreService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GameScoreRepository gameScoreRepository;
    private final LevelSettingRepository levelSettingRepository;
    private final UserAccountRepository userAccountRepository;

    public ScoreService(GameScoreRepository gameScoreRepository,
                        LevelSettingRepository levelSettingRepository,
                        UserAccountRepository userAccountRepository) {
        this.gameScoreRepository = gameScoreRepository;
        this.levelSettingRepository = levelSettingRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public ScoreView save(UserAccount user, ScoreRequest request) {
        if (request.levelNo == null || request.score == null || request.targetScore == null
                || request.movesUsed == null || request.win == null) {
            throw new IllegalArgumentException("成绩数据不完整");
        }
        if (request.score < 0 || request.score > 999999 || request.movesUsed < 0) {
            throw new IllegalArgumentException("成绩数据不正确");
        }
        LevelSetting level = levelSettingRepository.findByLevelNo(request.levelNo)
                .orElseThrow(() -> new IllegalArgumentException("关卡不存在"));
        if (request.levelNo > openedLevel(user)) {
            throw new IllegalArgumentException("请先通过上一关");
        }
        int carried = carriedScore(user, level.getLevelNo());
        int need = carried + level.getTargetScore();
        if (request.score < carried) {
            throw new IllegalArgumentException("分数不能低于上一关结算");
        }
        if (request.win && request.score < need) {
            throw new IllegalArgumentException("还没有达到过关分数");
        }

        GameScore record = new GameScore();
        record.setUserId(user.getId());
        record.setLevelNo(level.getLevelNo());
        record.setScore(request.score);
        record.setTargetScore(need);
        record.setMovesUsed(request.movesUsed);
        record.setWin(request.win);
        record.setCreatedAt(LocalDateTime.now());
        gameScoreRepository.save(record);
        return toView(record, user, level.getLevelName());
    }

    public MyScoreView myScores(UserAccount user) {
        MyScoreView result = new MyScoreView();
        GameScore best = gameScoreRepository.findFirstByUserIdOrderByScoreDesc(user.getId()).orElse(null);
        result.bestScore = best == null ? 0 : best.getScore();
        result.unlockedLevel = openedLevel(user);
        result.records = toViews(gameScoreRepository.findByUserIdOrderByIdDesc(user.getId()));
        return result;
    }

    /**
     * 第 1 关从 0 分开始。后面每一关，从上一关最近一次胜利的分数接着打。
     */
    private int carriedScore(UserAccount user, int levelNo) {
        int previous = 0;
        for (LevelSetting item : levelSettingRepository.findAllByOrderByLevelNoAsc()) {
            if (item.getLevelNo() == levelNo) {
                break;
            }
            previous = item.getLevelNo();
        }
        if (previous == 0) {
            return 0;
        }
        GameScore win = gameScoreRepository
                .findFirstByUserIdAndLevelNoAndWinTrueOrderByIdDesc(user.getId(), previous)
                .orElse(null);
        return win == null ? 0 : win.getScore();
    }

    /**
     * 第 1 关直接开放。后面每一关，都要先有上一关的胜利记录。
     */
    private int openedLevel(UserAccount user) {
        List<LevelSetting> levels = levelSettingRepository.findAllByOrderByLevelNoAsc();
        int opened = 0;
        int previous = 0;
        for (LevelSetting item : levels) {
            if (previous != 0 && !gameScoreRepository.existsByUserIdAndLevelNoAndWinTrue(user.getId(), previous)) {
                break;
            }
            opened = item.getLevelNo();
            previous = item.getLevelNo();
        }
        return opened;
    }

    public List<ScoreView> allScores() {
        return toViews(gameScoreRepository.findAllByOrderByIdDesc());
    }

    public void deleteScore(Long id) {
        if (!gameScoreRepository.existsById(id)) {
            throw new IllegalArgumentException("这条成绩不存在");
        }
        gameScoreRepository.deleteById(id);
    }

    @Transactional
    public void deleteScoresOfUser(Long userId) {
        gameScoreRepository.deleteByUserId(userId);
    }

    private List<ScoreView> toViews(List<GameScore> records) {
        List<ScoreView> result = new ArrayList<>();
        for (GameScore record : records) {
            UserAccount user = userAccountRepository.findById(record.getUserId()).orElse(null);
            LevelSetting level = levelSettingRepository.findByLevelNo(record.getLevelNo()).orElse(null);
            String levelName = level == null ? "第" + record.getLevelNo() + "关" : level.getLevelName();
            result.add(toView(record, user, levelName));
        }
        return result;
    }

    private ScoreView toView(GameScore record, UserAccount user, String levelName) {
        ScoreView view = new ScoreView();
        view.id = record.getId();
        view.userId = record.getUserId();
        view.username = user == null ? "" : user.getUsername();
        view.nickname = user == null ? "已删除用户" : user.getNickname();
        view.levelNo = record.getLevelNo();
        view.levelName = levelName;
        view.score = record.getScore();
        view.targetScore = record.getTargetScore();
        view.movesUsed = record.getMovesUsed();
        view.win = record.isWin();
        view.createdAt = record.getCreatedAt() == null ? "" : record.getCreatedAt().format(TIME_FORMAT);
        return view;
    }
}
