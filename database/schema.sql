-- 消灭星星数据库。可以在 MySQL 里整份执行。
-- 后端启动时也会自动建库建表，并补齐 10 个关卡。
-- target_score 是这一关要新得到的分数。实际过关线 = 上一关结算分 + target_score。

CREATE DATABASE IF NOT EXISTS pop_stars
  DEFAULT CHARACTER SET utf8mb4;

USE pop_stars;

-- 注册用户。password 以后只存加密后的密码，不存明文。
CREATE TABLE IF NOT EXISTS user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(32) NOT NULL,
  role VARCHAR(16) NOT NULL,
  token VARCHAR(64) NULL UNIQUE,
  created_at DATETIME NOT NULL
);

-- 关卡配置。一共 10 关，游戏按 level_no 从小到大读取。
CREATE TABLE IF NOT EXISTS level_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  level_no INT NOT NULL UNIQUE,
  level_name VARCHAR(16) NOT NULL,
  row_count INT NOT NULL,
  col_count INT NOT NULL,
  color_count INT NOT NULL,
  target_score INT NOT NULL,
  max_moves INT NOT NULL
);

-- 每一局结束后的成绩。只有登录玩家才会写入。
CREATE TABLE IF NOT EXISTS game_score (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  level_no INT NOT NULL,
  score INT NOT NULL,
  target_score INT NOT NULL,
  moves_used INT NOT NULL,
  win TINYINT NOT NULL,
  created_at DATETIME NOT NULL
);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 1, '入门', 6, 6, 3, 400, 15
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 1);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 2, '进阶', 8, 8, 4, 800, 18
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 2);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 3, '标准', 10, 10, 5, 1000, 20
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 3);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 4, '挑战', 10, 10, 5, 1800, 18
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 4);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 5, '高手', 10, 10, 6, 2500, 16
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 5);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 6, '第6关', 12, 12, 5, 2800, 22
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 6);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 7, '第7关', 12, 12, 5, 3200, 20
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 7);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 8, '第8关', 12, 12, 6, 3600, 20
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 8);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 9, '第9关', 12, 12, 6, 4000, 18
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 9);

INSERT INTO level_config (level_no, level_name, row_count, col_count, color_count, target_score, max_moves)
SELECT 10, '第10关', 12, 12, 6, 4600, 16
WHERE NOT EXISTS (SELECT 1 FROM level_config WHERE level_no = 10);

-- 公告。玩家只读，管理员发布。
CREATE TABLE IF NOT EXISTS notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(40) NOT NULL,
  content VARCHAR(300) NOT NULL,
  created_at DATETIME NOT NULL
);

-- 客服留言。reply 为空表示还没有回复。
CREATE TABLE IF NOT EXISTS service_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  content VARCHAR(200) NOT NULL,
  reply VARCHAR(200) NULL,
  created_at DATETIME NOT NULL,
  replied_at DATETIME NULL
);

-- 好友申请。status 为 PENDING 或 ACCEPTED。
CREATE TABLE IF NOT EXISTS friendship (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_friend_pair (user_id, friend_id)
);
