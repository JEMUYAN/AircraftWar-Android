package edu.hitsz.aircraftwar2026.game;

/**
 * 随难度和游戏时间变化的可调参数，由 {@link DifficultyRules} 初始化、调整。
 */
public class DifficultyState {

    /** 屏幕中敌机的最大数量 */
    public int enemyMaxNumber = 5;
    /** 敌机生成周期（帧） */
    public double enemySpawnCycle = 40;
    /** 射击周期（帧），英雄机与敌机共用 */
    public double shootCycle = 20;
    /** Boss 出现的分数阈值，每次召唤后递增 */
    public int bossScoreThreshold = 60;
    /** Boss 分数阈值每次的递增量 */
    public int bossScoreIncrement = 100;
    /** Boss 已召唤次数 */
    public int bossSpawnCount = 0;
    /** 敌机属性（速度、血量）倍率，随时间提升 */
    public double enemyAttributeMultiplier = 1.0;
    /** 难度计时器（帧） */
    public int difficultyCounter = 0;
}
