package com.popstars.dto;

/**
 * 一局结束后前端提交的成绩。
 */
public class ScoreRequest {

    public Integer levelNo;
    public Integer score;
    public Integer targetScore;
    public Integer movesUsed;
    public Boolean win;
}
