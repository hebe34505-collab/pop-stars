package com.popstars.config;

import com.popstars.entity.LevelSetting;
import com.popstars.entity.Notice;
import com.popstars.entity.UserAccount;
import com.popstars.repository.LevelSettingRepository;
import com.popstars.repository.NoticeRepository;
import com.popstars.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 保证数据库里有 10 关。已经存在的关卡不会被覆盖。
 * targetScore 是这一关要新得到的分数，实际过关线还要加上上一关结算分。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final LevelSettingRepository levelSettingRepository;
    private final UserAccountRepository userAccountRepository;
    private final NoticeRepository noticeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(LevelSettingRepository levelSettingRepository,
                           UserAccountRepository userAccountRepository,
                           NoticeRepository noticeRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.levelSettingRepository = levelSettingRepository;
        this.userAccountRepository = userAccountRepository;
        this.noticeRepository = noticeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 早期版本只有一条关卡，没有关卡号。把它保留为第 3 关。
        for (LevelSetting item : levelSettingRepository.findAll()) {
            if (item.getLevelNo() == 0) {
                item.setLevelNo(3);
                item.setLevelName("标准");
                levelSettingRepository.save(item);
            }
        }

        ensureLevel(1, "入门", 6, 6, 3, 400, 15);
        ensureLevel(2, "进阶", 8, 8, 4, 800, 18);
        ensureLevel(3, "标准", 10, 10, 5, 1000, 20);
        ensureLevel(4, "挑战", 10, 10, 5, 1800, 18);
        ensureLevel(5, "高手", 10, 10, 6, 2500, 16);
        ensureLevel(6, "第6关", 12, 12, 5, 2800, 22);
        ensureLevel(7, "第7关", 12, 12, 5, 3200, 20);
        ensureLevel(8, "第8关", 12, 12, 6, 3600, 20);
        ensureLevel(9, "第9关", 12, 12, 6, 4000, 18);
        ensureLevel(10, "第10关", 12, 12, 6, 4600, 16);
        ensureAdmin();
        ensureNotice();
    }

    /** 没有公告时放一条欢迎说明，避免公告页是空的。 */
    private void ensureNotice() {
        if (noticeRepository.count() > 0) {
            return;
        }
        Notice notice = new Notice();
        notice.setTitle("欢迎来到消灭星星");
        notice.setContent("从第 1 关开始，分数会累计到下一关。遇到问题可以在「客服」留言，也可以在「好友」里添加其他玩家。");
        notice.setCreatedAt(LocalDateTime.now());
        noticeRepository.save(notice);
    }

    /** 默认管理员。密码只存加密后的结果。 */
    private void ensureAdmin() {
        if (userAccountRepository.findByUsername("admin").isPresent()) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("管理员");
        admin.setRole("ADMIN");
        admin.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(admin);
    }

    private void ensureLevel(int levelNo, String levelName, int rows, int cols,
                             int colorCount, int targetScore, int maxMoves) {
        if (levelSettingRepository.findByLevelNo(levelNo).isPresent()) {
            return;
        }
        LevelSetting level = new LevelSetting();
        level.setLevelNo(levelNo);
        level.setLevelName(levelName);
        level.setRowCount(rows);
        level.setColCount(cols);
        level.setColorCount(colorCount);
        level.setTargetScore(targetScore);
        level.setMaxMoves(maxMoves);
        levelSettingRepository.save(level);
    }
}
