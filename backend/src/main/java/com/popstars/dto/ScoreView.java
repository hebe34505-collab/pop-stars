package com.popstars.dto;

/**
 * 一条成绩记录。
 */
public class ScoreView {

    public Long id;
    public Long userId;
    public String username;
    public String nickname;
    public int levelNo;
    public String levelName;
    public int score;
    public int targetScore;
    public int movesUsed;
    public boolean win;
    public String createdAt;
}
