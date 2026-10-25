package com.popstars.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 保存在 MySQL 里的关卡配置。
 */
@Entity
@Table(name = "level_config")
public class LevelSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 第几关，从 1 开始。 */
    @Column(name = "level_no", nullable = false, unique = true)
    private int levelNo;

    @Column(name = "level_name", nullable = false, length = 16)
    private String levelName;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(name = "col_count", nullable = false)
    private int colCount;

    @Column(name = "color_count", nullable = false)
    private int colorCount;

    @Column(name = "target_score", nullable = false)
    private int targetScore;

    @Column(name = "max_moves", nullable = false)
    private int maxMoves;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLevelNo() {
        return levelNo;
    }

    public void setLevelNo(int levelNo) {
        this.levelNo = levelNo;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public void setColCount(int colCount) {
        this.colCount = colCount;
    }

    public int getColorCount() {
        return colorCount;
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }

    public int getMaxMoves() {
        return maxMoves;
    }

    public void setMaxMoves(int maxMoves) {
        this.maxMoves = maxMoves;
    }
}
