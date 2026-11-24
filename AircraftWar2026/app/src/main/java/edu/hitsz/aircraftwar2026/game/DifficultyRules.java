package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.aircraft.EnemyFactories;

/**
 * 难度规则（模板方法/策略）：各难度只需实现自己的参数、敌机构成和难度提升方式，
 * 游戏主流程由 {@link GameEngine} 统一驱动。
 */
public abstract class DifficultyRules {

    /** Boss 基础血量 */
    protected static final int BOSS_BASE_HP = 150;

    /** 对应的难度枚举 */
    public abstract Difficulty getDifficulty();

    /** 设置该难度的初始参数 */
    public abstract void configure(DifficultyState state);

    /**
     * 按该难度的概率生成一架敌机。
     *
     * @param locX  生成 x
     * @param locY  生成 y
     * @param state 当前难度参数（含属性倍率）
     * @param roll  [0,1) 随机数，决定机种
     * @param coin  [0,1) 随机数，决定横向速度方向
     */
    public abstract AbstractAircraft generateEnemy(int locX, int locY, DifficultyState state,
                                                   double roll, double coin);

    /** 随游戏时间提升难度；默认不提升 */
    public void increaseDifficulty(DifficultyState state) {
    }

    /**
     * Boss 血量。
     *
     * @param spawnCount 第几次召唤（从 1 开始）
     */
    public int bossHp(int spawnCount) {
        return BOSS_BASE_HP;
    }

    /**
     * 按三种敌机的出现概率生成敌机（剩余概率为王牌敌机），三个难度共用。
     */
    protected static AbstractAircraft spawnByProbability(int locX, int locY, DifficultyState state,
                                                         double roll, double coin,
                                                         double mobProb, double eliteProb,
                                                         double superiorProb) {
        double m = state.enemyAttributeMultiplier;
        if (roll < mobProb) {
            return EnemyFactories.MOB.createEnemy(locX, locY, 0, (int) (10 * m), (int) (30 * m));
        } else if (roll < mobProb + eliteProb) {
            return EnemyFactories.ELITE.createEnemy(locX, locY, 0, (int) (10 * m), (int) (30 * m));
        } else if (roll < mobProb + eliteProb + superiorProb) {
            int speedX = coin > 0.5 ? 2 : -2;
            return EnemyFactories.SUPERIOR.createEnemy(locX, locY,
                    (int) (speedX * m), (int) (8 * m), (int) (60 * m));
        } else {
            int speedX = coin > 0.5 ? 3 : -3;
            return EnemyFactories.ACE.createEnemy(locX, locY,
                    (int) (speedX * m), (int) (8 * m), (int) (90 * m));
        }
    }
}
