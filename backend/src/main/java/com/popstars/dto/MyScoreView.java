package com.popstars.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人最高分和历史记录。
 */
public class MyScoreView {

    public int bestScore;
    public int unlockedLevel;
    public List<ScoreView> records = new ArrayList<>();
}
